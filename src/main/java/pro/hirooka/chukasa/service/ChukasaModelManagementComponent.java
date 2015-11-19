package pro.hirooka.chukasa.service;

import org.springframework.stereotype.Component;
import pro.hirooka.chukasa.domain.ChukasaModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChukasaModelManagementComponent implements IChukasaModelManagementComponent {

    private Map<Integer, ChukasaModel> chukasaModelLMap = new ConcurrentHashMap<>();

    @Override
    public ChukasaModel create(int adaptiveBitrateStreaming, ChukasaModel chukasaModel) {
        if(!chukasaModelLMap.containsKey(adaptiveBitrateStreaming)) {
            chukasaModelLMap.put(adaptiveBitrateStreaming, chukasaModel);
            return chukasaModelLMap.get(adaptiveBitrateStreaming);
        }
        return null;
    }

    @Override
    public List<ChukasaModel> get() {
        return new ArrayList<>(chukasaModelLMap.values());
    }

    @Override
    public ChukasaModel get(int adaptiveBitrateStreaming) {
        if(chukasaModelLMap.containsKey(adaptiveBitrateStreaming)) {
            return chukasaModelLMap.get(adaptiveBitrateStreaming);
        }
        return null;
    }

    @Override
    public ChukasaModel update(int adaptiveBitrateStreaming, ChukasaModel chukasaModel) {
        if(chukasaModelLMap.containsKey(adaptiveBitrateStreaming)){
            chukasaModelLMap.put(adaptiveBitrateStreaming, chukasaModel);
            return chukasaModelLMap.get(adaptiveBitrateStreaming);
        }
        return null;
    }

    @Override
    public void delete(int adaptiveBitrateStreaming) {
        if(chukasaModelLMap.containsKey(adaptiveBitrateStreaming)){
            chukasaModelLMap.remove(adaptiveBitrateStreaming);
        }
    }

    @Override
    public void deleteAll() {
        chukasaModelLMap.clear();
    }
}
