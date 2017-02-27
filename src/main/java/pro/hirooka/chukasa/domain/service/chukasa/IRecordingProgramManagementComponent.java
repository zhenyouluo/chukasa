package pro.hirooka.chukasa.domain.service.chukasa;

import pro.hirooka.chukasa.domain.model.recorder.RecordingProgramModel;

import java.util.List;

public interface IRecordingProgramManagementComponent {
    RecordingProgramModel create(int id, RecordingProgramModel recordingProgramModel);
    List<RecordingProgramModel> get();
    RecordingProgramModel get(int id);
    RecordingProgramModel update(int id, RecordingProgramModel recordingProgramModel);
    void delete(int id);
    void deleteAll();
}
