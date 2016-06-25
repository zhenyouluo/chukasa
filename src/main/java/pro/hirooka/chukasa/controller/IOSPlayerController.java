package pro.hirooka.chukasa.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import pro.hirooka.chukasa.ChukasaConstant;
import pro.hirooka.chukasa.configuration.ChukasaConfiguration;
import pro.hirooka.chukasa.configuration.HLSConfiguration;
import pro.hirooka.chukasa.configuration.SystemConfiguration;
import pro.hirooka.chukasa.domain.chukasa.ChukasaModel;
import pro.hirooka.chukasa.domain.chukasa.ChukasaSettings;
import pro.hirooka.chukasa.domain.chukasa.type.StreamingType;
import pro.hirooka.chukasa.handler.ChukasaRemover;
import pro.hirooka.chukasa.handler.ChukasaStopper;
import pro.hirooka.chukasa.operator.IDirectoryCreator;
import pro.hirooka.chukasa.operator.ITimerTaskParameterCalculator;
import pro.hirooka.chukasa.service.chukasa.IChukasaModelManagementComponent;
import pro.hirooka.chukasa.service.chukasa.IChukasaTaskService;

import javax.servlet.http.HttpServletRequest;

import java.io.File;
import java.nio.file.Files;

@Slf4j
@Controller
@RequestMapping("ios/player")
public class IOSPlayerController {

    static final String FILE_SEPARATOR = System.getProperty("file.separator");

    final String STREAM_ROOT_PATH_NAME = ChukasaConstant.STREAM_ROOT_PATH_NAME;
    final String LIVE_PATH_NAME = ChukasaConstant.LIVE_PATH_NAME;
    final String M3U8_FILE_NAME_PREFIX = ChukasaConstant.M3U8_FILE_NAME_PREFIX;
    final String M3U8_FILE_EXTENSION = ChukasaConstant.M3U8_FILE_EXTENSION;
    final String M3U8_FILE_NAME = M3U8_FILE_NAME_PREFIX + M3U8_FILE_EXTENSION;

    @Autowired
    ChukasaConfiguration chukasaConfiguration;
    @Autowired
    SystemConfiguration systemConfiguration;
    @Autowired
    HLSConfiguration hlsConfiguration;
    @Autowired
    IChukasaModelManagementComponent chukasaModelManagementComponent;
    @Autowired
    IDirectoryCreator directoryCreator;
    @Autowired
    ITimerTaskParameterCalculator timerTaskParameterCalculator;
    @Autowired
    ChukasaStopper chukasaStopper;
    @Autowired
    ChukasaRemover chukasaRemover;
    @Autowired
    HttpServletRequest httpServletRequest;
    @Autowired
    IChukasaTaskService chukasaTaskService;

    @RequestMapping(value = "/start", method = RequestMethod.POST)
    String play(@RequestBody ChukasaSettings chukasaSettings){

        if(chukasaModelManagementComponent.get().size() > 0){

            log.warn("cannot start streaming bacause previous one is not finished.");

        }else{

            String userAgent = httpServletRequest.getHeader("user-agent");
            if(!userAgent.contains("chukasa-ios")){
                return "index";
            }

            log.info("ChukasaSettings -> {}", chukasaSettings.toString());

            ChukasaModel chukasaModel = new ChukasaModel();

            chukasaModel.setAdaptiveBitrateStreaming(0);

            chukasaModel.setChukasaConfiguration(chukasaConfiguration);
            chukasaModel.setSystemConfiguration(systemConfiguration);
            chukasaModel.setHlsConfiguration(hlsConfiguration);
            chukasaModel.setChukasaSettings(chukasaSettings);

            String encodingSettings = chukasaModel.getChukasaSettings().getEncodingSettingsType().getName();
            String videoResolution = encodingSettings.split("-")[0];
            int videoBitrate = Integer.parseInt(encodingSettings.split("-")[1]);
            int audioBitrate = Integer.parseInt(encodingSettings.split("-")[2]);
            chukasaModel.getChukasaSettings().setVideoResolution(videoResolution);
            chukasaModel.getChukasaSettings().setVideoBitrate(videoBitrate);
            chukasaModel.getChukasaSettings().setAudioBitrate(audioBitrate);

            String streamRootPath = httpServletRequest.getSession().getServletContext().getRealPath("") + STREAM_ROOT_PATH_NAME;
            chukasaModel.setStreamRootPath(streamRootPath);

            chukasaModel = chukasaModelManagementComponent.create(0, chukasaModel);

            directoryCreator.setup(0);

            timerTaskParameterCalculator.calculate(0);

            chukasaTaskService.execute(0);

            chukasaModel = chukasaModelManagementComponent.get(0);

            String playlistURI = "";
            if(chukasaModel.getChukasaSettings().getStreamingType() == StreamingType.WEB_CAMERA
                    || chukasaModel.getChukasaSettings().getStreamingType() == StreamingType.CAPTURE){
                playlistURI = "/"
                        + STREAM_ROOT_PATH_NAME
                        + FILE_SEPARATOR
                        + LIVE_PATH_NAME
                        + FILE_SEPARATOR
                        + chukasaModel.getChukasaSettings().getVideoBitrate()
                        + FILE_SEPARATOR
                        + M3U8_FILE_NAME;
            }else if(chukasaModel.getChukasaSettings().getStreamingType() == StreamingType.FILE
                    || chukasaModel.getChukasaSettings().getStreamingType() == StreamingType.OKKAKE){
                playlistURI = "/"
                        + STREAM_ROOT_PATH_NAME
                        + FILE_SEPARATOR
                        + chukasaModel.getChukasaSettings().getFileName()
                        + FILE_SEPARATOR
                        + chukasaModel.getChukasaSettings().getVideoBitrate()
                        + FILE_SEPARATOR
                        + M3U8_FILE_NAME;
            }
            log.info("playlistURI = {}", playlistURI);

            return "redirect:" + playlistURI;
        }
        return "index";
    }

    @RequestMapping(value = "/stop", method = RequestMethod.GET)
    String stop(){
        chukasaStopper.stop();
        return "redirect:/video/remove";
    }

    @RequestMapping(value = "/remove", method = RequestMethod.GET)
    String remove(){

        if(chukasaModelManagementComponent.get().size() > 0){
            log.warn("cannot remove files bacause streaming is not finished.");
        }else {
            String streamRootPath = httpServletRequest.getSession().getServletContext().getRealPath("") + STREAM_ROOT_PATH_NAME;
            if(Files.exists(new File(streamRootPath).toPath())) {
                chukasaRemover.setStreamRootPath(streamRootPath);
                chukasaRemover.remove();
            }else {
                log.warn("cannot remove files bacause streamRootPath: {} does not exist.", streamRootPath);
            }
        }
        return "redirect:/";
    }
}
