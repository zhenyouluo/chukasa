package pro.hirooka.chukasa.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.hirooka.chukasa.domain.EPGDumpProgramInformation;
import pro.hirooka.chukasa.repository.IEPGDumpProgramInformationRepository;

import java.util.List;

@Slf4j
@Component
public class EPGDumpProgramTableService implements IEPGDumpProgramTableService {

    @Autowired
    private IEPGDumpProgramInformationRepository epgDumpProgramInformationRepository;

    @Override
    public EPGDumpProgramInformation create(EPGDumpProgramInformation epgDumpProgramInformation) {
        return epgDumpProgramInformationRepository.save(epgDumpProgramInformation);
    }

    @Override
    public List<EPGDumpProgramInformation> read() {
        return epgDumpProgramInformationRepository.findAll();
    }

    @Override
    public List<EPGDumpProgramInformation> read(int ch) {
        return null;
    }

    @Override
    public List<EPGDumpProgramInformation> read(String beginDate) {
        return null;
    }

    @Override
    public List<EPGDumpProgramInformation> read(int ch, String beginDate) {
        return null;
    }

    @Override
    public List<EPGDumpProgramInformation> readNow(long now) {
        return null;
    }

    @Override
    public EPGDumpProgramInformation read(long id) {
        return epgDumpProgramInformationRepository.findOne(id);
    }

    @Override
    public EPGDumpProgramInformation readNow(int ch, long now) {
        return null;
    }

    @Override
    public EPGDumpProgramInformation update(EPGDumpProgramInformation epgDumpProgramInformation) {
        return epgDumpProgramInformationRepository.save(epgDumpProgramInformation);
    }

    @Override
    public void delete(long id) {
        epgDumpProgramInformationRepository.delete(id);
    }

    @Override
    public void deleteAll() {
        epgDumpProgramInformationRepository.deleteAll();
    }
}
