package pro.hirooka.chukasa.domain.service.chukasa.transcoder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.Future;

@Slf4j
@Service
public class IntermediateFFmpegStopperService implements IIntermediateFFmpegStopperService {

    private Future<Integer> future;

    @Autowired
    IFFmpegStopperService ffmpegStopperService;

    @Override
    public Future<Integer> submit(int adaptiveBitrateStreaming) {
        future = ffmpegStopperService.stop();
        return null;
    }

    @Override
    public void execute(int adaptiveBitrateStreaming) {
        ffmpegStopperService.stop();
    }
}
