package pro.hirooka.chukasa.service;

public interface ISystemService {
    boolean isWebCamera();
    String getWebCameraDeviceName();
    boolean isPTx();
    boolean isEPGDump();
    boolean isMongoDB();
}
