package pro.hirooka.chukasa.service;

import pro.hirooka.chukasa.domain.ProgramInformation;

import java.util.List;

public interface IProgramTableService {
    ProgramInformation create(ProgramInformation programInformation);
    List<ProgramInformation> read();
    List<ProgramInformation> read(int ch);
    List<ProgramInformation> read(String beginDate);
    List<ProgramInformation> read(int ch, String beginDate);
    ProgramInformation read(long id);
    ProgramInformation update(ProgramInformation programInformation);
    void delete(long id);
    void deleteAll();
}
