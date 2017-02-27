package pro.hirooka.chukasa.domain.model.recorder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Channel {
    private String id;
    private int transportStreamId;
    private int originalNetworkId;
    private int serviceId;
    private String name;
    private List<Program> programs;
}
