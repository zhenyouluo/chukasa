package pro.hirooka.chukasa.domain.service.chukasa.detector;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.fn.Consumer;

import javax.annotation.PostConstruct;
import java.util.Date;

import static java.util.Objects.requireNonNull;
import static reactor.bus.selector.Selectors.$;

@Slf4j
@Service
public class IntermediateFFmpegHLSMediaSegmentDetectorService implements IIntermediateFFmpegHLSMediaSegmentDetectorService, Consumer<Event<Integer>> {

    private final FFmpegHLSMediaSegmentDetector ffmpegHLSMediaSegmentDetector;
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;
    private final EventBus eventBus;

    @Autowired
    public IntermediateFFmpegHLSMediaSegmentDetectorService(FFmpegHLSMediaSegmentDetector ffmpegHLSMediaSegmentDetector, EventBus eventBus) {
        this.ffmpegHLSMediaSegmentDetector = requireNonNull(ffmpegHLSMediaSegmentDetector, "ffmpegHLSMediaSegmentDetector");
        this.eventBus = requireNonNull(eventBus, "eventBus");
    }

    @Async
    @Override
    public void schedule(int adaptiveBitrateStreaming, Date startTime, long period) {
        ffmpegHLSMediaSegmentDetector.setAdaptiveBitrateStreaming(adaptiveBitrateStreaming);
        threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setThreadNamePrefix(Integer.toString(adaptiveBitrateStreaming));
        threadPoolTaskScheduler.setPoolSize(3);
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
    public void accept(Event<Integer> integerEvent) {
        if(threadPoolTaskScheduler != null){
            if(threadPoolTaskScheduler.getThreadNamePrefix().equals(Integer.toString(integerEvent.getData()))) {
                log.info("shutdown - {}", integerEvent.getData());
                threadPoolTaskScheduler.shutdown();
            }
        }
    }

    @PostConstruct
    void init(){
        eventBus.on($("FFmpegHLSMediaSegmentDetector"), this);
    }
}
