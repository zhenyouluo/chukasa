package pro.hirooka.chukasa.domain.service.chukasa.transcoder;

import java.util.concurrent.Future;

public interface IIntermediateFFmpegAndRecxxxService {
    Future<Integer> submit(int adaptiveBitrateStreaming);
    void execute(int adaptiveBitrateStreaming);
    void cancel(int adaptiveBitrateStreaming);
}
