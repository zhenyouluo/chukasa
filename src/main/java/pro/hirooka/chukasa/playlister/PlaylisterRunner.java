package pro.hirooka.chukasa.playlister;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import pro.hirooka.chukasa.domain.ChukasaModel;
import pro.hirooka.chukasa.service.IChukasaModelManagementComponent;

import java.util.Timer;

import static java.util.Objects.requireNonNull;

@Slf4j
public class PlaylisterRunner implements Runnable {

    private int adaptiveBitrateStreaming;

    private final IChukasaModelManagementComponent chukasaModelManagementComponent;

    private boolean isRunning = true;

    @Autowired
    public PlaylisterRunner(int adaptiveBitrateStreaming, IChukasaModelManagementComponent chukasaModelManagementComponent) {
        this.adaptiveBitrateStreaming = adaptiveBitrateStreaming;
        this.chukasaModelManagementComponent = requireNonNull(chukasaModelManagementComponent, "chukasaModelManagementComponent");
    }

    @Override
    public void run() {

        ChukasaModel chukasaModel = chukasaModelManagementComponent.get(adaptiveBitrateStreaming);

        Timer playlisterTimer = new Timer();
        playlisterTimer.scheduleAtFixedRate(new Playlister(adaptiveBitrateStreaming, chukasaModelManagementComponent), chukasaModel.getTimerPlaylisterDelay(), chukasaModel.getTimerPlaylisterPeriod());

        while(isRunning){
            chukasaModel = chukasaModelManagementComponent.get(adaptiveBitrateStreaming);
            if(chukasaModel == null || chukasaModel.isFlagTimerPlaylister()){
                playlisterTimer.cancel();
                playlisterTimer = null;
                log.info("{} is completed.", this.getClass().getName());
                break;
            }
        }
        if(!isRunning){
            playlisterTimer.cancel();
        }

        if(isRunning) {
            chukasaModelManagementComponent.deleteAll();
            log.info("all ChukasaModels have been deleted because streming was finished.");
        }
    }

    public void stop(){
        isRunning = false;
    }
}
