package pro.hirooka.chukasa.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.hirooka.chukasa.configuration.SystemConfiguration;

import java.io.File;

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
}
