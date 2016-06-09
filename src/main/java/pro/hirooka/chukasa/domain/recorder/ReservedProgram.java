package pro.hirooka.chukasa.domain.recorder;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class ReservedProgram {
    @Id
    private int id;

    private String channel;
    private String title;
    private String detail;
    private long start;
    private long end;
    private long duration;
    private boolean freeCA;
    private int eventID;

    private int physicalChannel;
    private String channelName;
    private String beginDate;
    private String endDate;

    private long startRecording;
    private long stopRecording;
    private long durationRecording;
    private long recordingDuration;

    private long begin;
    private String fileName;
    RecordingStatus recordingStatus;
    MP4TranscodingStatus mp4TranscodingStatus;
}
