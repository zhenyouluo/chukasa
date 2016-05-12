package pro.hirooka.chukasa.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import pro.hirooka.chukasa.configuration.ChukasaConfiguration;
import pro.hirooka.chukasa.configuration.SystemConfiguration;
import pro.hirooka.chukasa.domain.EPGDumpProgramInformation;
import pro.hirooka.chukasa.domain.VideoFileModel;
import pro.hirooka.chukasa.service.IEPGDumpProgramTableService;
import pro.hirooka.chukasa.service.ISystemService;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.*;

import static java.util.Objects.requireNonNull;

@Slf4j
@Controller
public class IndexController {

    private final SystemConfiguration systemConfiguration;
    private final ChukasaConfiguration chukasaConfiguration;
    private final ISystemService systemService;
    private final IEPGDumpProgramTableService epgDumpProgramTableService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    public IndexController(
            SystemConfiguration systemConfiguration,
            ChukasaConfiguration chukasaConfiguration,
            ISystemService systemService,
            IEPGDumpProgramTableService epgDumpProgramTableService){
        this.systemConfiguration = requireNonNull(systemConfiguration, "systemConfiguration");
        this.chukasaConfiguration = requireNonNull(chukasaConfiguration, "chukasaConfiguration");
        this.systemService = requireNonNull(systemService, "systemService");
        this.epgDumpProgramTableService = requireNonNull(epgDumpProgramTableService, "epgDumpProgramTableService");
    }

    @RequestMapping("/")
    String index(Model model){

        // TODO: 正確に判断する
        boolean isSupported = false;
        String userAgent = httpServletRequest.getHeader("user-agent");
        if(userAgent.contains("Mac OS X 10_11") && (userAgent.contains("Version") && userAgent.split("Version/")[1].split(" ")[0].contains("9"))){
            isSupported = true;
            log.info("{}", userAgent);
        }else if(userAgent.contains("iPhone OS 9") && (userAgent.contains("Version") && userAgent.split("Version/")[1].split(" ")[0].contains("9"))){
            isSupported = true;
            log.info("{}", userAgent);
        }else if(userAgent.contains("iPad; CPU OS 9") && (userAgent.contains("Version") && userAgent.split("Version/")[1].split(" ")[0].contains("9"))){
            isSupported = true;
            log.info("{}", userAgent);
        }else if(userAgent.contains("Windows") && userAgent.contains("Edge/")){
            isSupported = true;
            log.info("{}", userAgent);
        }

        boolean isPTx = systemService.isPTx();
        boolean isEPGDump = systemService.isEPGDump();
        boolean isMongoDB = systemService.isMongoDB();
        boolean isWebCamera = systemService.isWebCamera();

        List<VideoFileModel> videoFileModelList = new ArrayList<>();

        File fileDirectory = new File(systemConfiguration.getFilePath());
        File[] fileArray = fileDirectory.listFiles();
        if(fileArray != null) {
            String[] videoFileExtensionArray = chukasaConfiguration.getVideoFileExtension();
            List<String> videoFileExtensionList = Arrays.asList(videoFileExtensionArray);
            for (File file : fileArray) {
                for(String videoFileExtension : videoFileExtensionList){
                   if(file.getName().endsWith("." + videoFileExtension)){
                       VideoFileModel videoFileModel = new VideoFileModel();
                       videoFileModel.setName(file.getName());
                       videoFileModelList.add(videoFileModel);
                   }
                }
            }
        }else{
            log.warn("'{}' does not exist.", fileDirectory);
        }

        List<EPGDumpProgramInformation> epgDumpProgramInformationList = new ArrayList<>();
        if(systemService.isMongoDB() && systemService.isEPGDump()){
            epgDumpProgramInformationList = epgDumpProgramTableService.readByNow(new Date().getTime() * 10);
        }

        model.addAttribute("isSupported", isSupported);
        model.addAttribute("isPTx", isPTx);
        model.addAttribute("isEPGDump", isEPGDump);
        model.addAttribute("isMongoDB", isMongoDB);
        model.addAttribute("isWebCamera", isWebCamera);
        model.addAttribute("videoFileModelList", videoFileModelList);
        model.addAttribute("epgDumpProgramInformationList", epgDumpProgramInformationList);

        return "index";
    }
}
