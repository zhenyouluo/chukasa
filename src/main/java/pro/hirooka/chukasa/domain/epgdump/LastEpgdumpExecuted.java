package pro.hirooka.chukasa.domain.epgdump;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class LastEpgdumpExecuted {
    @Id
    private int unique;
    private long date;
}
