package pro.hirooka.chukasa.domain.service.chukasa.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.hirooka.chukasa.domain.model.chukasa.enums.StreamingType;
import pro.hirooka.chukasa.domain.service.chukasa.IChukasaModelManagementComponent;
import pro.hirooka.chukasa.domain.service.chukasa.detector.IFFmpegHLSMediaSegmentDetectorService;
import pro.hirooka.chukasa.domain.service.chukasa.remover.IIntermediateChukasaHLSFileRemoverService;
import pro.hirooka.chukasa.domain.service.chukasa.segmenter.IIntermediateChukasaHLSSegmenterService;
import pro.hirooka.chukasa.domain.service.chukasa.transcoder.IFFmpegAndRecxxxService;
import pro.hirooka.chukasa.domain.service.chukasa.transcoder.IFFmpegService;
import pro.hirooka.chukasa.domain.service.chukasa.transcoder.IFFmpegStopperService;

import java.util.Date;
import java.util.concurrent.Future;

import static java.util.Objects.requireNonNull;

@Slf4j
@Service
public class TaskCoordinatorService implements ITaskCoordinatorService {

    private final IChukasaModelManagementComponent chukasaModelManagementComponent;
    private final IFFmpegService ffmpegService;
    private final IFFmpegAndRecxxxService ffmpegAndRecxxxService;
    private final IFFmpegHLSMediaSegmentDetectorService ffmpegHLSMediaSegmentDetectorService;
    private final IFFmpegStopperService ffmpegStopperService;
    private final IIntermediateChukasaHLSFileRemoverService intermediateChukasaHLSFileRemoverService;
    private final IIntermediateChukasaHLSSegmenterService intermediateChukasaHLSSegmenterService;

    private Future<Integer> future;

    @Autowired
    public TaskCoordinatorService(
            IChukasaModelManagementComponent chukasaModelManagementComponent,
            IFFmpegService ffmpegService,
            IFFmpegAndRecxxxService ffmpegAndRecxxxService,
            IFFmpegHLSMediaSegmentDetectorService ffmpegHLSMediaSegmentDetectorService,
            IFFmpegStopperService ffmpegStopperService,
            IIntermediateChukasaHLSFileRemoverService intermediateChukasaHLSFileRemoverService,
            IIntermediateChukasaHLSSegmenterService intermediateChukasaHLSSegmenterService
    ) {
        this.chukasaModelManagementComponent = requireNonNull(
                chukasaModelManagementComponent, "chukasaModelManagementComponent");
        this.ffmpegService = requireNonNull(
                ffmpegService, "ffmpegService");
        this.ffmpegAndRecxxxService = requireNonNull(
                ffmpegAndRecxxxService, "ffmpegAndRecxxxService");
        this.ffmpegHLSMediaSegmentDetectorService = requireNonNull(
                ffmpegHLSMediaSegmentDetectorService, "ffmpegHLSMediaSegmentDetectorService");
        this.ffmpegStopperService = requireNonNull(
                ffmpegStopperService, "intermediateFFmpegStopperService");
        this.intermediateChukasaHLSFileRemoverService = requireNonNull(
                intermediateChukasaHLSFileRemoverService, "intermediateChukasaHLSFileRemoverService");
        this.intermediateChukasaHLSSegmenterService = requireNonNull(
                intermediateChukasaHLSSegmenterService, "intermediateChukasaHLSSegmenterService");
    }

    @Override
    public void execute() {

        chukasaModelManagementComponent.get().forEach(chukasaModel -> {
            final StreamingType streamingType = chukasaModel.getChukasaSettings().getStreamingType();
            final int adaptiveBitrateStreaming = chukasaModel.getAdaptiveBitrateStreaming();
            if(streamingType == StreamingType.WEBCAM
                    || chukasaModel.getChukasaSettings().getStreamingType() == StreamingType.FILE) {
                ffmpegHLSMediaSegmentDetectorService.schedule(adaptiveBitrateStreaming, new Date(), 2000);
                if(future != null){
                    future.cancel(true);
                }
                future = ffmpegService.submit(adaptiveBitrateStreaming);
            } else if(streamingType == StreamingType.TUNER) {
                ffmpegHLSMediaSegmentDetectorService.schedule(adaptiveBitrateStreaming, new Date(), 2000);
                if(future != null){
                    future.cancel(true);
                }
                future = ffmpegAndRecxxxService.submit(adaptiveBitrateStreaming);
            } else if(streamingType == StreamingType.OKKAKE) {
                intermediateChukasaHLSSegmenterService.schedule(adaptiveBitrateStreaming, new Date(), 2000);
            } else {
                //
            }
        });

    }

    @Override
    public void cancel() {

        chukasaModelManagementComponent.get().forEach(chukasaModel -> {
            final StreamingType streamingType = chukasaModel.getChukasaSettings().getStreamingType();
            final int adaptiveBitrateStreaming = chukasaModel.getAdaptiveBitrateStreaming();
            if(streamingType == StreamingType.WEBCAM
                    || chukasaModel.getChukasaSettings().getStreamingType() == StreamingType.FILE) {
                ffmpegHLSMediaSegmentDetectorService.cancel(adaptiveBitrateStreaming);
                ffmpegService.cancel(adaptiveBitrateStreaming);
                if(future != null){
                    future.cancel(true);
                }
            } else if(streamingType == StreamingType.TUNER) {
                ffmpegHLSMediaSegmentDetectorService.cancel(adaptiveBitrateStreaming);
                ffmpegAndRecxxxService.cancel(adaptiveBitrateStreaming);
                if(future != null){
                    future.cancel(true);
                }
            } else if(streamingType == StreamingType.OKKAKE) {
                intermediateChukasaHLSSegmenterService.cancel(adaptiveBitrateStreaming);
            } else {
                //
            }
        });
    }

    @Override
    public void stop() {
        chukasaModelManagementComponent.get().forEach(chukasaModel -> {
            final int adaptiveBitrateStreaming = chukasaModel.getAdaptiveBitrateStreaming();
            ffmpegStopperService.stop();
        });
    }

    @Override
    public void remove() {
        chukasaModelManagementComponent.get().forEach(chukasaModel -> {
            final String streamPath = chukasaModel.getStreamPath();
            intermediateChukasaHLSFileRemoverService.remove(streamPath);
        });
    }
}
