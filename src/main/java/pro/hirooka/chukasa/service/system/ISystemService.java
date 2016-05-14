package pro.hirooka.chukasa.service.system;

public interface ISystemService {
    boolean isFFmpeg();
    boolean isWebCamera();
    String getWebCameraDeviceName();
    boolean isPTx();
    boolean isRecpt1();
    boolean isEPGDump();
    boolean isMongoDB();
}
