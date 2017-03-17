package pro.hirooka.chukasa.domain.service.chukasa.remover;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class IntermediateChukasaHLSFileRemoverService implements IIntermediateChukasaHLSFileRemoverService {

    private final IChukasaHLSFileRemoverService chukasaHLSFileRemoverService;

    @Setter
    private String streamRootPath;

    @Autowired
    public IntermediateChukasaHLSFileRemoverService(IChukasaHLSFileRemoverService chukasaHLSFileRemoverService) {
        this.chukasaHLSFileRemoverService = chukasaHLSFileRemoverService;
    }

    @Async
    @Override
    public void remove(String streamRootPath) {
        chukasaHLSFileRemoverService.remove(streamRootPath);
    }
}
