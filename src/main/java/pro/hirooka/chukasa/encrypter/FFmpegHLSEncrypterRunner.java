package pro.hirooka.chukasa.encrypter;


import lombok.extern.slf4j.Slf4j;
import pro.hirooka.chukasa.domain.chukasa.ChukasaModel;
import pro.hirooka.chukasa.service.chukasa.IChukasaModelManagementComponent;

import java.util.Timer;

import static java.util.Objects.requireNonNull;

@Slf4j
public class FFmpegHLSEncrypterRunner implements Runnable {

    private int adaptiveBitrateStreaming;
    private boolean isRunning = true;

    private IChukasaModelManagementComponent chukasaModelManagementComponent;

    public FFmpegHLSEncrypterRunner(int adaptiveBitrateStreaming, IChukasaModelManagementComponent chukasaModelManagementComponent){
        this.adaptiveBitrateStreaming = adaptiveBitrateStreaming;
        this.chukasaModelManagementComponent = requireNonNull(chukasaModelManagementComponent, "chukasaModelManagementComponent");
    }

    @Override
    public void run() {

        ChukasaModel chukasaModel = chukasaModelManagementComponent.get(adaptiveBitrateStreaming);

        Timer segmenterTimer = new Timer();
        segmenterTimer.scheduleAtFixedRate(new FFmpegHLSEncrypter(adaptiveBitrateStreaming, chukasaModelManagementComponent), 0, chukasaModel.getHlsConfiguration().getDuration() * 1000);

        while(isRunning){
            chukasaModel = chukasaModelManagementComponent.get(adaptiveBitrateStreaming);
            if(chukasaModel == null || chukasaModel.isFlagTimerSegmenter()){
                segmenterTimer.cancel();
                segmenterTimer = null;
                log.info("{} is completed.", this.getClass().getName());
                break;
            }
        }
        if(!isRunning){
            if(segmenterTimer != null) {
                segmenterTimer.cancel();
            }
        }
    }

    public void stop(){
        isRunning = false;
    }
}
