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

            PlaylisterRunner playlisterRunner = new PlaylisterRunner(adaptiveBitrateStreaming, chukasaModelManagementComponent);
            taskExecutor.execute(playlisterRunner);
            chukasaModel.setPlaylisterRunner(playlisterRunner);

            FFmpegRunner ffmpegRunner = new FFmpegRunner(adaptiveBitrateStreaming, chukasaModelManagementComponent);
            taskExecutor.execute(ffmpegRunner);

            if(chukasaModel.getChukasaSettings().isEncrypted()) {
                FFmpegHLSEncrypterRunner ffmpegHLSEncrypterRunner = new FFmpegHLSEncrypterRunner(adaptiveBitrateStreaming, chukasaModelManagementComponent);
                taskExecutor.execute(ffmpegHLSEncrypterRunner);
                chukasaModel.setFfmpegHLSEncrypterRunner(ffmpegHLSEncrypterRunner);
            }else{
                FFmpegHLSStreamDetectorRunner ffmpegHLSStreamDetectorRunner = new FFmpegHLSStreamDetectorRunner(adaptiveBitrateStreaming, chukasaModelManagementComponent);
                taskExecutor.execute(ffmpegHLSStreamDetectorRunner);
                chukasaModel.setFfmpegHLSStreamDetectorRunner(ffmpegHLSStreamDetectorRunner);
            }

            chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);

        }else if(chukasaModel.getChukasaSettings().getStreamingType().equals(StreamingType.CAPTURE)){

            PlaylisterRunner playlisterRunner = new PlaylisterRunner(adaptiveBitrateStreaming, chukasaModelManagementComponent);
            taskExecutor.execute(playlisterRunner);
            chukasaModel.setPlaylisterRunner(playlisterRunner);

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

            chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);

        }else if(chukasaModel.getChukasaSettings().getStreamingType().equals(StreamingType.OKKAKE)){

            PlaylisterRunner playlisterRunner = new PlaylisterRunner(adaptiveBitrateStreaming, chukasaModelManagementComponent);
            taskExecutor.execute(playlisterRunner);
            chukasaModel.setPlaylisterRunner(playlisterRunner);

            SegmenterRunner segmenterRunner = new SegmenterRunner(adaptiveBitrateStreaming, chukasaModelManagementComponent);
            taskExecutor.execute(segmenterRunner);
            chukasaModel.setSegmenterRunner(segmenterRunner);

            chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);
        }
    }
}
