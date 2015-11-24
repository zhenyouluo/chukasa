package pro.hirooka.chukasa.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import pro.hirooka.chukasa.configuration.SystemConfiguration;
import pro.hirooka.chukasa.domain.VideoFileModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

@Slf4j
@Controller
public class IndexController {

    private final SystemConfiguration systemConfiguration;

    @Autowired
    public IndexController(SystemConfiguration systemConfiguration){
        this.systemConfiguration = requireNonNull(systemConfiguration, "systemConfiguration");
    }

    @RequestMapping("/")
    String index(Model model){

        List<VideoFileModel> videoFileModelList = new ArrayList<>();

        File fileDirectory = new File(systemConfiguration.getFilePath());
        File[] fileArray = fileDirectory.listFiles();
        for(File file : fileArray){
            // TODO filter by extension
            VideoFileModel videoFileModel = new VideoFileModel();
            videoFileModel.setName(file.getName());
            videoFileModelList.add(videoFileModel);
        }

        model.addAttribute("videoFileModelList", videoFileModelList);

        return "index";
    }
}
