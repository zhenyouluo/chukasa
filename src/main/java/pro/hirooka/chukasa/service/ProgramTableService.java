package pro.hirooka.chukasa.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.hirooka.chukasa.domain.ProgramInformation;
import pro.hirooka.chukasa.repository.IProgramInformationRepository;

import java.util.List;

@Slf4j
@Component
public class ProgramTableService implements IProgramTableService {

    @Autowired
    private IProgramInformationRepository programInformationRepository;

    @Override
    public ProgramInformation create(ProgramInformation programInformation) {
        return programInformationRepository.save(programInformation);
    }

    @Override
    public List<ProgramInformation> read() {
        return programInformationRepository.findAll();
    }

    @Override
    public ProgramInformation read(long id) {
        return programInformationRepository.findOne(id);
    }

    @Override
    public ProgramInformation update(ProgramInformation programInformation) {
        return programInformationRepository.save(programInformation);
    }

    @Override
    public void delete(long id) {
        programInformationRepository.delete(id);
    }

    @Override
    public void deleteAll() {
        programInformationRepository.deleteAll();
    }
}
