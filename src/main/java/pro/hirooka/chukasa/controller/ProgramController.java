package pro.hirooka.chukasa.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import pro.hirooka.chukasa.domain.recorder.M4vTranscodingStatus;
import pro.hirooka.chukasa.domain.recorder.Program;
import pro.hirooka.chukasa.domain.recorder.RecordingStatus;
import pro.hirooka.chukasa.domain.recorder.ReservedProgram;
import pro.hirooka.chukasa.service.recorder.IProgramTableService;
import pro.hirooka.chukasa.service.recorder.IRecorderService;

import java.util.Date;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("programs")
public class ProgramController {

    @Autowired
    IProgramTableService programTableService;
    @Autowired
    IRecorderService recorderService;

    @RequestMapping(method = RequestMethod.GET)
    String read(Model model){
        //List<Program> programList = programTableService.read();
        Date now = new Date();
        List<Program> programList = programTableService.readOneDayByNow(now.getTime());
        model.addAttribute("programList", programList);
        return "programs/list";
    }

    @RequestMapping(value = "now", method = RequestMethod.GET)
    String readNow(Model model){
        Date now = new Date();
        List<Program> programList = programTableService.readByNow(now.getTime());
        model.addAttribute("programList", programList);
        return "programs/list";
    }

    @RequestMapping(value = "{physicalChannel}", method = RequestMethod.GET)
    String read(@PathVariable int physicalChannel, Model model){
        List<Program> programList = programTableService.read(physicalChannel);
        model.addAttribute("programList", programList);
        return "programs/list";
    }

    @RequestMapping(value = "{physicalChannel}/{date}", method = RequestMethod.GET)
    String read(@PathVariable int physicalChannel, @PathVariable String date, Model model){
        List<Program> programList = programTableService.read(physicalChannel, date);
        model.addAttribute("programList", programList);
        return "programs/list";
    }

    @RequestMapping(value = "create", method = RequestMethod.POST)
    String create(@Validated ReservedProgram reservedProgram, BindingResult bindingResult, Model model){
        if(bindingResult.hasErrors()){
            return read(model);
        }
        log.info("reservation -> {}", reservedProgram.toString());
        ReservedProgram createdReservedProgram = new ReservedProgram();
        createdReservedProgram.setChannel(reservedProgram.getChannel());
        createdReservedProgram.setTitle(reservedProgram.getTitle());
        createdReservedProgram.setDetail(reservedProgram.getDetail());
        createdReservedProgram.setStart(reservedProgram.getStart());
        createdReservedProgram.setBegin(reservedProgram.getStart());
        createdReservedProgram.setEnd(reservedProgram.getEnd());
        createdReservedProgram.setDuration(reservedProgram.getDuration());
        createdReservedProgram.setPhysicalChannel(reservedProgram.getPhysicalChannel());
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
        recorderService.create(createdReservedProgram);
        return "redirect:/programs";
    }
}
