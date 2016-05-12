package pro.hirooka.chukasa.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.hirooka.chukasa.configuration.SystemConfiguration;

import java.io.*;

import static java.util.Objects.requireNonNull;

@Slf4j
@Component
public class SystemService implements ISystemService {

    private final String PT2_DEVICE = "/dev/pt1video0";
    private final String PT3_DEVICE = "/dev/pt3video0";
    private final String MONGOD = "mongod";

    private final SystemConfiguration systemConfiguration;

    @Autowired
    public SystemService(SystemConfiguration systemConfiguration){
        this.systemConfiguration = requireNonNull(systemConfiguration, "systemConfiguration");
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
        return false;
    }

    @Override
    public boolean isEPGDump() {
        File epgdump = new File(systemConfiguration.getEpgdumpPath());
        if(epgdump.exists()){
            return true;
        }
        return false;
    }

    @Override
    public boolean isMongoDB() {
        String[] command = {"/bin/sh", "-c", "ps aux | grep " + MONGOD}; // TODO: which mongod も？
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        try {
            Process process = processBuilder.start();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String str = "";
            while((str = bufferedReader.readLine()) != null){
                log.info(str);
                if(str.startsWith(MONGOD)){
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
}
