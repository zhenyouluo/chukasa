package pro.hirooka.chukasa.domain.chukasa;

import lombok.Data;
import org.springframework.data.annotation.Id;
import pro.hirooka.chukasa.domain.chukasa.type.EncodingSettingsType;
import pro.hirooka.chukasa.domain.chukasa.type.PlaylistType;
import pro.hirooka.chukasa.domain.chukasa.type.StreamingType;

@Data
public class ChukasaSettings {

    @Id
    private int adaptiveBitrateStreaming;
    private StreamingType streamingType;
    private PlaylistType playlistType;
    private EncodingSettingsType encodingSettingsType;
    private boolean isEncrypted;
    private int ch;
    private String fileName;
    private int totalWebCameraLiveduration;

    private String videoResolution;
    private int videoBitrate;
    private int audioBitrate;
}
