package pro.hirooka.chukasa.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class ProgramInformation {
    @Id
    private long id;
    private int ch;
    private int genre;
    private long begin;
    private long end;
    private long start;
    private long stop;
    private long duration;
    private String title;
    private String summury;
}
