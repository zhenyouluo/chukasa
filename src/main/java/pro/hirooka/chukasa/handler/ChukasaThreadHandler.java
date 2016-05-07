package pro.hirooka.chukasa.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import pro.hirooka.chukasa.capture.CaptureRunner;
import pro.hirooka.chukasa.domain.ChukasaModel;
import pro.hirooka.chukasa.domain.type.StreamingType;
import pro.hirooka.chukasa.playlister.PlaylisterRunner;
import pro.hirooka.chukasa.segmenter.SegmenterRunner;
import pro.hirooka.chukasa.service.IChukasaModelManagementComponent;
import pro.hirooka.chukasa.transcoder.FFmpegRunner;

import static java.util.Objects.requireNonNull;

@Slf4j
public class ChukasaThreadHandler implements Runnable {

    private int adaptiveBitrateStreaming;

    private final IChukasaModelManagementComponent chukasaModelManagementComponent;

    @Autowired
    public ChukasaThreadHandler(int adaptiveBitrateStreaming, IChukasaModelManagementComponent chukasaModelManagementComponent){
        this.adaptiveBitrateStreaming = adaptiveBitrateStreaming;
        this.chukasaModelManagementComponent = requireNonNull(chukasaModelManagementComponent, "chukasaModelManagementComponent");
    }

    @Override
    public void run() {

        ChukasaModel chukasaModel = chukasaModelManagementComponent.get(adaptiveBitrateStreaming);

        if(chukasaModel.getChukasaSettings().getStreamingType() == StreamingType.WEB_CAMERA || chukasaModel.getChukasaSettings().getStreamingType() == StreamingType.FILE) {

            FFmpegRunner ffmpegRunner = new FFmpegRunner(adaptiveBitrateStreaming, chukasaModelManagementComponent);
            Thread fThread = new Thread(ffmpegRunner, "__FFmpegRunner__");
            fThread.start();

//            SegmenterRunner segmenterRunner = new SegmenterRunner(adaptiveBitrateStreaming, chukasaModelManagementComponent);
//            Thread sThread = new Thread(segmenterRunner, "__SegmenterRunner__");
//            sThread.start();

            PlaylisterRunner playlisterRunner = new PlaylisterRunner(adaptiveBitrateStreaming, chukasaModelManagementComponent);
            Thread pThread = new Thread(playlisterRunner, "__PlaylisterRunner__");
            pThread.start();
            chukasaModel.setPlaylisterRunner(playlisterRunner);
            chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);

        }else if(chukasaModel.getChukasaSettings().getStreamingType() == StreamingType.CAPTURE){

            CaptureRunner captureRunner = new CaptureRunner(adaptiveBitrateStreaming, chukasaModelManagementComponent);
            Thread cThread = new Thread(captureRunner, "__CaptureRunner__");
            cThread.start();

//            SegmenterRunner segmenterRunner = new SegmenterRunner(adaptiveBitrateStreaming, chukasaModelManagementComponent);
//            Thread sThread = new Thread(segmenterRunner, "__SegmenterRunner__");
//            sThread.start();

            PlaylisterRunner playlisterRunner = new PlaylisterRunner(adaptiveBitrateStreaming, chukasaModelManagementComponent);
            Thread pThread = new Thread(playlisterRunner, "__PlaylisterRunner__");
            pThread.start();
            chukasaModel.setPlaylisterRunner(playlisterRunner);
            chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);

        }else if(chukasaModel.getChukasaSettings().getStreamingType() == StreamingType.OKKAKE){

            SegmenterRunner segmenterRunner = new SegmenterRunner(adaptiveBitrateStreaming, chukasaModelManagementComponent);
            Thread sThread = new Thread(segmenterRunner, "__SegmenterRunner__");
            sThread.start();
            chukasaModel.setSegmenterRunner(segmenterRunner);
            chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);

            PlaylisterRunner playlisterRunner = new PlaylisterRunner(adaptiveBitrateStreaming, chukasaModelManagementComponent);
            Thread pThread = new Thread(playlisterRunner, "__PlaylisterRunner__");
            pThread.start();
            chukasaModel.setPlaylisterRunner(playlisterRunner);
            chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);

        }
    }
}
