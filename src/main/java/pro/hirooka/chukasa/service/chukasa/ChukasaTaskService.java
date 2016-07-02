package pro.hirooka.chukasa.service.chukasa;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.stereotype.Service;
import pro.hirooka.chukasa.capture.CaptureRunner;
import pro.hirooka.chukasa.detector.FFmpegHLSStreamDetectorRunner;
import pro.hirooka.chukasa.domain.chukasa.ChukasaModel;
import pro.hirooka.chukasa.domain.chukasa.type.StreamingType;
import pro.hirooka.chukasa.encrypter.FFmpegHLSEncrypterRunner;
import pro.hirooka.chukasa.playlister.PlaylisterRunner;
import pro.hirooka.chukasa.segmenter.SegmenterRunner;
import pro.hirooka.chukasa.transcoder.FFmpegRunner;

@Slf4j
@Service
public class ChukasaTaskService implements IChukasaTaskService {

    @Autowired
    IChukasaModelManagementComponent chukasaModelManagementComponent;

    @Override
    public void execute(int adaptiveBitrateStreaming) {

        SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();

        ChukasaModel chukasaModel = chukasaModelManagementComponent.get(adaptiveBitrateStreaming);

        if(chukasaModel.getChukasaSettings().getStreamingType().equals(StreamingType.WEB_CAMERA) || chukasaModel.getChukasaSettings().getStreamingType().equals(StreamingType.FILE)) {

            FFmpegRunner ffmpegRunner = new FFmpegRunner(adaptiveBitrateStreaming, chukasaModelManagementComponent);
            taskExecutor.execute(ffmpegRunner);
//            Thread fThread = new Thread(ffmpegRunner, "__FFmpegRunner__");
//            fThread.start();

//            SegmenterRunner segmenterRunner = new SegmenterRunner(adaptiveBitrateStreaming, chukasaModelManagementComponent);
//            Thread sThread = new Thread(segmenterRunner, "__SegmenterRunner__");
//            sThread.start();

            PlaylisterRunner playlisterRunner = new PlaylisterRunner(adaptiveBitrateStreaming, chukasaModelManagementComponent);
            taskExecutor.execute(playlisterRunner);
//            Thread pThread = new Thread(playlisterRunner, "__PlaylisterRunner__");
//            pThread.start();
            chukasaModel.setPlaylisterRunner(playlisterRunner);
            chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);

        }else if(chukasaModel.getChukasaSettings().getStreamingType().equals(StreamingType.CAPTURE)){

            CaptureRunner captureRunner = new CaptureRunner(adaptiveBitrateStreaming, chukasaModelManagementComponent);
            taskExecutor.execute(captureRunner);

            if(chukasaModel.getChukasaSettings().isEncrypted()) {
                FFmpegHLSEncrypterRunner ffmpegHLSEncrypterRunner = new FFmpegHLSEncrypterRunner(adaptiveBitrateStreaming, chukasaModelManagementComponent);
                taskExecutor.execute(ffmpegHLSEncrypterRunner);
                chukasaModel.setFfmpegHLSEncrypterRunner(ffmpegHLSEncrypterRunner);
            }else{
                FFmpegHLSStreamDetectorRunner ffmpegHLSStreamDetectorRunner = new FFmpegHLSStreamDetectorRunner(adaptiveBitrateStreaming, chukasaModelManagementComponent);
                taskExecutor.execute(ffmpegHLSStreamDetectorRunner);
                chukasaModel.setFfmpegHLSStreamDetectorRunner(ffmpegHLSStreamDetectorRunner);
            }

            PlaylisterRunner playlisterRunner = new PlaylisterRunner(adaptiveBitrateStreaming, chukasaModelManagementComponent);
            taskExecutor.execute(playlisterRunner);

            chukasaModel.setPlaylisterRunner(playlisterRunner);
            chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);

        }else if(chukasaModel.getChukasaSettings().getStreamingType() == StreamingType.OKKAKE){

            SegmenterRunner segmenterRunner = new SegmenterRunner(adaptiveBitrateStreaming, chukasaModelManagementComponent);
            taskExecutor.execute(segmenterRunner);
//            Thread sThread = new Thread(segmenterRunner, "__SegmenterRunner__");
//            sThread.start();
            chukasaModel.setSegmenterRunner(segmenterRunner);
            chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);

            PlaylisterRunner playlisterRunner = new PlaylisterRunner(adaptiveBitrateStreaming, chukasaModelManagementComponent);
            taskExecutor.execute(playlisterRunner);
//            Thread pThread = new Thread(playlisterRunner, "__PlaylisterRunner__");
//            pThread.start();
            chukasaModel.setPlaylisterRunner(playlisterRunner);
            chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);

        }
    }
}
