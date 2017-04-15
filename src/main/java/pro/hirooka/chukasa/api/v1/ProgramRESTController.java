package pro.hirooka.chukasa.api.v1;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import pro.hirooka.chukasa.domain.model.recorder.M4vTranscodingStatus;
import pro.hirooka.chukasa.domain.model.recorder.Program;
import pro.hirooka.chukasa.domain.model.recorder.RecordingStatus;
import pro.hirooka.chukasa.domain.model.recorder.ReservedProgram;
import pro.hirooka.chukasa.domain.service.recorder.IProgramTableService;
import pro.hirooka.chukasa.domain.service.recorder.IRecorderService;

import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/v1/programs")
public class ProgramRESTController {

    @Autowired
    IProgramTableService programTableService;
    @Autowired
    IRecorderService recorderService;

    @RequestMapping(method = RequestMethod.GET)
    List<Program> read(){
        Date now = new Date();
        List<Program> programList = programTableService.readOneDayByNow(now.getTime());
        return programList;
    }

    @RequestMapping(value = "now", method = RequestMethod.GET)
    List<Program> readNow(){
        Date now = new Date();
        List<Program> programList = programTableService.readByNow(now.getTime());
        return programList;
    }

    @RequestMapping(value = "{physicalLogicalChannel}", method = RequestMethod.GET)
    List<Program> read(@PathVariable int physicalLogicalChannel){
        List<Program> programList = programTableService.read(physicalLogicalChannel);
        return programList;
    }

    @RequestMapping(value = "{physicalLogicalChannel}/{date}", method = RequestMethod.GET)
    List<Program> read(@PathVariable int physicalLogicalChannel, @PathVariable String date){
        List<Program> programList = programTableService.read(physicalLogicalChannel, date);
        return programList;
    }

    @RequestMapping(value = "create", method = RequestMethod.POST)
    ReservedProgram create(@Validated ReservedProgram reservedProgram){

        log.info("reservation -> {}", reservedProgram.toString());
        ReservedProgram createdReservedProgram = new ReservedProgram();
        createdReservedProgram.setChannel(reservedProgram.getChannel());
        createdReservedProgram.setTitle(reservedProgram.getTitle());
        createdReservedProgram.setDetail(reservedProgram.getDetail());
        createdReservedProgram.setStart(reservedProgram.getStart());
        createdReservedProgram.setBegin(reservedProgram.getStart());
        createdReservedProgram.setEnd(reservedProgram.getEnd());
        createdReservedProgram.setDuration(reservedProgram.getDuration());
        createdReservedProgram.setPhysicalLogicalChannel(reservedProgram.getPhysicalLogicalChannel());
        createdReservedProgram.setRemoteControllerChannel(reservedProgram.getRemoteControllerChannel());
        createdReservedProgram.setChannelName(reservedProgram.getChannelName());
        createdReservedProgram.setBeginDate(reservedProgram.getBeginDate());
        createdReservedProgram.setEndDate(reservedProgram.getEndDate());
        createdReservedProgram.setStartRecording(reservedProgram.getBegin());
        createdReservedProgram.setStopRecording(reservedProgram.getEnd());
        createdReservedProgram.setRecordingDuration(reservedProgram.getDuration());
        createdReservedProgram.setFileName("");
        createdReservedProgram.setRecordingStatus(RecordingStatus.Reserved);
        createdReservedProgram.setM4vTranscodingStatus(M4vTranscodingStatus.None);

        log.info("createdReservedProgram -> {}", createdReservedProgram.toString());
        return recorderService.create(createdReservedProgram);
    }
}
