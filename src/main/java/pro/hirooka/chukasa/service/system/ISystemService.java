package pro.hirooka.chukasa.service.system;

import pro.hirooka.chukasa.domain.chukasa.type.VideoCodecType;

public interface ISystemService {
    boolean isFFmpeg();
    boolean isWebCamera();
    String getWebCameraDeviceName();
    boolean isPTx();
    boolean isRecpt1();
    boolean isEpgdump();
    boolean isMongoDB();
    boolean canWebCameraStreaming();
    boolean canFileStreaming();
    boolean canPTxStreaming();
    boolean canRecording();
    VideoCodecType getVideoCodecType();
}
