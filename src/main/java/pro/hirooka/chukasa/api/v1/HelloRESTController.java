package pro.hirooka.chukasa.api.v1;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import pro.hirooka.chukasa.domain.model.chukasa.ChukasaResponse;

@Slf4j
@RestController
@RequestMapping("api/v1")
public class HelloRESTController {

    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public ChukasaResponse hello(){
        return new ChukasaResponse("hello");
    }
}
