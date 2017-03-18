package pro.hirooka.chukasa.domain.service.chukasa.transcoder;

import java.util.concurrent.Future;

public interface IIntermediateFFmpegStopperService {
    Future<Integer> submit(int adaptiveBitrateStreaming);
    void execute(int adaptiveBitrateStreaming);
}
