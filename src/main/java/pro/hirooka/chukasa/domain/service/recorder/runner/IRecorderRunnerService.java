package pro.hirooka.chukasa.domain.service.recorder.runner;

import pro.hirooka.chukasa.domain.model.recorder.ReservedProgram;

import java.util.concurrent.Future;

public interface IRecorderRunnerService {
    Future<Integer> submit(ReservedProgram reservedProgram);
    void cancel();
}
