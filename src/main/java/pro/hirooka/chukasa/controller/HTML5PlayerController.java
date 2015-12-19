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
import pro.hirooka.chukasa.domain.ChukasaModel;
import pro.hirooka.chukasa.domain.ChukasaSettings;
import pro.hirooka.chukasa.domain.HTML5PlayerModel;
import pro.hirooka.chukasa.domain.type.PlaylistType;
import pro.hirooka.chukasa.domain.type.StreamingType;
import pro.hirooka.chukasa.domain.type.VideoResolutionType;
import pro.hirooka.chukasa.handler.ChukasaRemover;
import pro.hirooka.chukasa.handler.ChukasaStopper;
import pro.hirooka.chukasa.handler.ChukasaThreadHandler;
import pro.hirooka.chukasa.operator.IDirectoryCreator;
import pro.hirooka.chukasa.operator.ITimerTaskParameterCalculator;
import pro.hirooka.chukasa.service.IChukasaModelManagementComponent;

import javax.servlet.http.HttpServletRequest;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;

import static java.util.Objects.requireNonNull;

@Slf4j
@Controller
@RequestMapping("video")
public class HTML5PlayerController {

    static final String FILE_SEPARATOR = System.getProperty("file.separator");

    private final ChukasaConfiguration chukasaConfiguration;
    private final SystemConfiguration systemConfiguration;
    private final HLSConfiguration hlsConfiguration;
    private final IChukasaModelManagementComponent chukasaModelManagementComponent;
    private final IDirectoryCreator directoryCreator;
    private final ITimerTaskParameterCalculator timerTaskParameterCalculator;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    public HTML5PlayerController(
            ChukasaConfiguration chukasaConfiguration,
            SystemConfiguration systemConfiguration,
            HLSConfiguration hlsConfiguration,
            IChukasaModelManagementComponent chukasaModelManagementComponent,
            IDirectoryCreator directoryCreator,
            ITimerTaskParameterCalculator timerTaskParameterCalculator
    ) {
        this.chukasaConfiguration = requireNonNull(chukasaConfiguration, "chukasaConfiguration");
        this.systemConfiguration = requireNonNull(systemConfiguration, "systemConfiguration");
        this.hlsConfiguration = requireNonNull(hlsConfiguration, "hlsConfiguration");
        this.chukasaModelManagementComponent = requireNonNull(chukasaModelManagementComponent, "chukasaModelManagementComponent");
        this.directoryCreator = requireNonNull(directoryCreator, "directoryCreator");
        this.timerTaskParameterCalculator = requireNonNull(timerTaskParameterCalculator, "timerTaskParameterCalculator");
    }

    @RequestMapping(method = RequestMethod.POST)
    String play(Model model, @Validated ChukasaSettings chukasaSettings, BindingResult bindingResult){

        if(bindingResult.hasErrors()){
            return null;
        }

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

            String streamRootPath = request.getSession().getServletContext().getRealPath("") + chukasaConfiguration.getStreamRootPathName();
            chukasaModel.setStreamRootPath(streamRootPath);

            chukasaModel = chukasaModelManagementComponent.create(0, chukasaModel);

            directoryCreator.setup(0);

            timerTaskParameterCalculator.calculate(0);

            ChukasaThreadHandler chukasaThreadHandler = new ChukasaThreadHandler(chukasaModel.getAdaptiveBitrateStreaming(), chukasaModelManagementComponent);
            Thread thread = new Thread(chukasaThreadHandler);
            thread.start();

            chukasaModel = chukasaModelManagementComponent.get(0);

            String playlistURI = "";
            if(chukasaModel.getChukasaSettings().getStreamingType() == StreamingType.USB_CAMERA
                    || chukasaModel.getChukasaSettings().getStreamingType() == StreamingType.CAPTURE){
                playlistURI = "/"
                        + chukasaModel.getChukasaConfiguration().getStreamRootPathName()
                        + FILE_SEPARATOR
                        + chukasaModel.getChukasaConfiguration().getLivePathName()
                        + FILE_SEPARATOR
                        + chukasaModel.getChukasaSettings().getVideoBitrate()
                        + FILE_SEPARATOR
                        + chukasaModel.getChukasaConfiguration().getM3u8PlaylistName();
            }else if(chukasaModel.getChukasaSettings().getStreamingType() == StreamingType.FILE){
                playlistURI = "/"
                        + chukasaModel.getChukasaConfiguration().getStreamRootPathName()
                        + FILE_SEPARATOR
                        + chukasaModel.getChukasaSettings().getFileName()
                        + FILE_SEPARATOR
                        + chukasaModel.getChukasaSettings().getVideoBitrate()
                        + FILE_SEPARATOR
                        + chukasaModel.getChukasaConfiguration().getM3u8PlaylistName();
            }
            log.info(playlistURI);

            HTML5PlayerModel html5PlayerModel = new HTML5PlayerModel();
            html5PlayerModel.setPlaylistURI(playlistURI);
            model.addAttribute("html5PlayerModel", html5PlayerModel);

            return "player";
        }

        return null;
    }

    @RequestMapping(method = RequestMethod.GET)
    String play(Model model,
                @RequestParam StreamingType streamingtype,
                @RequestParam int ch,
                @RequestParam int videobitrate,
                @RequestParam int duration,
                @RequestParam boolean encrypted){

        if(chukasaModelManagementComponent.get().size() > 0){
            log.warn("cannot start streaming bacause previous one is not finished.");
        }else {

            Map<String, String> env = System.getenv();
            for (String name : env.keySet()) {
                log.info("{} {}", name, env.get(name));
                if (name.equals("HOME")) {
                    String filePath = systemConfiguration.getFilePath().replace("$HOME", env.get(name));
                    systemConfiguration.setFilePath(filePath);
                }
            }

            ChukasaSettings chukasaSettings = new ChukasaSettings();
            chukasaSettings.setAdaptiveBitrateStreaming(0);
            chukasaSettings.setStreamingType(streamingtype);
            chukasaSettings.setCh(ch);
            chukasaSettings.setVideoBitrate(videobitrate);
            chukasaSettings.setVideoResolutionType(VideoResolutionType.HD);
            chukasaSettings.setCaptureResolutionType(VideoResolutionType.HD);
            chukasaSettings.setAudioBitrate(128);
            chukasaSettings.setTotalUSBCameraLiveduration(duration);
            chukasaSettings.setEncrypted(encrypted);

            log.info("ChukasaSettings -> {}", chukasaSettings.toString());

            ChukasaModel chukasaModel = new ChukasaModel();

            // TODO: adaptive
            chukasaModel.setAdaptiveBitrateStreaming(0);

            chukasaModel.setChukasaConfiguration(chukasaConfiguration);
            chukasaModel.setSystemConfiguration(systemConfiguration);
            chukasaModel.setHlsConfiguration(hlsConfiguration);
            chukasaModel.setChukasaSettings(chukasaSettings);
            chukasaModel.setPlaylistType(PlaylistType.EVENT);

            String streamRootPath = request.getSession().getServletContext().getRealPath("") + chukasaConfiguration.getStreamRootPathName();
            chukasaModel.setStreamRootPath(streamRootPath);

            chukasaModel = chukasaModelManagementComponent.create(0, chukasaModel);

            directoryCreator.setup(0);

            timerTaskParameterCalculator.calculate(0);

            ChukasaThreadHandler chukasaThreadHandler = new ChukasaThreadHandler(chukasaModel.getAdaptiveBitrateStreaming(), chukasaModelManagementComponent);
            Thread thread = new Thread(chukasaThreadHandler);
            thread.start();

            chukasaModel = chukasaModelManagementComponent.get(0);

            String playlistURI = "/"
                    + chukasaModel.getChukasaConfiguration().getStreamRootPathName()
                    + FILE_SEPARATOR
                    + chukasaModel.getChukasaConfiguration().getLivePathName()
                    + FILE_SEPARATOR
                    + chukasaModel.getChukasaSettings().getVideoBitrate()
                    + FILE_SEPARATOR
                    + chukasaModel.getChukasaConfiguration().getM3u8PlaylistName();
            log.info(playlistURI);

            HTML5PlayerModel html5PlayerModel = new HTML5PlayerModel();
            html5PlayerModel.setPlaylistURI(playlistURI);
            model.addAttribute("html5PlayerModel", html5PlayerModel);

            return "player";
        }
        return null;
    }

    @RequestMapping(value = "/stop", method = RequestMethod.GET)
    String stop(){
        ChukasaStopper chukasaStopper = new ChukasaStopper(chukasaModelManagementComponent);
        chukasaStopper.stop();

        return "redirect:/";
    }

    @RequestMapping(value = "/remove", method = RequestMethod.GET)
    String remove(){

        if(chukasaModelManagementComponent.get().size() > 0){
            log.warn("cannot remove files bacause streaming is not finished.");
        }else {
            String streamRootPath = request.getSession().getServletContext().getRealPath("") + chukasaConfiguration.getStreamRootPathName();
            if(Files.exists(new File(streamRootPath).toPath())) {
                ChukasaRemover chukasaRemover = new ChukasaRemover(streamRootPath, systemConfiguration);
                chukasaRemover.remove();
            }else {
                log.warn("cannot remove files bacause streamRootPath: {} does not exist.", streamRootPath);
            }
        }

        return "redirect:/";
    }

}
