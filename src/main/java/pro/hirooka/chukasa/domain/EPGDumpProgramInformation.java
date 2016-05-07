package pro.hirooka.chukasa.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class EPGDumpProgramInformation {
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
}
