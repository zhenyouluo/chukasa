package pro.hirooka.chukasa.domain.service.chukasa;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.hirooka.chukasa.domain.configuration.EpgdumpConfiguration;
import pro.hirooka.chukasa.domain.configuration.MongoDBConfiguration;
import pro.hirooka.chukasa.domain.configuration.SystemConfiguration;
import pro.hirooka.chukasa.domain.model.chukasa.constants.ChukasaConstant;
import pro.hirooka.chukasa.domain.model.chukasa.enums.HardwareAccelerationType;

import java.io.*;

@Slf4j
@Service
public class SystemService implements ISystemService {

    private final String DVB_DEVICE = ChukasaConstant.DVB_DEVICE + "0"; // TODO:
    private final String CHARACTER_DEVICE = ChukasaConstant.CHARACTER_DEVICE + "0";;

    @Autowired
    SystemConfiguration systemConfiguration;
    @Autowired
    EpgdumpConfiguration epgdumpConfiguration;
    @Autowired
    MongoDBConfiguration mongoDBConfiguration;

    @Override
    public boolean isFFmpeg() {
        File ffmpeg = new File(systemConfiguration.getFfmpegPath());
        return ffmpeg.exists();
    }

    @Override
    public boolean isWebCamera() {
        String webCameraDeviceName = systemConfiguration.getWebcamDeviceName();
        File file = new File(webCameraDeviceName);
        return file.exists();
    }

    @Override
    public String getWebCameraDeviceName() {
        return systemConfiguration.getWebcamDeviceName();
    }

    @Override
    public boolean isTuner() {
        return new File(DVB_DEVICE).exists() || new File(CHARACTER_DEVICE).exists();
    }

    @Override
    public boolean isRecxxx() {
        String recxxxPath = systemConfiguration.getRecxxxPath().split(" ")[0];
        File recpt1 = new File(recxxxPath);
        return recpt1.exists();
    }

    @Override
    public boolean isEpgdump() {
        File epgdump = new File(epgdumpConfiguration.getPath());
        return epgdump.exists();
    }

    @Override
    public boolean isMongoDB() {
        if(mongoDBConfiguration.getHost().equals("mongo")){
            return true;
        }
        ServerAddress serverAddress = new ServerAddress(mongoDBConfiguration.getHost(), mongoDBConfiguration.getPort());
        MongoClientOptions mongoClientOptions = MongoClientOptions.builder().serverSelectionTimeout(mongoDBConfiguration.getServerSelectionTimeout()).build();
        MongoClient mongoClient = new MongoClient(serverAddress, mongoClientOptions);
        try {
            mongoClient.getServerAddressList();
            //mongoClient.getDatabase("admin").runCommand(new Document("ping", 1));
            mongoClient.close();
            log.info("MongoDB is running.");
            return true;
        } catch (Exception e) {
            log.info("MongoDB is down or not installed.");
            mongoClient.close();
            return false;
        }
    }

    @Override
    public boolean canWebCameraStreaming() {
        return isFFmpeg() && isWebCamera();
    }

    @Override
    public boolean canFileStreaming() {
        return isFFmpeg();
    }

    @Override
    public boolean canPTxStreaming() {
        return isFFmpeg() && isTuner() && isRecxxx();
    }

    @Override
    public boolean canRecording() {
        return isFFmpeg() && isTuner() && isRecxxx() && isEpgdump() && isMongoDB();
    }

    @Override
    public HardwareAccelerationType getHardwareAccelerationType() {
        final String H264_QSV = "--enable-libmfx";
        final String H264_X264 = "--enable-libx264";
        final String H264_OMX = "--enable-omx-rpi";
        String ffmpeg = systemConfiguration.getFfmpegPath();
        String[] command = {ffmpeg, "-version"};
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        try {
            Process process = processBuilder.start();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String str = "";
            while((str = bufferedReader.readLine()) != null){
                log.info(str);
                if(str.contains(H264_QSV)){
                    bufferedReader.close();
                    process.destroy();
                    return HardwareAccelerationType.H264_QSV;
                }
                if(str.contains(H264_OMX)){
                    bufferedReader.close();
                    process.destroy();
                    return HardwareAccelerationType.H264_OMX;
                }
                if(str.contains(H264_X264)){
                    bufferedReader.close();
                    process.destroy();
                    return HardwareAccelerationType.H264_X264;
                }
            }
            bufferedReader.close();
            process.destroy();
        } catch (IOException e) {
            log.error("{} {}", e.getMessage(), e);
        }
        return HardwareAccelerationType.H264_NVENC;
    }

}
