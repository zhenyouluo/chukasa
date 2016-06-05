package pro.hirooka.chukasa.service.recorder;

import pro.hirooka.chukasa.domain.recorder.Program;

import java.util.List;

public interface IProgramTableService {
    Program create(Program program);
    List<Program> read();
    List<Program> read(int ch);
    List<Program> read(String beginDate);
    List<Program> read(int ch, String beginDate);
    List<Program> readByNow(long now);
    Program read(long id);
    Program readNow(int ch, long now);
    Program update(Program Program);
    void delete(long id);
    void deleteAll();
    int getNumberOfPhysicalChannels();
}
