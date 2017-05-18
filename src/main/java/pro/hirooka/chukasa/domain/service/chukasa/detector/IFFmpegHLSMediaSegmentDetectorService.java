package pro.hirooka.chukasa.domain.service.chukasa.detector;

import java.util.Date;

public interface IFFmpegHLSMediaSegmentDetectorService {
    void schedule(int adaptiveBitrateStreaming, Date startTime, long period);
    void cancel(int adaptiveBitrateStreaming);
}
