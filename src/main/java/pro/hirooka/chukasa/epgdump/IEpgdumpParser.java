package pro.hirooka.chukasa.epgdump;

import java.util.Map;

public interface IEpgdumpParser {
    void parse(String path, Map<String, Integer> epgdumpChannnelMap);
}
