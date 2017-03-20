package pro.hirooka.chukasa.domain.model.chukasa;

import lombok.Data;
import org.springframework.data.annotation.Id;
import pro.hirooka.chukasa.domain.model.chukasa.enums.TranscodingSettings;
import pro.hirooka.chukasa.domain.model.chukasa.enums.PlaylistType;
import pro.hirooka.chukasa.domain.model.chukasa.enums.StreamingType;

@Data
public class ChukasaSettings {

    @Id
    private int adaptiveBitrateStreaming;
    private StreamingType streamingType;
    private PlaylistType playlistType;
    private TranscodingSettings transcodingSettings;
    private boolean canEncrypt;
    private int physicalLogicalChannel;
    private String fileName;

    private String videoResolution;
    private int videoBitrate;
    private int audioBitrate;
}
