package pro.hirooka.chukasa.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import pro.hirooka.chukasa.domain.service.chukasa.IChukasaModelManagementComponent;
import pro.hirooka.chukasa.domain.service.chukasa.SystemService;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Controller
@RequestMapping("system")
public class SystemController {

    @Autowired
    IChukasaModelManagementComponent chukasaModelManagementComponent;
    @Autowired
    HttpServletRequest httpServletRequest;
    @Autowired
    SystemService systemService;

    @RequestMapping("/")
    String index(Model model){

        boolean isFFmpeg = systemService.isFFmpeg();
        boolean isPTx = systemService.isTuner();
        boolean isRecpt1 = systemService.isRecxxx();
        boolean isEpgdump = systemService.isEpgdump();
        boolean isMongoDB = systemService.isMongoDB();
        boolean isWebCamera = systemService.isWebCamera();

        String userAgent = httpServletRequest.getHeader("user-agent");

        String systemProperties = System.getProperties().toString();
        log.info(systemProperties);

        model.addAttribute("isFFmpeg", isFFmpeg);
        model.addAttribute("isPTx", isPTx);
        model.addAttribute("isRecpt1", isRecpt1);
        model.addAttribute("isEpgdump", isEpgdump);
        model.addAttribute("isMongoDB", isMongoDB);
        model.addAttribute("isWebCamera", isWebCamera);
        model.addAttribute("systemProperties", systemProperties);

        return "system";
    }

}
