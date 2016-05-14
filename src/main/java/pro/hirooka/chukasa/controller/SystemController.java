package pro.hirooka.chukasa.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import pro.hirooka.chukasa.service.chukasa.IChukasaModelManagementComponent;

import static java.util.Objects.requireNonNull;

@Slf4j
@Controller
@RequestMapping("system")
public class SystemController {

    private final IChukasaModelManagementComponent chukasaModelManagementComponent;

    @Autowired
    public SystemController(IChukasaModelManagementComponent chukasaModelManagementComponent){
        this.chukasaModelManagementComponent = requireNonNull(chukasaModelManagementComponent, "chukasaModelManagementComponent");
    }

}
