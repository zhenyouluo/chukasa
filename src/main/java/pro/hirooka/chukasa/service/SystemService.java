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
        File pt2 = new File("/dev/pt1video0");
        File pt3 = new File("/dev/pt3video0");
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
        String[] command = {"/bin/sh", "-c", "ps aux | grep mongod"};
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        try {
            Process process = processBuilder.start();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String str = "";
            while((str = bufferedReader.readLine()) != null){
                log.info(str);
                if(str.startsWith("mongod")){
                    return true;
                }
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
