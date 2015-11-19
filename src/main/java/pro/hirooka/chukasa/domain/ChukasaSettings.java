package pro.hirooka.chukasa.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import pro.hirooka.chukasa.domain.type.StreamingType;
import pro.hirooka.chukasa.domain.type.VideoProfileType;
import pro.hirooka.chukasa.domain.type.VideoResolutionType;

@Data
public class ChukasaSettings {

    @Id
    private int adaptiveBitrateStreaming;

    private StreamingType streamingType;

    private int videoBitrate;
    private VideoResolutionType videoResolutionType;
    private VideoProfileType videoProfileType;
    private VideoResolutionType captureResolutionType;
    private int totalUSBCameraLiveduration;

    private int audioBitrate;

    private boolean isEncrypted;

    private String fileName;

    private int ch;
}
