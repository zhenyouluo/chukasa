package pro.hirooka.chukasa.service.recorder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.hirooka.chukasa.domain.recorder.Program;
import pro.hirooka.chukasa.repository.IProgramRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ProgramTableService implements IProgramTableService {

    @Autowired
    private IProgramRepository programRepository;

    @Override
    public Program create(Program Program) {
        return programRepository.save(Program);
    }

    @Override
    public List<Program> read() {
        return programRepository.findAll();
    }

    @Override
    public List<Program> read(int ch) {
        return programRepository.findAllByCh(ch);
    }

    @Override
    public List<Program> read(String beginDate) {
        return null;
    }

    @Override
    public List<Program> read(int ch, String beginDate) {
        return null;
    }

    @Override
    public List<Program> readByNow(long now)  {
        return programRepository.findAllByNowLike(now);
    }

    @Override
    public Program read(long id) {
        return programRepository.findOne(id);
    }

    @Override
    public Program readNow(int ch, long now) {
        return null;
    }

    @Override
    public Program update(Program Program) {
        return programRepository.save(Program);
    }

    @Override
    public void delete(long id) {
        programRepository.delete(id);
    }

    @Override
    public void deleteAll() {
        programRepository.deleteAll();
    }

    @Override
    public int getNumberOfPhysicalChannels() {
        return programRepository.findAll().stream().map(Program::getChannel).collect(Collectors.toSet()).size();
    }
}
