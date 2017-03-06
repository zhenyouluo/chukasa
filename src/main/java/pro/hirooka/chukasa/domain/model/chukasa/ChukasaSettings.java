package pro.hirooka.chukasa.domain.model.chukasa;

import lombok.Data;
import org.springframework.data.annotation.Id;
import pro.hirooka.chukasa.domain.model.chukasa.enums.TranscodingEncodingPreferencesType;
import pro.hirooka.chukasa.domain.model.chukasa.enums.PlaylistType;
import pro.hirooka.chukasa.domain.model.chukasa.enums.StreamingType;

@Data
public class ChukasaSettings {

    @Id
    private int adaptiveBitrateStreaming;
    private StreamingType streamingType;
    private PlaylistType playlistType;
    private TranscodingEncodingPreferencesType encodingSettingsType;
    private boolean isEncrypted;
    private int ch;
    private String fileName;
    private int totalWebCameraLiveduration;

    private String videoResolution;
    private int videoBitrate;
    private int audioBitrate;
}
