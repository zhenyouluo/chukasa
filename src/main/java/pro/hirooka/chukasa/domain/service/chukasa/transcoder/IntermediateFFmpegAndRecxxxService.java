package pro.hirooka.chukasa.domain.service.chukasa.transcoder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.Future;

@Slf4j
@Service
public class IntermediateFFmpegAndRecxxxService implements IIntermediateFFmpegAndRecxxxService {

    private final IFFmpegAndRecxxxService ffmpegAndRecxxxService;

    private Future<Integer> future;

    @Autowired
    public IntermediateFFmpegAndRecxxxService(IFFmpegAndRecxxxService ffmpegAndRecxxxService) {
        this.ffmpegAndRecxxxService = ffmpegAndRecxxxService;
    }

    @Async
    @Override
    public Future<Integer> submit(int adaptiveBitrateStreaming) {
        if(future != null){
            future.cancel(true);
        }
        future = ffmpegAndRecxxxService.submit(adaptiveBitrateStreaming);
//        try {
//            int sequenceLastMediaSegment = future.get();
//            log.info("sequenceLastMediaSegment = {}", sequenceLastMediaSegment);
//            return new AsyncResult<>(sequenceLastMediaSegment);
//        } catch (InterruptedException | ExecutionException e) {
//            log.error("{}", e.getMessage());
//            return null;
//        }
        return null;
    }

    @Override
    public void execute(int adaptiveBitrateStreaming) {
        ffmpegAndRecxxxService.submit(adaptiveBitrateStreaming);
    }

    @Override
    public void cancel(int adaptiveBitrateStreaming) {
        if(future != null){
            future.cancel(true);
        }
    }
}
