package pro.hirooka.chukasa.service;

import pro.hirooka.chukasa.domain.ReservedProgram;

import java.util.List;

public interface IRecorderService {
    ReservedProgram create(ReservedProgram reservedProgram);
    List<ReservedProgram> read();
    ReservedProgram read(int id);
    ReservedProgram update(ReservedProgram reservedProgram);
    void delete(int id);
    void deleteAll();
}
