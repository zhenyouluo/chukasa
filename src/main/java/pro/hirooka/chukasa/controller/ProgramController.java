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
import pro.hirooka.chukasa.domain.recorder.Program;
import pro.hirooka.chukasa.domain.recorder.ReservedProgram;
import pro.hirooka.chukasa.service.recorder.IProgramTableService;
import pro.hirooka.chukasa.service.recorder.IRecorderService;

import java.util.List;

import static java.util.Objects.requireNonNull;

@Slf4j
@Controller
@RequestMapping("programs")
public class ProgramController {

    private final IProgramTableService programTableService;
    private final IRecorderService recorderService;

    @Autowired
    public ProgramController(IProgramTableService programTableService, IRecorderService recorderService){
        this.programTableService = requireNonNull(programTableService, "programTableService");
        this.recorderService = requireNonNull(recorderService, "recorderService");
    }

    @RequestMapping(method = RequestMethod.GET)
    String read(Model model){
        List<Program> programList = programTableService.read();
        model.addAttribute("programList", programList);
        return "programs/list";
    }

    @RequestMapping(value = "{ch}", method = RequestMethod.GET)
    String read(@PathVariable int ch, Model model){
        List<Program> programList = programTableService.read(ch);
        model.addAttribute("programList", programList);
        return "programs/list";
    }

    @RequestMapping(value = "{channel}/{date}", method = RequestMethod.GET)
    String read(@PathVariable int channel, @PathVariable String date, Model model){
        List<Program> programList = programTableService.read(channel, date);
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
        createdReservedProgram.setEnd(reservedProgram.getEnd());
        createdReservedProgram.setDuration(reservedProgram.getDuration());
        createdReservedProgram.setCh(reservedProgram.getCh());
        createdReservedProgram.setChannelName(reservedProgram.getChannelName());
        createdReservedProgram.setBeginDate(reservedProgram.getBeginDate());
        createdReservedProgram.setEndDate(reservedProgram.getEndDate());

        log.info("createdReservedProgram -> {}", createdReservedProgram.toString());
        recorderService.create(createdReservedProgram);
        return "redirect:/programs";
    }
}
