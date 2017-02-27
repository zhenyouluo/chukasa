package pro.hirooka.chukasa.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import pro.hirooka.chukasa.configuration.ChukasaConfiguration;
import pro.hirooka.chukasa.configuration.HLSConfiguration;
import pro.hirooka.chukasa.configuration.SystemConfiguration;
import pro.hirooka.chukasa.api.v1.helper.IChukasaBrowserDetector;
import pro.hirooka.chukasa.domain.model.chukasa.ChukasaModel;
import pro.hirooka.chukasa.domain.model.chukasa.ChukasaSettings;
import pro.hirooka.chukasa.domain.model.chukasa.HTML5PlayerModel;
import pro.hirooka.chukasa.domain.model.chukasa.enums.StreamingType;
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
@Controller
@RequestMapping("video")
public class HTML5PlayerController {

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
    @Autowired
    IChukasaBrowserDetector chukasaBrowserDetector;

    @RequestMapping(method = RequestMethod.POST)
    String play(Model model, @Validated ChukasaSettings chukasaSettings, BindingResult bindingResult){

        if(bindingResult.hasErrors()){
            return "index";
        }

        VideoCodecType videoCodecType = systemService.getVideoCodecType();
        if(videoCodecType.equals(VideoCodecType.UNKNOWN)){
            return "index";
        }

        ChukasaUtility.initializeRunner(chukasaModelManagementComponent, systemConfiguration);
        if(chukasaModelManagementComponent.get().size() > 0){
            return "index";
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
            return "index";
        }

        String streamRootPath = httpServletRequest.getSession().getServletContext().getRealPath("") + STREAM_ROOT_PATH_NAME;
        chukasaModel.setStreamRootPath(streamRootPath);
        chukasaModel = ChukasaUtility.createChukasaDerectory(chukasaModel);
        chukasaModel = ChukasaUtility.calculateTimerTaskParameter(chukasaModel);

        String playlistURI = ChukasaUtility.buildM3u8URI(chukasaModel);
        if(playlistURI.equals("/")){
            return "index";
        }

        chukasaModelManagementComponent.create(0, chukasaModel);

        chukasaTaskService.execute(0);

        HTML5PlayerModel html5PlayerModel = new HTML5PlayerModel();
        html5PlayerModel.setPlaylistURI(playlistURI);
        model.addAttribute("html5PlayerModel", html5PlayerModel);

        String userAgent = httpServletRequest.getHeader("user-agent");
        log.info("userAgent: {}", userAgent);
        if(chukasaBrowserDetector.isNativeSupported(userAgent)){
            return "player";
        }else if(chukasaBrowserDetector.isAlternativeSupported(userAgent)){
            return ALTERNATIVE_HLS_PLAYER + "-player";
        }else{
            return "index";
        }
    }

    // TODO: GET 修正
    @RequestMapping(method = RequestMethod.GET)
    String play(Model model,
                @RequestParam StreamingType streamingtype,
                @RequestParam int ch,
                @RequestParam int videobitrate,
                @RequestParam boolean encrypted){

        ChukasaSettings chukasaSettings = new ChukasaSettings();
        chukasaSettings.setAdaptiveBitrateStreaming(0);
        chukasaSettings.setStreamingType(streamingtype);
        chukasaSettings.setCh(ch);
        chukasaSettings.setVideoBitrate(videobitrate);
        //chukasaSettings.setVideoResolutionType(VideoResolutionType.HD);
        //chukasaSettings.setCaptureResolutionType(VideoResolutionType.HD);
        chukasaSettings.setAudioBitrate(128);
        chukasaSettings.setEncrypted(encrypted);

        log.info("ChukasaSettings -> {}", chukasaSettings.toString());

        return "index";
    }

    @RequestMapping(value = "/stop", method = RequestMethod.GET)
    String stop(){
        chukasaStopper.stop();
        return "redirect:/video/remove";
    }

    @RequestMapping(value = "/remove", method = RequestMethod.GET)
    String remove(){
        String streamRootPath = httpServletRequest.getSession().getServletContext().getRealPath("") + STREAM_ROOT_PATH_NAME;
        if(Files.exists(new File(streamRootPath).toPath())) {
            chukasaRemover.setStreamRootPath(streamRootPath);
            chukasaRemover.remove();
        }else {
            log.warn("cannot remove files bacause streamRootPath: {} does not exist.", streamRootPath);
        }
        return "redirect:/";
    }
}
