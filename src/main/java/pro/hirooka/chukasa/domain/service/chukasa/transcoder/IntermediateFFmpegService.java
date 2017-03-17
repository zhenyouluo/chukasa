package pro.hirooka.chukasa.domain.service.chukasa.transcoder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Slf4j
@Service
public class IntermediateFFmpegService implements IIntermediateFFmpegService {

    private final IFFmpegService ffmpegService;

    private Future<Integer> future;

    @Autowired
    public IntermediateFFmpegService(IFFmpegService ffmpegService) {
        this.ffmpegService = ffmpegService;
    }

    @Async
    @Override
    public Future<Integer> submit(int adaptiveBitrateStreaming) {
        if(future != null){
            future.cancel(true);
        }
        future = ffmpegService.submit(adaptiveBitrateStreaming);
        try {
            int sequenceLastMediaSegment = future.get();
            log.info("sequenceLastMediaSegment = {}", sequenceLastMediaSegment);
            return new AsyncResult<>(sequenceLastMediaSegment);
        } catch (InterruptedException | ExecutionException e) {
            log.error("{}", e.getMessage());
            return null;
        }
    }

    @Override
    public void execute(int adaptiveBitrateStreaming) {
        ffmpegService.submit(adaptiveBitrateStreaming);
    }

    @Override
    public void cancel(int adaptiveBitrateStreaming) {
        if(future != null){
            future.cancel(true);
        }
    }
}
