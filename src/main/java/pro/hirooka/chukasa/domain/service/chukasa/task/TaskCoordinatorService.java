package pro.hirooka.chukasa.domain.service.chukasa.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.hirooka.chukasa.domain.model.chukasa.enums.StreamingType;
import pro.hirooka.chukasa.domain.service.chukasa.IChukasaModelManagementComponent;
import pro.hirooka.chukasa.domain.service.chukasa.detector.IIntermediateFFmpegHLSMediaSegmentDetectorService;
import pro.hirooka.chukasa.domain.service.chukasa.remover.IIntermediateChukasaHLSFileRemoverService;
import pro.hirooka.chukasa.domain.service.chukasa.segmenter.IIntermediateChukasaHLSSegmenterService;
import pro.hirooka.chukasa.domain.service.chukasa.transcoder.IIntermediateFFmpegAndRecxxxService;
import pro.hirooka.chukasa.domain.service.chukasa.transcoder.IIntermediateFFmpegService;
import pro.hirooka.chukasa.domain.service.chukasa.transcoder.IIntermediateFFmpegStopperService;

import java.util.Date;

import static java.util.Objects.requireNonNull;

@Slf4j
@Service
public class TaskCoordinatorService implements ITaskCoordinatorService {

    private final IChukasaModelManagementComponent chukasaModelManagementComponent;
    private final IIntermediateFFmpegService intermediateFFmpegService;
    private final IIntermediateFFmpegAndRecxxxService intermediateFFmpegAndRecxxxService;
    private final IIntermediateFFmpegHLSMediaSegmentDetectorService intermediateFFmpegHLSMediaSegmentDetectorService;
    private final IIntermediateFFmpegStopperService intermediateFFmpegStopperService;
    private final IIntermediateChukasaHLSFileRemoverService intermediateChukasaHLSFileRemoverService;
    private final IIntermediateChukasaHLSSegmenterService intermediateChukasaHLSSegmenterService;

    @Autowired
    public TaskCoordinatorService(IChukasaModelManagementComponent chukasaModelManagementComponent, IIntermediateFFmpegService intermediateFFmpegService, IIntermediateFFmpegAndRecxxxService intermediateFFmpegAndRecxxxService, IIntermediateFFmpegHLSMediaSegmentDetectorService intermediateFFmpegHLSMediaSegmentDetectorService, IIntermediateFFmpegStopperService intermediateFFmpegStopperService, IIntermediateChukasaHLSFileRemoverService intermediateChukasaHLSFileRemoverService, IIntermediateChukasaHLSSegmenterService intermediateChukasaHLSSegmenterService) {
        this.chukasaModelManagementComponent = requireNonNull(chukasaModelManagementComponent, "chukasaModelManagementComponent");
        this.intermediateFFmpegService = requireNonNull(intermediateFFmpegService, "intermediateFFmpegService");
        this.intermediateFFmpegAndRecxxxService = requireNonNull(intermediateFFmpegAndRecxxxService, "intermediateFFmpegAndRecxxxService");
        this.intermediateFFmpegHLSMediaSegmentDetectorService = requireNonNull(intermediateFFmpegHLSMediaSegmentDetectorService, "intermediateFFmpegHLSMediaSegmentDetectorService");
        this.intermediateFFmpegStopperService = requireNonNull(intermediateFFmpegStopperService, "intermediateFFmpegStopperService");
        this.intermediateChukasaHLSFileRemoverService = requireNonNull(intermediateChukasaHLSFileRemoverService, "intermediateChukasaHLSFileRemoverService");
        this.intermediateChukasaHLSSegmenterService = requireNonNull(intermediateChukasaHLSSegmenterService, "intermediateChukasaHLSSegmenterService");
    }

    @Override
    public void execute() {

        chukasaModelManagementComponent.get().forEach(chukasaModel -> {
            final StreamingType streamingType = chukasaModel.getChukasaSettings().getStreamingType();
            final int adaptiveBitrateStreaming = chukasaModel.getAdaptiveBitrateStreaming();
            if(streamingType == StreamingType.WEBCAM
                    || chukasaModel.getChukasaSettings().getStreamingType() == StreamingType.FILE) {
                intermediateFFmpegHLSMediaSegmentDetectorService.schedule(adaptiveBitrateStreaming, new Date(), 2000);
                intermediateFFmpegService.execute(adaptiveBitrateStreaming);
            } else if(streamingType == StreamingType.TUNER) {
                intermediateFFmpegHLSMediaSegmentDetectorService.schedule(adaptiveBitrateStreaming, new Date(), 2000);
                intermediateFFmpegAndRecxxxService.execute(adaptiveBitrateStreaming);
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
                intermediateFFmpegHLSMediaSegmentDetectorService.cancel(adaptiveBitrateStreaming);
                intermediateFFmpegService.cancel(adaptiveBitrateStreaming);
            } else if(streamingType == StreamingType.TUNER) {
                intermediateFFmpegHLSMediaSegmentDetectorService.cancel(adaptiveBitrateStreaming);
                intermediateFFmpegAndRecxxxService.cancel(adaptiveBitrateStreaming);
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
            intermediateFFmpegStopperService.submit(adaptiveBitrateStreaming);
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
