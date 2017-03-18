package pro.hirooka.chukasa.domain.service.chukasa.transcoder;

import java.util.concurrent.Future;

public interface IFFmpegStopperService {
    Future<Integer> stop();
}
