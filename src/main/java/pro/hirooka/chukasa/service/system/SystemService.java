package pro.hirooka.chukasa.service.system;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.hirooka.chukasa.configuration.EpgdumpConfiguration;
import pro.hirooka.chukasa.configuration.MongoDBConfiguration;
import pro.hirooka.chukasa.configuration.SystemConfiguration;

import java.io.*;

@Slf4j
@Component
public class SystemService implements ISystemService {

    private final String PT2_DEVICE = "/dev/pt1video0";
    private final String PT3_DEVICE = "/dev/pt3video0";
    private final String MONGOD = "/bin/mongod";

    @Autowired
    SystemConfiguration systemConfiguration;
    @Autowired
    EpgdumpConfiguration epgdumpConfiguration;
    @Autowired
    MongoDBConfiguration mongoDBConfiguration;

    @Override
    public boolean isFFmpeg() {
        File ffmpeg = new File(systemConfiguration.getFfmpegPath());
        if(ffmpeg.exists()){
            return true;
        }
        return false;
    }

    @Override
    public boolean isWebCamera() {
        String webCameraDeviceName = systemConfiguration.getWebCameraDeviceName();
        File file = new File(webCameraDeviceName);
        return file.exists();
    }

    @Override
    public String getWebCameraDeviceName() {
        return systemConfiguration.getWebCameraDeviceName();
    }

    @Override
    public boolean isPTx() {
        File pt2 = new File(PT2_DEVICE);
        File pt3 = new File(PT3_DEVICE);
        if(pt2.exists() || pt3.exists()){
            return true;
        }
        // for PX-S1UD V2.0 (temporary)
        if(new File("/dev/dvb/adapter0/frontend0").exists()){
            return true;
        }
        return false;
    }

    @Override
    public boolean isRecpt1() {
        File recpt1 = new File(systemConfiguration.getRecpt1Path());
        if(recpt1.exists()){
            return true;
        }
        return false;
    }

    @Override
    public boolean isEpgdump() {
        File epgdump = new File(epgdumpConfiguration.getPath());
        if(epgdump.exists()){
            return true;
        }
        return false;
    }

    @Override
    public boolean isMongoDB() {
        if(mongoDBConfiguration.getHost().equals("mongo")){
            return true;
        }
        String[] command = {"/bin/sh", "-c", "ps aux | grep " + MONGOD};
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        try {
            Process process = processBuilder.start();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String str = "";
            while((str = bufferedReader.readLine()) != null){
                log.info(str);
                if(str.contains(MONGOD) && !str.contains("grep")){
                    bufferedReader.close();
                    process.destroy();
                    return true;
                }
            }
            bufferedReader.close();
            process.destroy();
        } catch (IOException e) {
            log.error("{} {}", e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean canWebCameraStreaming() {
        if(isFFmpeg() && isWebCamera()){
            return true;
        }
        return false;
    }

    @Override
    public boolean canFileStreaming() {
        if(isFFmpeg()){
            return true;
        }
        return false;
    }

    @Override
    public boolean canPTxStreaming() {
        if(isFFmpeg() && isPTx() && isRecpt1()){
            return true;
        }
        return false;
    }

    @Override
    public boolean canRecording() {
        if(isFFmpeg() && isPTx() && isRecpt1() && isEpgdump() && isMongoDB()){
            return true;
        }
        return false;
    }
}
