package pro.hirooka.chukasa.service.chukasa;

import org.springframework.stereotype.Component;
import pro.hirooka.chukasa.domain.recorder.RecordingProgramModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RecordingProgramManagementComponent implements IRecordingProgramManagementComponent {

    private Map<Integer, RecordingProgramModel> recordingProgramModelMap = new ConcurrentHashMap<>();

    @Override
    public RecordingProgramModel create(int id, RecordingProgramModel recordingProgramModel) {
        if(!recordingProgramModelMap.containsKey(id)){
            recordingProgramModelMap.put(id, recordingProgramModel);
            return recordingProgramModelMap.get(id);
        }
        return null;
    }

    @Override
    public List<RecordingProgramModel> get() {
        return new ArrayList<>(recordingProgramModelMap.values());
    }

    @Override
    public RecordingProgramModel get(int id) {
        if(recordingProgramModelMap.containsKey(id)){
            return recordingProgramModelMap.get(id);
        }
        return null;
    }

    @Override
    public RecordingProgramModel update(int id, RecordingProgramModel recordingProgramModel) {
        if(recordingProgramModelMap.containsKey(id)){
            recordingProgramModelMap.put(id, recordingProgramModel);
            return recordingProgramModelMap.get(id);
        }
        return null;
    }

    @Override
    public void delete(int id) {
        if(recordingProgramModelMap.containsKey(id)){
            recordingProgramModelMap.remove(id);
        }
    }

    @Override
    public void deleteAll() {
        recordingProgramModelMap.clear();
    }
}
