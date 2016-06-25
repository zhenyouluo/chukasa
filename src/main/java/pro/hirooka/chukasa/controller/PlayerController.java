package pro.hirooka.chukasa.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
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
@RestController
@RequestMapping("player")
public class PlayerController {

    // TODO: remove

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
    private HttpServletRequest request;
    @Autowired
    IChukasaTaskService chukasaTaskService;

    @RequestMapping(value = "/start", method = RequestMethod.POST)
    void play(@RequestBody ChukasaSettings chukasaSettings){

        if(chukasaModelManagementComponent.get().size() > 0){
            log.warn("cannot start streaming bacause previous one is not finished.");
        }else {

            log.info("ChukasaSettings -> {}", chukasaSettings.toString());

//            Map<String, String> env = System.getenv();
//            for(String name : env.keySet()){
//                log.info("{} {}", name, env.get(name));
//                if(name.equals("HOME")){
//                    String filePath = systemConfiguration.getFilePath().replace("$HOME", env.get(name));
//                    systemConfiguration.setFilePath(filePath);
//                }
//            }

            ChukasaModel chukasaModel = new ChukasaModel();

            // TODO: adaptive
            chukasaModel.setAdaptiveBitrateStreaming(0);

            chukasaModel.setChukasaConfiguration(chukasaConfiguration);
            chukasaModel.setSystemConfiguration(systemConfiguration);
            chukasaModel.setHlsConfiguration(hlsConfiguration);
            chukasaModel.setChukasaSettings(chukasaSettings);

            String streamRootPath = request.getSession().getServletContext().getRealPath("") + STREAM_ROOT_PATH_NAME;
            chukasaModel.setStreamRootPath(streamRootPath);

            chukasaModel = chukasaModelManagementComponent.create(0, chukasaModel);

            directoryCreator.setup(0);

            timerTaskParameterCalculator.calculate(0);

            chukasaTaskService.execute(0);
        }
    }

    @RequestMapping(value = "/stop", method = RequestMethod.GET)
    void stop(){
        chukasaStopper.stop();
    }

    @RequestMapping(value = "/remove", method = RequestMethod.GET)
    void remove(){

        if(chukasaModelManagementComponent.get().size() > 0){
            log.warn("cannot remove files bacause streaming is not finished.");
        }else {
            String streamRootPath = request.getSession().getServletContext().getRealPath("") + STREAM_ROOT_PATH_NAME;
            if(Files.exists(new File(streamRootPath).toPath())) {
                chukasaRemover.setStreamRootPath(streamRootPath);
                chukasaRemover.remove();
            }else {
                log.warn("cannot remove files bacause streamRootPath: {} does not exist.", streamRootPath);
            }
        }
    }



    @RequestMapping(value = "/start", method = RequestMethod.GET)
    String play(@RequestParam StreamingType streamingtype,
                @RequestParam int ch,
                @RequestParam int videobitrate,
                @RequestParam int duration,
                @RequestParam boolean encrypted){

        if(chukasaModelManagementComponent.get().size() > 0){
            log.warn("cannot start streaming bacause previous one is not finished.");
        }else {

//            Map<String, String> env = System.getenv();
//            for(String name : env.keySet()){
//                log.info("{} {}", name, env.get(name));
//                if(name.equals("HOME")){
//                    String filePath = systemConfiguration.getFilePath().replace("$HOME", env.get(name));
//                    systemConfiguration.setFilePath(filePath);
//                }
//            }

            ChukasaSettings chukasaSettings = new ChukasaSettings();
            chukasaSettings.setAdaptiveBitrateStreaming(0);
            chukasaSettings.setStreamingType(streamingtype);
            chukasaSettings.setCh(ch);
            chukasaSettings.setVideoBitrate(videobitrate);
            //chukasaSettings.setVideoResolutionType(VideoResolutionType.HD);
            //chukasaSettings.setCaptureResolutionType(VideoResolutionType.HD);
            chukasaSettings.setAudioBitrate(128);
            chukasaSettings.setTotalWebCameraLiveduration(duration);
            chukasaSettings.setEncrypted(encrypted);

            log.info("ChukasaSettings -> {}", chukasaSettings.toString());

            ChukasaModel chukasaModel = new ChukasaModel();

            // TODO: adaptive
            chukasaModel.setAdaptiveBitrateStreaming(0);

            chukasaModel.setChukasaConfiguration(chukasaConfiguration);
            chukasaModel.setSystemConfiguration(systemConfiguration);
            chukasaModel.setHlsConfiguration(hlsConfiguration);
            chukasaModel.setChukasaSettings(chukasaSettings);

            String streamRootPath = request.getSession().getServletContext().getRealPath("") + STREAM_ROOT_PATH_NAME;
            chukasaModel.setStreamRootPath(streamRootPath);

            chukasaModel = chukasaModelManagementComponent.create(0, chukasaModel);

            directoryCreator.setup(0);

            timerTaskParameterCalculator.calculate(0);

            chukasaTaskService.execute(0);

            chukasaModel = chukasaModelManagementComponent.get(0);

            String playlistURI = "/"
                    + STREAM_ROOT_PATH_NAME
                    + FILE_SEPARATOR
                    + LIVE_PATH_NAME
                    + FILE_SEPARATOR
                    + chukasaModel.getChukasaSettings().getVideoBitrate()
                    + FILE_SEPARATOR
                    + M3U8_FILE_NAME;
            log.info(playlistURI);

            //return new ModelAndView("redirect:" + playlistURI);
            return "redirect:" + playlistURI;

        }

        return null;
    }

    @RequestMapping(value = "tvos", method = RequestMethod.GET, produces = "application/javascript")
    String tvOS(){
        return "";
    }

}