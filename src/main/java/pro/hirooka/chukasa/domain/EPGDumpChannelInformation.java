package pro.hirooka.chukasa.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class EPGDumpChannelInformation {
    private String id;
    private int transportStreamId;
    private int originalNetworkId;
    private int serviceId;
    private String name;
    private List<EPGDumpProgramInformation> programs;
}
