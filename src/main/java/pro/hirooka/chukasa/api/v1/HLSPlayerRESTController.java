package pro.hirooka.chukasa.api.v1;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import pro.hirooka.chukasa.api.v1.exception.ChukasaBadRequestException;
import pro.hirooka.chukasa.api.v1.exception.ChukasaInternalServerErrorException;
import pro.hirooka.chukasa.configuration.ChukasaConfiguration;
import pro.hirooka.chukasa.configuration.HLSConfiguration;
import pro.hirooka.chukasa.configuration.SystemConfiguration;
import pro.hirooka.chukasa.domain.model.chukasa.ChukasaModel;
import pro.hirooka.chukasa.domain.model.chukasa.ChukasaResponseModel;
import pro.hirooka.chukasa.domain.model.chukasa.ChukasaSettings;
import pro.hirooka.chukasa.domain.model.chukasa.HLSPlaylist;
import pro.hirooka.chukasa.domain.model.chukasa.enums.VideoCodecType;
import pro.hirooka.chukasa.domain.service.chukasa.eraser.ChukasaRemover;
import pro.hirooka.chukasa.domain.service.chukasa.stopper.ChukasaStopper;
import pro.hirooka.chukasa.domain.service.chukasa.IChukasaModelManagementComponent;
import pro.hirooka.chukasa.domain.service.chukasa.IChukasaTaskService;
import pro.hirooka.chukasa.domain.service.chukasa.ISystemService;
import pro.hirooka.chukasa.api.v1.helper.ChukasaUtility;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.nio.file.Files;
import java.util.UUID;

import static pro.hirooka.chukasa.domain.model.chukasa.constants.ChukasaConstant.*;

@Slf4j
@RestController
@RequestMapping("api/v1/hls")
public class  HLSPlayerRESTController {

    @Autowired
    ChukasaConfiguration chukasaConfiguration;
    @Autowired
    SystemConfiguration systemConfiguration;
    @Autowired
    HLSConfiguration hlsConfiguration;
    @Autowired
    IChukasaModelManagementComponent chukasaModelManagementComponent;
    @Autowired
    ChukasaStopper chukasaStopper;
    @Autowired
    ChukasaRemover chukasaRemover;
    @Autowired
    HttpServletRequest httpServletRequest;
    @Autowired
    IChukasaTaskService chukasaTaskService;
    @Autowired
    ISystemService systemService;

    @RequestMapping(value = "/start", method = RequestMethod.POST)
    HLSPlaylist play(@RequestBody @Validated ChukasaSettings chukasaSettings) throws ChukasaBadRequestException, ChukasaInternalServerErrorException {

        VideoCodecType videoCodecType = systemService.getVideoCodecType();
        if(videoCodecType.equals(VideoCodecType.UNKNOWN)){
            throw new ChukasaInternalServerErrorException("FFmpeg configuration is not suitable for this application.");
        }

        String userAgent = httpServletRequest.getHeader("user-agent");
        if(!userAgent.contains(USER_AGENT)){
            throw new ChukasaBadRequestException("User-Agent is invalid");
        }

        ChukasaUtility.initializeRunner(chukasaModelManagementComponent, systemConfiguration);
        if(chukasaModelManagementComponent.get().size() > 0){
            throw new ChukasaInternalServerErrorException("Cannot start streaming bacause previous one is not finished.");
        }

        log.info("ChukasaSettings -> {}", chukasaSettings.toString());

        ChukasaModel chukasaModel = new ChukasaModel();
        chukasaModel.setChukasaConfiguration(chukasaConfiguration);
        chukasaModel.setSystemConfiguration(systemConfiguration);
        chukasaModel.setHlsConfiguration(hlsConfiguration);
        chukasaModel.setChukasaSettings(chukasaSettings);

        chukasaModel.setUuid(UUID.randomUUID());
        chukasaModel.setAdaptiveBitrateStreaming(0);
        chukasaModel.setVideoCodecType(videoCodecType);

        chukasaModel = ChukasaUtility.operateEncodingSettings(chukasaModel);
        if(chukasaModel == null){
            throw new ChukasaBadRequestException("EncodingSettingsType is invalid");
        }

        String streamRootPath = httpServletRequest.getSession().getServletContext().getRealPath("") + STREAM_ROOT_PATH_NAME;
        chukasaModel.setStreamRootPath(streamRootPath);
        chukasaModel = ChukasaUtility.createChukasaDerectory(chukasaModel);
        chukasaModel = ChukasaUtility.calculateTimerTaskParameter(chukasaModel);

        String playlistURI = ChukasaUtility.buildM3u8URI(chukasaModel);
        if(playlistURI.equals("/")){
            throw new ChukasaInternalServerErrorException("Cannot create playlist.");
        }

        chukasaModelManagementComponent.create(0, chukasaModel);

        chukasaTaskService.execute(0);

        HLSPlaylist hlsPlaylist = new HLSPlaylist();
        hlsPlaylist.setUri(playlistURI);
        return hlsPlaylist;
    }

    @RequestMapping(value = "/stop", method = RequestMethod.GET)
    ChukasaResponseModel stop() throws ChukasaInternalServerErrorException {
        chukasaStopper.stop();
        return remove();
    }

    @RequestMapping(value = "/remove", method = RequestMethod.GET)
    ChukasaResponseModel remove() throws ChukasaInternalServerErrorException {
        removeStreamingFiles();
        ChukasaResponseModel chukasaResponseModel = new ChukasaResponseModel();
        chukasaResponseModel.setMessage("Streaming stopped successfully.");
        return chukasaResponseModel;
    }

    private void removeStreamingFiles() throws ChukasaInternalServerErrorException {
        String streamRootPath = httpServletRequest.getSession().getServletContext().getRealPath("") + STREAM_ROOT_PATH_NAME;
        if(Files.exists(new File(streamRootPath).toPath())) {
            chukasaRemover.setStreamRootPath(streamRootPath);
            chukasaRemover.remove();
        }else {
            log.warn("cannot remove files bacause streamRootPath: {} does not exist.", streamRootPath);
            throw new ChukasaInternalServerErrorException("Cannot remove files bacause streamRootPath does not exist.");
        }
    }
}
