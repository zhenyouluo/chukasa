package pro.hirooka.chukasa.domain.service.chukasa.detector;

import java.util.Date;

public interface IIntermediateFFmpegHLSMediaSegmentDetectorService {
    void schedule(int adaptiveBitrateStreaming, Date startTime, long period);
    void cancel(int adaptiveBitrateStreaming);
}
