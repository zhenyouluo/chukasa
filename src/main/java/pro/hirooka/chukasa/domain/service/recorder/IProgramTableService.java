package pro.hirooka.chukasa.domain.service.recorder;

import pro.hirooka.chukasa.domain.model.recorder.Program;

import java.util.List;

public interface IProgramTableService {
    Program create(Program program);
    List<Program> read();
    List<Program> read(int ch);
    List<Program> readByBeginDate(String beginDate);
    List<Program> read(int ch, String beginDate);
    List<Program> readByNow(long now);
    List<Program> readOneDayByNow(long now);
    Program read(String id);
    Program readNow(int ch, long now);
    Program update(Program Program);
    void delete(String id);
    void deleteAll();
    int getNumberOfPhysicalChannels();
}
