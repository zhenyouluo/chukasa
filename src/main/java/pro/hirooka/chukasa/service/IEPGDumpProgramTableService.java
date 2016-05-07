package pro.hirooka.chukasa.service;

import pro.hirooka.chukasa.domain.EPGDumpProgramInformation;

import java.util.List;

public interface IEPGDumpProgramTableService {
    EPGDumpProgramInformation create(EPGDumpProgramInformation epgDumpProgramInformation);
    List<EPGDumpProgramInformation> read();
    List<EPGDumpProgramInformation> read(int ch);
    List<EPGDumpProgramInformation> read(String beginDate);
    List<EPGDumpProgramInformation> read(int ch, String beginDate);
    List<EPGDumpProgramInformation> readNow(long now);
    EPGDumpProgramInformation read(long id);
    EPGDumpProgramInformation readNow(int ch, long now);
    EPGDumpProgramInformation update(EPGDumpProgramInformation epgDumpProgramInformation);
    void delete(long id);
    void deleteAll();
}
