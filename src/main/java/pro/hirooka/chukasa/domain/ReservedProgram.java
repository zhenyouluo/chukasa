package pro.hirooka.chukasa.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class ReservedProgram {
    @Id
    private int id;
    @NotNull
    private int ch;
    private int genre;
    @NotNull
    @Size(min = 12, max = 12)
    private String beginDate;
    @NotNull
    @Size(min = 12, max = 12)
    private String endDate;
    private long begin;
    private long end;
    private long start;
    private long stop;
    private long duration;
    @NotNull
//    @Size(min = 0, max = 32)
    private String title;
//    @Size(min = 0, max = 256)
    private String summary;
}
