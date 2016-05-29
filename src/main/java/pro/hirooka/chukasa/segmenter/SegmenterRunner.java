package pro.hirooka.chukasa.segmenter;

import lombok.extern.slf4j.Slf4j;
import pro.hirooka.chukasa.domain.chukasa.ChukasaModel;
import pro.hirooka.chukasa.service.chukasa.IChukasaModelManagementComponent;

import java.util.Timer;

import static java.util.Objects.requireNonNull;

@Slf4j
public class SegmenterRunner implements Runnable {

    private int adaptiveBitrateStreaming;
    private boolean isRunning = true;

    private IChukasaModelManagementComponent chukasaModelManagementComponent;

    public SegmenterRunner(int adaptiveBitrateStreaming, IChukasaModelManagementComponent chukasaModelManagementComponent){
        this.adaptiveBitrateStreaming = adaptiveBitrateStreaming;
        this.chukasaModelManagementComponent = requireNonNull(chukasaModelManagementComponent, "chukasaModelManagementComponent");
    }

    @Override
    public void run() {

        ChukasaModel chukasaModel = chukasaModelManagementComponent.get(adaptiveBitrateStreaming);

        Timer segmenterTimer = new Timer();
        segmenterTimer.scheduleAtFixedRate(new Segmenter(adaptiveBitrateStreaming, chukasaModelManagementComponent), chukasaModel.getTimerSegmenterDelay(), chukasaModel.getTimerSegmenterPeriod());

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
            segmenterTimer.cancel();
        }
    }

    public void stop(){
        isRunning = false;
    }
}
