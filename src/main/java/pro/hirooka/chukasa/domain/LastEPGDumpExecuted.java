package pro.hirooka.chukasa.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class LastEPGDumpExecuted {
    @Id
    private int unique;
    private long date;
}
