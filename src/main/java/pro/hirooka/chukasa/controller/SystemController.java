package pro.hirooka.chukasa.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import pro.hirooka.chukasa.configuration.SystemConfiguration;
import pro.hirooka.chukasa.service.chukasa.IChukasaModelManagementComponent;
import pro.hirooka.chukasa.service.system.SystemService;

import javax.servlet.http.HttpServletRequest;

import static java.util.Objects.requireNonNull;

@Slf4j
@Controller
@RequestMapping("system")
public class SystemController {

    private final IChukasaModelManagementComponent chukasaModelManagementComponent;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private SystemService systemService;

    @Autowired
    public SystemController(IChukasaModelManagementComponent chukasaModelManagementComponent){
        this.chukasaModelManagementComponent = requireNonNull(chukasaModelManagementComponent, "chukasaModelManagementComponent");
    }

    @RequestMapping("/")
    String index(Model model){

        boolean isFFmpeg = systemService.isFFmpeg();
        boolean isPTx = systemService.isPTx();
        boolean isRecpt1 = systemService.isRecpt1();
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
