package pro.hirooka.chukasa.domain.service.chukasa.stopper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.hirooka.chukasa.domain.model.chukasa.ChukasaModel;
import pro.hirooka.chukasa.domain.model.chukasa.enums.StreamingType;
import pro.hirooka.chukasa.domain.service.chukasa.IChukasaModelManagementComponent;
import pro.hirooka.chukasa.domain.service.chukasa.transcoder.FFmpegStopper;

import java.util.List;

@Deprecated
@Slf4j
@Component
public class ChukasaStopper {

    @Autowired
    IChukasaModelManagementComponent chukasaModelManagementComponent;

    public void stop(){

        log.info("stop command is called.");

        List<ChukasaModel> chukasaModelList = chukasaModelManagementComponent.get();

        // set flag timer cancel (segmenter, playlister)
        // if set flag true -> break while loop
        for(ChukasaModel chukasaModel : chukasaModelList){
            chukasaModel.setFlagTimerSegmenter(true);
            chukasaModel.setFlagTimerFFmpegHLSSegmenter(true);
            chukasaModel.setFlagTimerPlaylister(true);
            if(chukasaModel.getChukasaSettings().getStreamingType() == StreamingType.OKKAKE){
                chukasaModel.setFlagRemoveFile(true);
            }
            chukasaModelManagementComponent.update(chukasaModel.getAdaptiveBitrateStreaming(), chukasaModel);
        }

        FFmpegStopper ffmpegStopper = new FFmpegStopper(chukasaModelManagementComponent);
        Thread thread = new Thread(ffmpegStopper);
        thread.start();
    }
}
