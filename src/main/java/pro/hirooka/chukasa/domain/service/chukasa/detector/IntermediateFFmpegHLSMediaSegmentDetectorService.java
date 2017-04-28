package pro.hirooka.chukasa.domain.service.chukasa.detector;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import pro.hirooka.chukasa.domain.event.LastMediaSegmentSequenceEvent;

import java.util.Date;

import static java.util.Objects.requireNonNull;

@Slf4j
@Service
public class IntermediateFFmpegHLSMediaSegmentDetectorService implements IIntermediateFFmpegHLSMediaSegmentDetectorService, ApplicationListener<LastMediaSegmentSequenceEvent> {

    private final FFmpegHLSMediaSegmentDetector ffmpegHLSMediaSegmentDetector;
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;

    @Autowired
    public IntermediateFFmpegHLSMediaSegmentDetectorService(FFmpegHLSMediaSegmentDetector ffmpegHLSMediaSegmentDetector) {
        this.ffmpegHLSMediaSegmentDetector = requireNonNull(ffmpegHLSMediaSegmentDetector, "ffmpegHLSMediaSegmentDetector");
    }

    @Async
    @Override
    public void schedule(int adaptiveBitrateStreaming, Date startTime, long period) {
        ffmpegHLSMediaSegmentDetector.setAdaptiveBitrateStreaming(adaptiveBitrateStreaming);
        threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setThreadNamePrefix(Integer.toString(adaptiveBitrateStreaming));
        //threadPoolTaskScheduler.setPoolSize(3);
        threadPoolTaskScheduler.initialize();
        threadPoolTaskScheduler.scheduleAtFixedRate(ffmpegHLSMediaSegmentDetector, startTime, period);
    }

    @Override
    public void cancel(int adaptiveBitrateStreaming) {
        if(threadPoolTaskScheduler != null){
            if(threadPoolTaskScheduler.getThreadNamePrefix().equals(Integer.toString(adaptiveBitrateStreaming))){
                log.info("shutdown - {}", adaptiveBitrateStreaming);
                threadPoolTaskScheduler.shutdown();
            }
        }
    }

    @Override
    public void onApplicationEvent(LastMediaSegmentSequenceEvent event) {
        if(threadPoolTaskScheduler != null){
            if(threadPoolTaskScheduler.getThreadNamePrefix().equals(Integer.toString(event.getAdaptiveBitrateStreaming()))) {
                log.info("shutdown - {}", event.getAdaptiveBitrateStreaming());
                threadPoolTaskScheduler.shutdown();
            }
        }
    }
}
