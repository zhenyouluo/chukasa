package pro.hirooka.chukasa.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import pro.hirooka.chukasa.domain.configuration.ChukasaConfiguration;
import pro.hirooka.chukasa.domain.configuration.EpgdumpConfiguration;
import pro.hirooka.chukasa.domain.configuration.SystemConfiguration;
import pro.hirooka.chukasa.api.v1.helper.IChukasaBrowserDetector;
import pro.hirooka.chukasa.domain.model.chukasa.VideoFile;
import pro.hirooka.chukasa.domain.model.epgdump.enums.EpgdumpStatus;
import pro.hirooka.chukasa.domain.model.recorder.*;
import pro.hirooka.chukasa.domain.service.chukasa.IRecordingProgramManagementComponent;
import pro.hirooka.chukasa.domain.service.common.ulitities.ICommonUtilityService;
import pro.hirooka.chukasa.domain.service.epgdump.IEpgdumpService;
import pro.hirooka.chukasa.domain.service.recorder.IProgramTableService;
import pro.hirooka.chukasa.domain.service.epgdump.ILastEpgdumpExecutedService;
import pro.hirooka.chukasa.domain.service.recorder.IRecorderService;
import pro.hirooka.chukasa.domain.service.chukasa.ISystemService;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static pro.hirooka.chukasa.domain.model.chukasa.constants.ChukasaConstant.FILE_SEPARATOR;

@Slf4j
@Controller
public class IndexController {

    @Autowired
    SystemConfiguration systemConfiguration;
    @Autowired
    ChukasaConfiguration chukasaConfiguration;
    @Autowired
    EpgdumpConfiguration epgdumpConfiguration;
    @Autowired
    ISystemService systemService;
    @Autowired
    IProgramTableService programTableService;
    @Autowired
    ILastEpgdumpExecutedService lastEpgdumpExecutedService;
    @Autowired
    private HttpServletRequest httpServletRequest;
    @Autowired
    IRecordingProgramManagementComponent recordingProgramManagementComponent;
    @Autowired
    IRecorderService recorderService;
    @Autowired
    IChukasaBrowserDetector chukasaBrowserDetector;
    @Autowired
    IEpgdumpService epgdumpService;
    @Autowired
    ICommonUtilityService commonUtilityService;

    @RequestMapping("/")
    String index(Model model){

        if(epgdumpService.getStatus().equals(EpgdumpStatus.RUNNING)){
            log.info("EpgdumpService is running.");
            // TODO:
        }

        boolean isSupported = false;
        String userAgent = httpServletRequest.getHeader("user-agent");
        if(chukasaBrowserDetector.isNativeSupported(userAgent) || chukasaBrowserDetector.isAlternativeSupported(userAgent)){
            isSupported = true;
        }
        log.info("{} : {}", isSupported, userAgent);

        boolean isFFmpeg = systemService.isFFmpeg();
        boolean isPTx = systemService.isPTx();
        boolean isRecpt1 = systemService.isRecpt1();
        boolean isEpgdump = systemService.isEpgdump();
        boolean isMongoDB = systemService.isMongoDB();
        boolean isWebCamera = systemService.isWebCamera();

        // PTx
        List<Program> programList = new ArrayList<>();
        boolean isLastEpgdumpExecuted = false;

        List<ChannelSettings> channelSettingsList = commonUtilityService.getChannelSettingsList();

//        Map<String, String> epgdumpChannelMap = new HashMap<>();
//        Resource resource = new ClassPathResource(epgdumpConfiguration.getPhysicalChannelMap());
//        ObjectMapper objectMapper = new ObjectMapper();
//        try {
//            epgdumpChannelMap = objectMapper.readValue(resource.getInputStream(), HashMap.class);
//            log.info(epgdumpChannelMap.toString());
//        } catch (IOException e) {
//            log.error("invalid epgdump_channel_map.json: {} {}", e.getMessage(), e);
//        }
        if(isMongoDB && isEpgdump){
            programList = programTableService.readByNow(new Date().getTime());
            programList = programList.stream().sorted(Comparator.comparing(Program::getPhysicalLogicalChannel)).collect(Collectors.toList());
//            if(programList != null && lastEpgdumpExecutedService.read(1) != null && programTableService.getNumberOfPhysicalChannels() >= epgdumpChannelMap.size()){
            if(programList != null && lastEpgdumpExecutedService.read(1) != null && programTableService.getNumberOfPhysicalLogicalChannels() >= channelSettingsList.size()){
                isLastEpgdumpExecuted = true;
            }
        }

        // PTx (switch Program/Channel)
        boolean isPTxByProgram = false;
        if(isFFmpeg && isPTx && isRecpt1 && isLastEpgdumpExecuted){
            isPTxByProgram = true;
        }
        boolean isPTxByChannel = false;
        if(isFFmpeg && isPTx && isRecpt1 && !isLastEpgdumpExecuted){
            programList = new ArrayList<>();
            isPTxByChannel = true;
            for(ChannelSettings channelSettings : channelSettingsList){
                try {
                    Program program = new Program();
                    program.setPhysicalLogicalChannel(channelSettings.getPhysicalLogicalChannel());
                    programList.add(program);
                }catch (NumberFormatException e){
                    log.error("invalid value {} {}", e.getMessage(), e);
                }
            }
            //if(epgDumpProgramInformationList.size() == 0) {
//                for (Map.Entry<String, String> entry : epgdumpChannelMap.entrySet()) {
//                    try {
//                        Program program = new Program();
//                        program.setPhysicalChannel(Integer.parseInt(entry.getKey()));
//                        programList.add(program);
//                    }catch (NumberFormatException e){
//                        log.error("invalid value {} {}", e.getMessage(), e);
//                    }
//                }
            //}
        }

        // FILE
        List<VideoFile> videoFileModelList = new ArrayList<>();
        File fileDirectory = new File(systemConfiguration.getFilePath());
        File[] fileArray = fileDirectory.listFiles();
        if(fileArray != null) {
            String[] videoFileExtensionArray = chukasaConfiguration.getVideoFileExtension();
            List<String> videoFileExtensionList = Arrays.asList(videoFileExtensionArray);
            for (File file : fileArray) {
                for(String videoFileExtension : videoFileExtensionList){
                    if(file.getName().endsWith("." + videoFileExtension)){
                        VideoFile videoFileModel = new VideoFile();
                        videoFileModel.setName(file.getName());
                        videoFileModelList.add(videoFileModel);
                    }
                }
            }
        }else{
            log.warn("'{}' does not exist.", fileDirectory);
        }

        // Okkake
        List<VideoFile> okkakeVideoFileModelList = new ArrayList<>();
        List<RecordingProgramModel> recordingProgramModelList = recordingProgramManagementComponent.get();
        for(RecordingProgramModel recordingProgramModel : recordingProgramModelList){
            Date now = new Date();
            if(recordingProgramModel.getStopRecording() > now.getTime() && now.getTime() > recordingProgramModel.getStartRecording()){
                String file = systemConfiguration.getFilePath() + FILE_SEPARATOR + recordingProgramModel.getFileName();
                if(new File(file).exists()){
                    VideoFile videoFileModel = new VideoFile();
                    videoFileModel.setName(recordingProgramModel.getFileName());
                    okkakeVideoFileModelList.add(videoFileModel);
                }
            }
        }
        List<ReservedProgram> reservedProgramList = recorderService.read();
        for(ReservedProgram reservedProgram : reservedProgramList){
            Date now = new Date();
            if(reservedProgram.getStopRecording() > now.getTime() && now.getTime() > reservedProgram.getStartRecording()){
                String file = systemConfiguration.getFilePath() + FILE_SEPARATOR + reservedProgram.getFileName();
                if(new File(file).exists()){
                    boolean isDuplicated = false;
                    for(RecordingProgramModel recordingProgramModel : recordingProgramModelList){
                        if(recordingProgramModel.getFileName().equals(reservedProgram.getFileName())){
                            isDuplicated = true;
                            break;
                        }
                    }
                    if(!isDuplicated) {
                        VideoFile videoFileModel = new VideoFile();
                        videoFileModel.setName(reservedProgram.getFileName());
                        okkakeVideoFileModelList.add(videoFileModel);
                    }
                }
            }
        }

//        File okkakeVideoFileDirectory = new File(systemConfiguration.getFilePath());
//        File[] okkakeVideoFileArray = okkakeVideoFileDirectory.listFiles();
//        if(okkakeVideoFileArray != null) {
//            for (File file : fileArray) {
//                if(file.getName().endsWith(".ts")){
//                    VideoFile okkakeVideoFileModel = new VideoFile();
//                    okkakeVideoFileModel.setName(file.getName());
//                    okkakeVideoFileModelList.add(okkakeVideoFileModel);
//                }
//            }
//        }else{
//            log.warn("'{}' does not exist.", okkakeVideoFileDirectory);
//        }

        model.addAttribute("isSupported", isSupported);
        model.addAttribute("isPTxByChannel", isPTxByChannel);
        model.addAttribute("isPTxByProgram", isPTxByProgram);
        model.addAttribute("isWebCamera", isWebCamera);
        model.addAttribute("videoFileModelList", videoFileModelList);
        model.addAttribute("okkakeVideoFileModelList", okkakeVideoFileModelList);
        model.addAttribute("programList", programList);

        return "index";
    }
}
