package pro.hirooka.chukasa.recorder;

import groovy.util.logging.Slf4j;
import lombok.Getter;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import pro.hirooka.chukasa.configuration.SystemConfiguration;
import pro.hirooka.chukasa.domain.ReservedProgram;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

@Slf4j
public class Recorder {

    private  SystemConfiguration systemConfiguration;

    public Recorder(SystemConfiguration systemConfiguration){
        this.systemConfiguration = systemConfiguration;
    }

    @Getter
    Map<Integer, ScheduledFuture> scheduledFutureMap = new HashMap<>();

    @Async
    public void reserve(ReservedProgram reservedProgram){
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        TaskScheduler taskScheduler = new ConcurrentTaskScheduler(scheduledExecutorService);
        Date date = new Date(reservedProgram.getStart());
        Runnable runnable = new RecorderRunner(systemConfiguration, reservedProgram);
        ScheduledFuture scheduledFuture = taskScheduler.schedule(runnable, date);
        scheduledFutureMap.put(reservedProgram.getId() , scheduledFuture);
    }

    @Async
    public void reserve(List<ReservedProgram> reservedProgramList){

        // todo
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        TaskScheduler taskScheduler = new ConcurrentTaskScheduler(scheduledExecutorService);
        Date date = new Date(reservedProgramList.get(0).getStart());
        Runnable runnable = new RecorderRunner(systemConfiguration, reservedProgramList.get(0));
        ScheduledFuture scheduledFuture = taskScheduler.schedule(runnable, date);
        scheduledFutureMap.put(0, scheduledFuture);
    }

    public void cancel(int id){
        if(scheduledFutureMap.containsKey(id)){
            scheduledFutureMap.get(id).cancel(true);
        }
    }

    public void cancelAll(){
        scheduledFutureMap.values().stream().forEach(scheduledFuture -> {
            scheduledFuture.cancel(true);
        });
    }

}
