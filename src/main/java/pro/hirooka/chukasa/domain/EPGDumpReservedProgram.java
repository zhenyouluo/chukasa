package pro.hirooka.chukasa.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class EPGDumpReservedProgram {
    // TODO: fix
    @Id
    private int id;
    private int ch;
    private int genre;
    private String beginDate;
    private String endDate;
    private long begin;
    private long end;
    private long start;
    private long stop;
    private long duration;
    private String title;
    private String summary;
}
