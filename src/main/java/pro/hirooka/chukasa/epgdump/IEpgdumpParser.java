package pro.hirooka.chukasa.epgdump;

import java.io.IOException;
import java.util.Map;

public interface IEpgdumpParser {
    void parse(String path, int physicalChannel, Map<String, String> epgdumpChannelMap) throws IOException;
    void parse(String path, Map<String, Integer> epgdumpChannelMap) throws IOException;
}
