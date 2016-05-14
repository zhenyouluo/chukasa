package pro.hirooka.chukasa.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import pro.hirooka.chukasa.configuration.ChukasaConfiguration;
import pro.hirooka.chukasa.configuration.HLSConfiguration;
import pro.hirooka.chukasa.configuration.SystemConfiguration;
import pro.hirooka.chukasa.domain.chukasa.ChukasaModel;
import pro.hirooka.chukasa.domain.chukasa.ChukasaSettings;
import pro.hirooka.chukasa.domain.chukasa.type.StreamingType;
import pro.hirooka.chukasa.handler.ChukasaRemover;
import pro.hirooka.chukasa.handler.ChukasaStopper;
import pro.hirooka.chukasa.handler.ChukasaThreadHandler;
import pro.hirooka.chukasa.operator.IDirectoryCreator;
import pro.hirooka.chukasa.operator.ITimerTaskParameterCalculator;
import pro.hirooka.chukasa.service.chukasa.IChukasaModelManagementComponent;

import javax.servlet.http.HttpServletRequest;

import java.io.File;
import java.nio.file.Files;

import static java.util.Objects.requireNonNull;

@Slf4j
@Controller
@RequestMapping("ios/player")
public class IOSPlayerController {

    static final String FILE_SEPARATOR = System.getProperty("file.separator");

    private final ChukasaConfiguration chukasaConfiguration;
    private final SystemConfiguration systemConfiguration;
    private final HLSConfiguration hlsConfiguration;
    private final IChukasaModelManagementComponent chukasaModelManagementComponent;
    private final IDirectoryCreator directoryCreator;
    private final ITimerTaskParameterCalculator timerTaskParameterCalculator;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    public IOSPlayerController(
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

            String streamRootPath = httpServletRequest.getSession().getServletContext().getRealPath("") + chukasaConfiguration.getStreamRootPathName();
            chukasaModel.setStreamRootPath(streamRootPath);

            chukasaModel = chukasaModelManagementComponent.create(0, chukasaModel);

            directoryCreator.setup(0);

            timerTaskParameterCalculator.calculate(0);

            ChukasaThreadHandler chukasaThreadHandler = new ChukasaThreadHandler(chukasaModel.getAdaptiveBitrateStreaming(), chukasaModelManagementComponent);
            Thread thread = new Thread(chukasaThreadHandler);
            thread.start();

            chukasaModel = chukasaModelManagementComponent.get(0);

            String playlistURI = "";
            if(chukasaModel.getChukasaSettings().getStreamingType() == StreamingType.WEB_CAMERA
                    || chukasaModel.getChukasaSettings().getStreamingType() == StreamingType.CAPTURE){
                playlistURI = "/"
                        + chukasaModel.getChukasaConfiguration().getStreamRootPathName()
                        + FILE_SEPARATOR
                        + chukasaModel.getChukasaConfiguration().getLivePathName()
                        + FILE_SEPARATOR
                        + chukasaModel.getChukasaSettings().getVideoBitrate()
                        + FILE_SEPARATOR
                        + chukasaModel.getChukasaConfiguration().getM3u8PlaylistName();
            }else if(chukasaModel.getChukasaSettings().getStreamingType() == StreamingType.FILE
                    || chukasaModel.getChukasaSettings().getStreamingType() == StreamingType.OKKAKE){
                playlistURI = "/"
                        + chukasaModel.getChukasaConfiguration().getStreamRootPathName()
                        + FILE_SEPARATOR
                        + chukasaModel.getChukasaSettings().getFileName()
                        + FILE_SEPARATOR
                        + chukasaModel.getChukasaSettings().getVideoBitrate()
                        + FILE_SEPARATOR
                        + chukasaModel.getChukasaConfiguration().getM3u8PlaylistName();
            }
            log.info("playlistURI = {}", playlistURI);

            return "redirect:" + playlistURI;
        }
        return "index";
    }

    @RequestMapping(value = "/stop", method = RequestMethod.GET)
    String stop(){
        ChukasaStopper chukasaStopper = new ChukasaStopper(chukasaModelManagementComponent);
        chukasaStopper.stop();
        return "redirect:/video/remove";
    }

    @RequestMapping(value = "/remove", method = RequestMethod.GET)
    String remove(){

        if(chukasaModelManagementComponent.get().size() > 0){
            log.warn("cannot remove files bacause streaming is not finished.");
        }else {
            String streamRootPath = httpServletRequest.getSession().getServletContext().getRealPath("") + chukasaConfiguration.getStreamRootPathName();
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
