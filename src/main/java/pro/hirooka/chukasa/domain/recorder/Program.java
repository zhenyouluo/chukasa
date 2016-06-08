package pro.hirooka.chukasa.domain.recorder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.data.annotation.Id;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Program {
    @Id
    private String id;
    private String channel;
    private String title;
    private String detail;
    //    private List<Item> extdetail;
    private long start;
    private long end;
    private long duration;
//    private List<Category> category;
//    private List<?> attachinfo;
//    private Video video;
//    private List<Audio> audio;
    private boolean freeCA;
    private int eventID;

    private int ch;
    private String channelName;
    private String beginDate;
    private String endDate;
}
