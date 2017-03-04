package pro.hirooka.chukasa.domain.service.epgdump.parser;

import pro.hirooka.chukasa.domain.model.recorder.ChannelPreferences;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface IEpgdumpParser {
    void parse(String path, int physicalLogicalChannel, int remoteControllerChannel) throws IOException;
    void parse(String path, int physicalChannel, Map<String, String> epgdumpChannelMap) throws IOException;
    void parse(String path, Map<String, Integer> epgdumpChannelMap) throws IOException;
}
