package pro.hirooka.chukasa.domain.service.chukasa.segmenter;

import java.util.Date;

public interface IIntermediateChukasaHLSSegmenterService {
    void schedule(int adaptiveBitrateStreaming, Date startTime, long period);
    void cancel(int adaptiveBitrateStreaming);
}
