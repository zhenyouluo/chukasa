package pro.hirooka.chukasa.service;

import pro.hirooka.chukasa.domain.ProgramInformation;

import java.util.List;

public interface IProgramTableService {
    ProgramInformation create(ProgramInformation programInformation);
    List<ProgramInformation> read();
    ProgramInformation read(long id);
    ProgramInformation update(ProgramInformation programInformation);
    void delete(long id);
    void deleteAll();
}
