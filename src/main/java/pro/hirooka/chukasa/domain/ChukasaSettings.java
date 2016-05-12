package pro.hirooka.chukasa.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import pro.hirooka.chukasa.domain.type.EncodingSettingsType;
import pro.hirooka.chukasa.domain.type.StreamingType;

@Data
public class ChukasaSettings {

    @Id
    private int adaptiveBitrateStreaming;

    private StreamingType streamingType;

    private EncodingSettingsType encodingSettingsType;

    private String videoResolution;
    private int videoBitrate;
//    private VideoResolutionType videoResolutionType;
//    private VideoProfileType videoProfileType;
//    private VideoResolutionType captureResolutionType;
    private int totalWebCameraLiveduration;

    private int audioBitrate;

    private boolean isEncrypted;

    private String fileName;

    private int ch;
}
