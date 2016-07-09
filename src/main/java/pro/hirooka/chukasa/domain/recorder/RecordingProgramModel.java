package pro.hirooka.chukasa.domain.recorder;

import lombok.Data;

@Data
public class RecordingProgramModel {
    private String fileName;
    private long startRecording;
    private long stopRecording;
}
