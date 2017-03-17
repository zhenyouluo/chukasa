package pro.hirooka.chukasa.domain.service.chukasa.transcoder;

import java.util.concurrent.Future;

public interface IFFmpegService {
    Future<Integer> submit(int adaptiveBitrateStreaming);
    void cancel(int adaptiveBitrateStreaming);
}
