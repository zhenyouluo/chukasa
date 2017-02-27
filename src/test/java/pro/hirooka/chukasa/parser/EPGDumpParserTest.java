package pro.hirooka.chukasa.parser;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import pro.hirooka.chukasa.domain.service.epgdump.parser.EpgdumpParser;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EPGDumpParserTest {

    @Autowired
    EpgdumpParser epgDumpParser;

    @Ignore
    @Test
    public void test(){
        //epgDumpParser.parse("/tmp/epgdump.json");
    }
}
