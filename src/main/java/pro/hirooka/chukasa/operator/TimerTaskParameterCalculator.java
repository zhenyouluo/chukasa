package pro.hirooka.chukasa.operator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.hirooka.chukasa.domain.chukasa.ChukasaModel;
import pro.hirooka.chukasa.domain.chukasa.type.StreamingType;
import pro.hirooka.chukasa.service.chukasa.IChukasaModelManagementComponent;

import static java.util.Objects.requireNonNull;

@Slf4j
@Component
public class TimerTaskParameterCalculator implements ITimerTaskParameterCalculator {

    private final IChukasaModelManagementComponent chukasaModelManagementComponent;

    @Autowired
    public TimerTaskParameterCalculator(IChukasaModelManagementComponent chukasaModelManagementComponent) {
        this.chukasaModelManagementComponent = requireNonNull(chukasaModelManagementComponent, "chukasaModelManagementComponent");
    }

    @Override
    public void calculate(int adaptiveBitrateStreaming) {

        ChukasaModel chukasaModel = chukasaModelManagementComponent.get(adaptiveBitrateStreaming);

        // segmenter timer parameters [ms]
        // TODO optimize
        int duration = chukasaModel.getHlsConfiguration().getDuration();
        int uriInPlaylist = chukasaModel.getHlsConfiguration().getUriInPlaylist();
        long timerSegmenterDelay = (long) (duration * 1000 * (uriInPlaylist - 1));

        if (chukasaModel.getChukasaSettings().getStreamingType() == StreamingType.WEB_CAMERA) {
            timerSegmenterDelay = (long) (duration * 1000 * uriInPlaylist);
        }

        if (chukasaModel.getChukasaSettings().getStreamingType() == StreamingType.FILE) {
            //timerSegmenterDelay = (long)(DURATION * 1000 * (URI_IN_PLAYLIST));
            //timerSegmenterDelay = 0;
            timerSegmenterDelay = (long) (duration * 1000);
        }

        if (chukasaModel.getChukasaSettings().getStreamingType() == StreamingType.CAPTURE) {
            timerSegmenterDelay = (long) (duration * 1000 * (uriInPlaylist)) + 1000;
            timerSegmenterDelay = (long) (duration * 1000);
            // timerSegmenterDelay = 0;
        }

        if(chukasaModel.getSystemConfiguration().isOpenmaxEnabled()){
            timerSegmenterDelay = timerSegmenterDelay + 3000; // todo
        }
        long timerSegmenterPeriod = (long) (duration * 1000);

        // playlister timer parameters [ms]
        long timerPlaylisterDelay = timerSegmenterDelay + (Math.round(duration) * 1000 * uriInPlaylist + 1000);
        timerPlaylisterDelay = 0;

        long timerPlaylisterPeriod = (long) (duration * 1000);

        log.info("timerSegmenterDelay = {}, timerSegmenterPeriod = {}, timerPlaylisterDelay = {}, timerPlaylisterPeriod = {}", timerSegmenterDelay, timerSegmenterPeriod, timerPlaylisterDelay, timerPlaylisterPeriod);

        chukasaModel.setTimerSegmenterDelay(timerSegmenterDelay);
        chukasaModel.setTimerSegmenterPeriod(timerSegmenterPeriod);
        chukasaModel.setTimerPlaylisterDelay(timerPlaylisterDelay);
        chukasaModel.setTimerPlaylisterPeriod(timerPlaylisterPeriod);

        chukasaModelManagementComponent.update(0, chukasaModel);
    }
}
