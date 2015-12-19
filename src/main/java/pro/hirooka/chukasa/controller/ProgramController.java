package pro.hirooka.chukasa.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import pro.hirooka.chukasa.domain.ProgramInformation;
import pro.hirooka.chukasa.domain.ReservedProgram;
import pro.hirooka.chukasa.service.IProgramTableService;
import pro.hirooka.chukasa.service.IRecorderService;

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
        List<ProgramInformation> programInformationList = programTableService.read();
        model.addAttribute("programInformationList", programInformationList);
        return "programs/list";
    }

    @RequestMapping(value = "create", method = RequestMethod.POST)
    String create(@Validated ReservedProgram reservedProgram, BindingResult bindingResult, Model model){
        if(bindingResult.hasErrors()){
            return read(model);
        }
        log.info("reservation -> {}", reservedProgram.toString());
        int ch = reservedProgram.getCh();
        String beginDate = reservedProgram.getBeginDate();
        String endDate = reservedProgram.getEndDate();
        String title = reservedProgram.getTitle();
        String summary = reservedProgram.getSummary();
        ReservedProgram createdReservedProgram = new ReservedProgram();
        createdReservedProgram.setCh(ch);
        createdReservedProgram.setBeginDate(beginDate);
        createdReservedProgram.setEndDate(endDate);
        createdReservedProgram.setTitle(title);
        createdReservedProgram.setSummary(summary);
        recorderService.create(createdReservedProgram);
        return "redirect:/programs";
    }

}
