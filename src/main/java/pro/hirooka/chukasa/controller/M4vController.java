package pro.hirooka.chukasa.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;
import pro.hirooka.chukasa.configuration.SystemConfiguration;
import pro.hirooka.chukasa.domain.model.recorder.M4vFile;

import static pro.hirooka.chukasa.domain.model.chukasa.constants.ChukasaConstant.FILE_SEPARATOR;

@Slf4j
@RestController
@RequestMapping("api/1/m4v")
public class M4vController {

    @Autowired
    SystemConfiguration systemConfiguration;

    @RequestMapping(value = "/", method = RequestMethod.POST)
    Resource downloadFile(@RequestBody M4vFile m4vFile){

        String filePath = "";

        switch (m4vFile.getType()){
            case PHONE:
            case PAD:
                filePath = systemConfiguration.getFilePath() + FILE_SEPARATOR + m4vFile.getName() + ".ts.m4v";
                break;
            case WATCH:
                filePath = systemConfiguration.getFilePath() + FILE_SEPARATOR + m4vFile.getName() + ".ts.watch.m4v";
                break;
            default:
                break;
        }

        return new FileSystemResource(filePath);
    }

    @RequestMapping(value = "/{name}", method = RequestMethod.GET)
    Resource downloadFile(@PathVariable String name){
        String filePath = systemConfiguration.getFilePath() + FILE_SEPARATOR + name + ".ts.watch.m4v";
        return new FileSystemResource(filePath);
    }
}
