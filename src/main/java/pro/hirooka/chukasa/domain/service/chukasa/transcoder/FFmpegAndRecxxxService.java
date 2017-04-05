package pro.hirooka.chukasa.domain.service.chukasa.transcoder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import pro.hirooka.chukasa.domain.model.chukasa.ChukasaModel;
import pro.hirooka.chukasa.domain.model.chukasa.enums.HardwareAccelerationType;
import pro.hirooka.chukasa.domain.model.common.TunerStatus;
import pro.hirooka.chukasa.domain.model.common.enums.RecxxxDriverType;
import pro.hirooka.chukasa.domain.model.epgdump.RecdvbBSModel;
import pro.hirooka.chukasa.domain.model.recorder.ChannelConfiguration;
import pro.hirooka.chukasa.domain.model.recorder.enums.ChannelType;
import pro.hirooka.chukasa.domain.service.chukasa.IChukasaModelManagementComponent;
import pro.hirooka.chukasa.domain.service.common.ITunerManagementService;
import pro.hirooka.chukasa.domain.service.common.ulitities.ICommonUtilityService;
import pro.hirooka.chukasa.domain.service.epgdump.runner.helper.IEpgdumpRecdvbHelper;

import java.io.*;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.Future;

import static pro.hirooka.chukasa.domain.model.chukasa.constants.ChukasaConstant.*;
import static pro.hirooka.chukasa.domain.model.chukasa.constants.ChukasaConstant.FILE_SEPARATOR;

@Slf4j
@Service
public class FFmpegAndRecxxxService implements IFFmpegAndRecxxxService {

    private final IChukasaModelManagementComponent chukasaModelManagementComponent;
    private final ICommonUtilityService commonUtilityService;

    @Autowired
    private ITunerManagementService tunerManagementService;
    @Autowired
    private IEpgdumpRecdvbHelper epgdumpRecdvbHelper;

    @Autowired
    public FFmpegAndRecxxxService(IChukasaModelManagementComponent chukasaModelManagementComponent, ICommonUtilityService commonUtilityService){
        this.chukasaModelManagementComponent = chukasaModelManagementComponent;
        this.commonUtilityService = commonUtilityService;
    }

    @Async
    @Override
    public Future<Integer> submit(int adaptiveBitrateStreaming) {

        final RecxxxDriverType recxxxDriverType = tunerManagementService.getRecxxxDriverType();
        TunerStatus tunerStatusGR = tunerManagementService.findOne(ChannelType.GR);
        TunerStatus tunerStatusBS = tunerManagementService.findOne(ChannelType.BS);

        // TODO: final
        ChukasaModel chukasaModel = chukasaModelManagementComponent.get(adaptiveBitrateStreaming);
        log.debug("StreamPath: {}", chukasaModel.getStreamPath());

        final HardwareAccelerationType hardwareAccelerationType = chukasaModel.getHardwareAccelerationType();

        final boolean canEncrypt = chukasaModel.getChukasaSettings().isCanEncrypt();
        final String ffmpegOutputPath;
        if(canEncrypt){
            ffmpegOutputPath = chukasaModel.getTempEncPath() + FILE_SEPARATOR + STREAM_FILE_NAME_PREFIX + "%d" + STREAM_FILE_EXTENSION;
        } else {
            ffmpegOutputPath = chukasaModel.getStreamPath() + FILE_SEPARATOR + STREAM_FILE_NAME_PREFIX + "%d" + STREAM_FILE_EXTENSION;
        }
        final String ffmpegM3U8OutputPath;
        if(canEncrypt){
            ffmpegM3U8OutputPath = chukasaModel.getTempEncPath() + FILE_SEPARATOR + M3U8_FILE_NAME + M3U8_FILE_EXTENSION;
        } else {
            ffmpegM3U8OutputPath = chukasaModel.getStreamPath() + FILE_SEPARATOR + M3U8_FILE_NAME + M3U8_FILE_EXTENSION;
        }

        ChannelType channelType = ChannelType.GR;
        List<ChannelConfiguration> channelConfigurationList = commonUtilityService.getChannelConfigurationList();
        for(ChannelConfiguration channelConfiguration : channelConfigurationList){
            if(channelConfiguration.getPhysicalLogicalChannel() == chukasaModel.getChukasaSettings().getPhysicalLogicalChannel()){
                if(channelConfiguration.getChannelType() == ChannelType.GR){
                    channelType = ChannelType.GR;
                    tunerStatusGR = tunerManagementService.update(tunerStatusGR, false);
                    chukasaModel.setTunerDeviceName(tunerStatusGR.getDeviceName());
                    chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);
                }else if(channelConfiguration.getChannelType() == ChannelType.BS){
                    channelType = ChannelType.BS;
                    tunerStatusBS = tunerManagementService.update(tunerStatusBS, false);
                    chukasaModel.setTunerDeviceName(tunerStatusBS.getDeviceName());
                    chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);
                }
            }
        }
        RecdvbBSModel recdvbBSModel = epgdumpRecdvbHelper.resovle(chukasaModel.getChukasaSettings().getPhysicalLogicalChannel());

        int frequency = 0;
        for(ChannelConfiguration channelConfiguration : channelConfigurationList){
            if(channelConfiguration.getPhysicalLogicalChannel() == chukasaModel.getChukasaSettings().getPhysicalLogicalChannel()){
                frequency = channelConfiguration.getFrequency();
            }
        }

        final String[] commandArray;

        if(hardwareAccelerationType == HardwareAccelerationType.H264_OMX){
            if(channelType == ChannelType.GR) {
                commandArray = new String[]{
                        chukasaModel.getSystemConfiguration().getRecxxxPath(),
                        "--b25", "--strip",
                        Integer.toString(chukasaModel.getChukasaSettings().getPhysicalLogicalChannel()),
                        "-", "-",
                        "|",
                        chukasaModel.getSystemConfiguration().getFfmpegPath(),
                        "-i", "-",
                        "-acodec", "copy",
                        //"-acodec", "aac",
                        //"-ab", chukasaModel.getChukasaSettings().getAudioBitrate() + "k",
                        //"-ar", "44100",
                        //"-ac", "2",
                        "-s", chukasaModel.getChukasaSettings().getVideoResolution(),
                        "-c:v", "h264_omx",
                        //"-vcodec", "h264_qsv",
                        //"-g", "60",
                        //"-profile:v", "high",
                        //"-level", "4.2",
                        //"-b:v", chukasaModel.getChukasaSettings().getVideoBitrate()+"k",
                        "-threads", Integer.toString(chukasaModel.getSystemConfiguration().getFfmpegThreads()),
                        "-f", "segment",
                        "-segment_format", "mpegts",
                        "-segment_time", Integer.toString(chukasaModel.getHlsConfiguration().getDuration()),
//                    "-segment_list", m3u8OutputPath,
                        ffmpegOutputPath
                };
            } else {
                commandArray = new String[]{
                        chukasaModel.getSystemConfiguration().getRecxxxPath(),
                        "--b25", "--strip", "--dev", "1", "--lch",
                        Integer.toString(chukasaModel.getChukasaSettings().getPhysicalLogicalChannel()),
                        "-", "-",
                        "|",
                        chukasaModel.getSystemConfiguration().getFfmpegPath(),
                        "-i", "-",
                        "-acodec", "copy",
                        //"-acodec", "aac",
                        //"-ab", chukasaModel.getChukasaSettings().getAudioBitrate() + "k",
                        //"-ar", "44100",
                        //"-ac", "2",
                        "-s", chukasaModel.getChukasaSettings().getVideoResolution(),
                        "-c:v", "h264_omx",
                        //"-vcodec", "h264_qsv",
                        //"-g", "60",
                        //"-profile:v", "high",
                        //"-level", "4.2",
                        //"-b:v", chukasaModel.getChukasaSettings().getVideoBitrate()+"k",
                        "-threads", Integer.toString(chukasaModel.getSystemConfiguration().getFfmpegThreads()),
                        "-f", "segment",
                        "-segment_format", "mpegts",
                        "-segment_time", Integer.toString(chukasaModel.getHlsConfiguration().getDuration()),
//                    "-segment_list", m3u8OutputPath,
                        ffmpegOutputPath
                };
            }
        } else if(hardwareAccelerationType == HardwareAccelerationType.H264_QSV){
            commandArray = new String[]{
                    chukasaModel.getSystemConfiguration().getRecxxxPath(),
                    "--b25", "--strip",
                    Integer.toString(chukasaModel.getChukasaSettings().getPhysicalLogicalChannel()),
                    "-", "-",
                    "|",
                    chukasaModel.getSystemConfiguration().getFfmpegPath(),
                    "-i", "-",
                    "-acodec", "aac",
                    "-ab", chukasaModel.getChukasaSettings().getAudioBitrate() + "k",
                    "-ar", "48000",
                    "-ac", "2",
                    "-s", chukasaModel.getChukasaSettings().getVideoResolution(),
                    "-vcodec", "h264_qsv",
                    "-g", "60",
                    "-profile:v", "high",
                    "-level", "4.2",
                    "-b:v", chukasaModel.getChukasaSettings().getVideoBitrate()+"k",
                    "-threads", Integer.toString(chukasaModel.getSystemConfiguration().getFfmpegThreads()),
                    "-f", "segment",
                    "-segment_format", "mpegts",
                    "-segment_time", Integer.toString(chukasaModel.getHlsConfiguration().getDuration()),
//                    "-segment_list", m3u8OutputPath,
                    ffmpegOutputPath
            };
        }else if(hardwareAccelerationType == HardwareAccelerationType.H264) {
            commandArray = new String[]{
                    chukasaModel.getSystemConfiguration().getRecxxxPath(),
                    "--b25", "--strip",
                    Integer.toString(chukasaModel.getChukasaSettings().getPhysicalLogicalChannel()),
                    "-", "-",
                    "|",
                    chukasaModel.getSystemConfiguration().getFfmpegPath(),
                    "-i", "-",
                    "-acodec", "aac",
                    "-ab", chukasaModel.getChukasaSettings().getAudioBitrate() + "k",
                    "-ar", "48000",
                    "-ac", "2",
                    "-s", chukasaModel.getChukasaSettings().getVideoResolution(),
                    "-vcodec", "libx264",
                    "-profile:v", "high",
                    "-level", "4.1",
                    "-b:v", chukasaModel.getChukasaSettings().getVideoBitrate() + "k",
                    "-preset:v", "superfast",
                    "-threads", Integer.toString(chukasaModel.getSystemConfiguration().getFfmpegThreads()),
                    "-f", "segment",
                    "-segment_format", "mpegts",
                    "-segment_time", Integer.toString(chukasaModel.getHlsConfiguration().getDuration()),
//                    "-segment_list", m3u8OutputPath,
                    "-x264opts", "keyint=10:min-keyint=10",
                    ffmpegOutputPath
            };
        } else if(hardwareAccelerationType == HardwareAccelerationType.H264_NVENC){
            if(channelType == ChannelType.GR) {
                if(recxxxDriverType == RecxxxDriverType.DVB) {
                    commandArray = new String[]{
                            chukasaModel.getSystemConfiguration().getRecxxxPath(),
                            "--dev", Integer.toString(tunerStatusGR.getIndex()),
                            Integer.toString(chukasaModel.getChukasaSettings().getPhysicalLogicalChannel()),
                            "-", "-",
                            "|",
                            chukasaModel.getSystemConfiguration().getFfmpegPath(),
                            "-i", "-",
                            "-acodec", "aac",
                            "-ab", chukasaModel.getChukasaSettings().getAudioBitrate() + "k",
                            "-ar", "48000",
                            "-ac", "2",
                            "-s", chukasaModel.getChukasaSettings().getVideoResolution(),
                            "-vcodec", "h264_nvenc",
                            "-g", "10",
                            //"-profile:v", "high",
                            //"-level", "4.2",
                            "-b:v", chukasaModel.getChukasaSettings().getVideoBitrate() + "k",
                            "-threads", Integer.toString(chukasaModel.getSystemConfiguration().getFfmpegThreads()),
                            "-f", "hls",
                            "-hls_time", Integer.toString(chukasaModel.getHlsConfiguration().getDuration()),
//                    "-segment_list", m3u8OutputPath,
                            "-hls_segment_filename", ffmpegOutputPath,
                            ffmpegM3U8OutputPath
//                    chukasaModel.getSystemConfiguration().getHaryukaPath(),
//                    "/dev/dvb/adapter5", // TODO:
//                    Integer.toString(frequency),
//                    "0", "-",
//                    "|",
//                    chukasaModel.getSystemConfiguration().getFfmpegPath(),
//                    "-i", "-",
//                    "-acodec", "aac",
//                    "-ab", chukasaModel.getChukasaSettings().getAudioBitrate() + "k",
//                    "-ar", "48000",
//                    "-ac", "2",
//                    "-s", chukasaModel.getChukasaSettings().getVideoResolution(),
//                    "-vcodec", "h264_nvenc",
//                    "-g", "10",
//                    "-b:v", chukasaModel.getChukasaSettings().getVideoBitrate()+"k",
//                    "-threads", Integer.toString(chukasaModel.getSystemConfiguration().getFfmpegThreads()),
//                    "-f", "hls",
//                    "-hls_time", Integer.toString(chukasaModel.getHlsConfiguration().getDuration()),
////                    "-segment_list", m3u8OutputPath,
//                    "-hls_segment_filename", ffmpegOutputPath,
//                    ffmpegM3U8OutputPath
                    };
                }else if(recxxxDriverType == RecxxxDriverType.CHARDEV){
                    commandArray = new String[]{
                            chukasaModel.getSystemConfiguration().getRecxxxPath(),
                            Integer.toString(chukasaModel.getChukasaSettings().getPhysicalLogicalChannel()),
                            "-", "-",
                            "|",
                            chukasaModel.getSystemConfiguration().getFfmpegPath(),
                            "-i", "-",
                            "-acodec", "aac",
                            "-ab", chukasaModel.getChukasaSettings().getAudioBitrate() + "k",
                            "-ar", "48000",
                            "-ac", "2",
                            "-s", chukasaModel.getChukasaSettings().getVideoResolution(),
                            "-vcodec", "h264_nvenc",
                            "-g", "10",
                            "-b:v", chukasaModel.getChukasaSettings().getVideoBitrate() + "k",
                            "-threads", Integer.toString(chukasaModel.getSystemConfiguration().getFfmpegThreads()),
                            "-f", "hls",
                            "-hls_time", Integer.toString(chukasaModel.getHlsConfiguration().getDuration()),
                            "-hls_segment_filename", ffmpegOutputPath,
                            ffmpegM3U8OutputPath
                    };
                }else{
                    commandArray = new String[]{};
                }
            }else if(channelType == ChannelType.BS){
                if(recxxxDriverType == RecxxxDriverType.DVB){
                    commandArray = new String[]{
                            chukasaModel.getSystemConfiguration().getRecxxxPath(),
                            "--dev", Integer.toString(tunerStatusBS.getIndex()),
                            "--tsid", recdvbBSModel.getTsid(), recdvbBSModel.getName(),
                            Integer.toString(chukasaModel.getChukasaSettings().getPhysicalLogicalChannel()),
                            "-", "-",
                            "|",
                            chukasaModel.getSystemConfiguration().getFfmpegPath(),
                            "-i", "-",
                            "-acodec", "aac",
                            "-ab", chukasaModel.getChukasaSettings().getAudioBitrate() + "k",
                            "-ar", "48000",
                            "-ac", "2",
                            "-s", chukasaModel.getChukasaSettings().getVideoResolution(),
                            "-vcodec", "h264_nvenc",
                            "-g", "10",
                            "-b:v", chukasaModel.getChukasaSettings().getVideoBitrate() + "k",
                            "-threads", Integer.toString(chukasaModel.getSystemConfiguration().getFfmpegThreads()),
                            "-f", "hls",
                            "-hls_time", Integer.toString(chukasaModel.getHlsConfiguration().getDuration()),
                            "-hls_segment_filename", ffmpegOutputPath,
                            ffmpegM3U8OutputPath
                    };
                }else if(recxxxDriverType == RecxxxDriverType.CHARDEV) {
                    commandArray = new String[]{
                            chukasaModel.getSystemConfiguration().getRecxxxPath(),
                            Integer.toString(chukasaModel.getChukasaSettings().getPhysicalLogicalChannel()),
                            "-", "-",
                            "|",
                            chukasaModel.getSystemConfiguration().getFfmpegPath(),
                            "-i", "-",
                            "-acodec", "aac",
                            "-ab", chukasaModel.getChukasaSettings().getAudioBitrate() + "k",
                            "-ar", "48000",
                            "-ac", "2",
                            "-s", chukasaModel.getChukasaSettings().getVideoResolution(),
                            "-vcodec", "h264_nvenc",
                            "-g", "10",
                            "-b:v", chukasaModel.getChukasaSettings().getVideoBitrate() + "k",
                            "-threads", Integer.toString(chukasaModel.getSystemConfiguration().getFfmpegThreads()),
                            "-f", "hls",
                            "-hls_time", Integer.toString(chukasaModel.getHlsConfiguration().getDuration()),
                            "-hls_segment_filename", ffmpegOutputPath,
                            ffmpegM3U8OutputPath
                    };
                }else{
                    commandArray = new String[]{};
                }
            }else{
                commandArray = new String[]{};
            }
        } else {
            commandArray = new String[]{};
        }

        String command = "";
        for(int i = 0; i < commandArray.length; i++){
            command += commandArray[i] + " ";
        }
        log.info("{}", command);

        final String captureShell = chukasaModel.getSystemConfiguration().getTemporaryPath() + FILE_SEPARATOR + "capture.sh";
        File file = new File(captureShell);
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) {
            bufferedWriter.write("#!/bin/bash");
            bufferedWriter.newLine();
            bufferedWriter.write(command);
        } catch (IOException e) {
            log.error("{} {}", e.getMessage(), e);
        }

        // chmod 755 capture.sh
        if(true){
            final String[] chmodCommandArray = {"chmod", "755", captureShell};
            final ProcessBuilder processBuilder = new ProcessBuilder(chmodCommandArray);
            try {
                final Process process = processBuilder.start();
                final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String str = "";
                while((str = bufferedReader.readLine()) != null){
                    log.debug("{}", str);
                }
                process.getInputStream().close();
                process.getErrorStream().close();
                process.getOutputStream().close();
                bufferedReader.close();
                process.destroy();
            } catch (IOException e) {
                log.error("{} {}", e.getMessage(), e);
            }
        }

        // run capture.sh
        if(true){
            final String[] capureCommandArray = {captureShell};
            final ProcessBuilder processBuilder = new ProcessBuilder(capureCommandArray);
            try {
                final Process process = processBuilder.start();
                final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

                // TODO: sh だから意味無し
                long pid = -1;
                try {
                    if (process.getClass().getName().equals("java.lang.UNIXProcess")) {
                        final Field field = process.getClass().getDeclaredField("pid");
                        field.setAccessible(true);
                        pid = field.getLong(process);
                        chukasaModel.setFfmpegPID(pid);
                        chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);
                        field.setAccessible(false);
                    }
                } catch (Exception e) {
                    log.error("{} {}", e.getMessage(), e);
                }

                String str = "";
                boolean isTranscoding = false;
                while((str = bufferedReader.readLine()) != null){
                    log.debug("{}", str);
                    if(str.startsWith("frame=")){
                        if(!isTranscoding){
                            isTranscoding = true;
                            chukasaModel.setTrascoding(isTranscoding);
                            chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);
                        }
                    }
                }
                isTranscoding = false;
                chukasaModel.setTrascoding(isTranscoding);
                chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);
                process.getInputStream().close();
                process.getErrorStream().close();
                process.getOutputStream().close();
                bufferedReader.close();
                process.destroy();
            } catch (IOException e) {
                log.error("{} {}", e.getMessage(), e);
            }
        }
        return null;
    }

    @Override
    public void execute(int adaptiveBitrateStreaming) {

    }

    @Override
    public void cancel(int adaptiveBitrateStreaming) {
        ChukasaModel chukasaModel = chukasaModelManagementComponent.get(adaptiveBitrateStreaming);
        tunerManagementService.update(chukasaModel.getTunerDeviceName(), true);
        chukasaModel.setTunerDeviceName("");
        chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);
    }
}
