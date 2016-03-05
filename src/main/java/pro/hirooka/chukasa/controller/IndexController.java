package pro.hirooka.chukasa.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import pro.hirooka.chukasa.configuration.ChukasaConfiguration;
import pro.hirooka.chukasa.configuration.SystemConfiguration;
import pro.hirooka.chukasa.domain.PhysicalChannelModel;
import pro.hirooka.chukasa.domain.VideoFileModel;
import pro.hirooka.chukasa.service.ISystemService;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Objects.requireNonNull;

@Slf4j
@Controller
public class IndexController {

    private final SystemConfiguration systemConfiguration;
    private final ChukasaConfiguration chukasaConfiguration;
    private final ISystemService systemService;

    @Autowired
    public IndexController(SystemConfiguration systemConfiguration, ChukasaConfiguration chukasaConfiguration, ISystemService systemService){
        this.systemConfiguration = requireNonNull(systemConfiguration, "systemConfiguration");
        this.chukasaConfiguration = requireNonNull(chukasaConfiguration, "chukasaConfiguration");
        this.systemService = requireNonNull(systemService, "systemService");
    }

    @RequestMapping("/")
    String index(Model model){

        boolean isWebCamera = systemService.isWebCamera();

        Integer[] physicalChannelArray = chukasaConfiguration.getPhysicalChannel();
        List<Integer> physicalChannelList = Arrays.asList(physicalChannelArray);
        List<PhysicalChannelModel> physicalChannelModelList = new ArrayList<>();
        for(int physicalChannel : physicalChannelList){
            PhysicalChannelModel physicalChannelModel = new PhysicalChannelModel();
            physicalChannelModel.setNumber(physicalChannel);
            physicalChannelModel.setName(Integer.toString(physicalChannel));
            physicalChannelModelList.add(physicalChannelModel);
        }

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

        model.addAttribute("isWebCamera", isWebCamera);
        model.addAttribute("physicalChannelModelList", physicalChannelModelList);
        model.addAttribute("videoFileModelList", videoFileModelList);
        model.addAttribute("isRecorder", chukasaConfiguration.isRecorderEnabled());

        return "index";
    }
}
