package pro.hirooka.chukasa.epgdump;

import java.util.Map;

public interface IEPGDumpParser {
    void parse(String path, Map<String, Integer> epgDumpChannnelMap);
}
