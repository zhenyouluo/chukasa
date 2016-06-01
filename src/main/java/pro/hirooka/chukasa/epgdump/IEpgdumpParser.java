package pro.hirooka.chukasa.epgdump;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

public interface IEpgdumpParser {
    void parse(String path, Map<String, Integer> epgdumpChannnelMap) throws IOException;
}
