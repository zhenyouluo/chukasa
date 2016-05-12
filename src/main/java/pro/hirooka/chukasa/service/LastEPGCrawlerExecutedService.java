//package pro.hirooka.chukasa.service;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import pro.hirooka.chukasa.domain.LastEPGCrawlerExecuted;
//import pro.hirooka.chukasa.repository.ILastEPGCrawlerExecutedRepository;
//
//@Slf4j
//@Component
//public class LastEPGCrawlerExecutedService implements ILastEPGCrawlerExecutedService {
//
//    @Autowired
//    private ILastEPGCrawlerExecutedRepository lastEPGCrawlerExecutedRepository;
//
//    @Override
//    public LastEPGCrawlerExecuted create(LastEPGCrawlerExecuted lastEPGCrawlerExecuted) {
//        return lastEPGCrawlerExecutedRepository.save(lastEPGCrawlerExecuted);
//    }
//
//    @Override
//    public LastEPGCrawlerExecuted read(int unique) {
//        return lastEPGCrawlerExecutedRepository.findOne(unique);
//    }
//
//    @Override
//    public LastEPGCrawlerExecuted update(LastEPGCrawlerExecuted lastEPGCrawlerExecuted) {
//        return lastEPGCrawlerExecutedRepository.save(lastEPGCrawlerExecuted);
//    }
//
//    @Override
//    public void delete(int unique) {
//        lastEPGCrawlerExecutedRepository.delete(unique);
//    }
//}
