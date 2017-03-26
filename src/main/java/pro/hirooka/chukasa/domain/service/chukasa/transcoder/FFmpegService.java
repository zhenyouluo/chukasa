package pro.hirooka.chukasa.domain.service.chukasa.transcoder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import pro.hirooka.chukasa.domain.model.chukasa.ChukasaModel;
import pro.hirooka.chukasa.domain.model.chukasa.constants.ChukasaConstant;
import pro.hirooka.chukasa.domain.model.chukasa.enums.HardwareAccelerationType;
import pro.hirooka.chukasa.domain.model.chukasa.enums.StreamingType;
import pro.hirooka.chukasa.domain.service.chukasa.IChukasaModelManagementComponent;
import pro.hirooka.chukasa.domain.service.chukasa.encrypter.IChukasaHLSEncrypter;
import pro.hirooka.chukasa.domain.service.chukasa.playlister.IPlaylistBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.concurrent.Future;

import static java.util.Objects.requireNonNull;

@Slf4j
@Service
public class FFmpegService implements IFFmpegService {

    final String FILE_SEPARATOR = ChukasaConstant.FILE_SEPARATOR;
    final String STREAM_FILE_NAME_PREFIX = ChukasaConstant.STREAM_FILE_NAME_PREFIX;
    final String STREAM_FILE_EXTENSION = ChukasaConstant.STREAM_FILE_EXTENSION;

    private final IChukasaModelManagementComponent chukasaModelManagementComponent;

    @Autowired
    IChukasaHLSEncrypter chukasaHLSEncrypter;
    @Autowired
    IPlaylistBuilder playlistBuilder;

    @Autowired
    public FFmpegService(IChukasaModelManagementComponent chukasaModelManagementComponent) {
        this.chukasaModelManagementComponent = requireNonNull(chukasaModelManagementComponent, "chukasaModelManagementComponent");
    }

    @Override
    public Future<Integer> submit(int adaptiveBitrateStreaming) {

        // TODO: chukasaModel final
        ChukasaModel chukasaModel = chukasaModelManagementComponent.get(adaptiveBitrateStreaming);
        final HardwareAccelerationType hardwareAccelerationType = chukasaModel.getHardwareAccelerationType();
        final boolean canEncrypt = chukasaModel.getChukasaSettings().isCanEncrypt();
        final String ffmpegOutputPath;
        if (canEncrypt) {
            ffmpegOutputPath = chukasaModel.getTempEncPath() + FILE_SEPARATOR + STREAM_FILE_NAME_PREFIX + "%d" + STREAM_FILE_EXTENSION;
        } else {
            ffmpegOutputPath = chukasaModel.getStreamPath() + FILE_SEPARATOR + STREAM_FILE_NAME_PREFIX + "%d" + STREAM_FILE_EXTENSION;
        }

        //final int seqCapturedTimeShifted = chukasaModel.getSeqTsOkkake();
        final int sequenceMediaSegment = chukasaModel.getSequenceMediaSegment();

        // TODO: custom command from properties
        final String[] commandArray;

        if (chukasaModel.getChukasaSettings().getStreamingType() == StreamingType.WEBCAM) {

            if (hardwareAccelerationType == HardwareAccelerationType.H264_OMX) {
                commandArray = new String[]{

                        chukasaModel.getSystemConfiguration().getFfmpegPath(),
                        "-f", "video4linux2",
                        "-s", chukasaModel.getChukasaSettings().getVideoResolution(),
                        "-i", chukasaModel.getSystemConfiguration().getWebcamDeviceName(),
                        "-f", "alsa",
                        "-ac", Integer.toString(chukasaModel.getSystemConfiguration().getWebcamAudioChannel()),
                        "-i", "hw:0,0",
                        "-acodec", "aac",
                        "-ab", chukasaModel.getChukasaSettings().getAudioBitrate() + "k",
                        "-ar", "44100",
                        "-s", chukasaModel.getChukasaSettings().getVideoResolution(),
                        "-vcodec", "h264_omx",
                        //"-g", "60",
                        //"-profile:v", "high",
                        //"-level", "4.2",
                        "-b:v", chukasaModel.getChukasaSettings().getVideoBitrate() + "k",
                        "-pix_fmt", "yuv420p",
                        "-threads", Integer.toString(chukasaModel.getSystemConfiguration().getFfmpegThreads()),
                        "-f", "segment",
                        "-segment_format", "mpegts",
                        "-segment_time", Integer.toString(chukasaModel.getHlsConfiguration().getDuration()),
//                        "-segment_list", m3u8OutputPath,
                        ffmpegOutputPath
                };
            } else if (hardwareAccelerationType == HardwareAccelerationType.H264_QSV) {
                commandArray = new String[]{

                        chukasaModel.getSystemConfiguration().getFfmpegPath(),
                        "-f", "video4linux2",
                        "-s", chukasaModel.getChukasaSettings().getVideoResolution(),
                        "-i", chukasaModel.getSystemConfiguration().getWebcamDeviceName(),
                        "-f", "alsa",
                        "-ac", Integer.toString(chukasaModel.getSystemConfiguration().getWebcamAudioChannel()),
                        "-i", "hw:0,0",
                        "-acodec", "aac",
                        "-ab", chukasaModel.getChukasaSettings().getAudioBitrate() + "k",
                        "-ar", "44100",
                        "-s", chukasaModel.getChukasaSettings().getVideoResolution(),
                        "-vcodec", "h264_qsv",
                        "-g", "60",
                        "-profile:v", "high",
                        "-level", "4.2",
                        "-b:v", chukasaModel.getChukasaSettings().getVideoBitrate() + "k",
                        "-pix_fmt", "yuv420p",
                        "-threads", Integer.toString(chukasaModel.getSystemConfiguration().getFfmpegThreads()),
                        "-f", "segment",
                        "-segment_format", "mpegts",
                        "-segment_time", Integer.toString(chukasaModel.getHlsConfiguration().getDuration()),
//                        "-segment_list", m3u8OutputPath,
                        ffmpegOutputPath
                };
            } else if (hardwareAccelerationType == HardwareAccelerationType.H264) {
                commandArray = new String[]{

                        chukasaModel.getSystemConfiguration().getFfmpegPath(),
                        "-f", "video4linux2",
                        "-s", chukasaModel.getChukasaSettings().getVideoResolution(),
                        //"-r", "30",
                        "-i", chukasaModel.getSystemConfiguration().getWebcamDeviceName(),
                        "-f", "alsa",
                        "-ac", Integer.toString(chukasaModel.getSystemConfiguration().getWebcamAudioChannel()),
                        "-i", "hw:0,0",
                        "-acodec", "aac",
                        "-ab", chukasaModel.getChukasaSettings().getAudioBitrate() + "k",
                        "-ar", "44100",
                        "-s", chukasaModel.getChukasaSettings().getVideoResolution(),
                        "-vcodec", "libx264",
                        "-profile:v", "high",
                        "-level", "4.1",
                        "-preset:v", "superfast",
                        "-b:v", chukasaModel.getChukasaSettings().getVideoBitrate() + "k",
                        "-pix_fmt", "yuv420p",
                        "-threads", Integer.toString(chukasaModel.getSystemConfiguration().getFfmpegThreads()),
                        "-x264opts", "keyint=10:min-keyint=10",
                        "-f", "segment",
                        "-segment_format", "mpegts",
                        "-segment_time", Integer.toString(chukasaModel.getHlsConfiguration().getDuration()),
                        ffmpegOutputPath
                };
            } else {
                commandArray = new String[]{};
            }

        } else if (chukasaModel.getChukasaSettings().getStreamingType() == StreamingType.FILE) {

            if (hardwareAccelerationType == HardwareAccelerationType.H264_OMX) {
                String[] cmdArrayTemporary = {

                        chukasaModel.getSystemConfiguration().getFfmpegPath(),
                        "-i", chukasaModel.getSystemConfiguration().getFilePath() + FILE_SEPARATOR + chukasaModel.getChukasaSettings().getFileName(),
                        "-acodec", "aac",
                        //"-ab", chukasaModel.getChukasaSettings().getAudioBitrate() + "k",
                        //"-ac", "2",
                        //"-ar", "44100",
                        //"-s", chukasaModel.getChukasaSettings().getVideoResolution(),
                        "-vcodec", "h264_omx",
                        //"-b:v", chukasaModel.getChukasaSettings().getVideoBitrate() + "k",
                        "-threads", Integer.toString(chukasaModel.getSystemConfiguration().getFfmpegThreads()),
                        "-f", "segment",
                        "-segment_format", "mpegts",
                        "-segment_time", Integer.toString(chukasaModel.getHlsConfiguration().getDuration()),
                        ffmpegOutputPath
                };
                commandArray = cmdArrayTemporary;
            } else if (hardwareAccelerationType == HardwareAccelerationType.H264_QSV) {
                commandArray = new String[]{

                        chukasaModel.getSystemConfiguration().getFfmpegPath(),
                        "-i", chukasaModel.getSystemConfiguration().getFilePath() + FILE_SEPARATOR + chukasaModel.getChukasaSettings().getFileName(),
                        "-acodec", "aac",
                        "-ab", chukasaModel.getChukasaSettings().getAudioBitrate() + "k",
                        "-ac", "2",
                        "-ar", "44100",
                        "-s", chukasaModel.getChukasaSettings().getVideoResolution(),
                        "-vcodec", "h264_qsv",
                        "-profile:v", "high",
                        "-level", "4.1",
                        "-b:v", chukasaModel.getChukasaSettings().getVideoBitrate() + "k",
                        "-threads", Integer.toString(chukasaModel.getSystemConfiguration().getFfmpegThreads()),
                        "-f", "segment",
                        "-segment_format", "mpegts",
                        "-segment_time", Integer.toString(chukasaModel.getHlsConfiguration().getDuration()),
                        ffmpegOutputPath
                };
            } else if (hardwareAccelerationType == HardwareAccelerationType.H264) {
                commandArray = new String[]{

                        chukasaModel.getSystemConfiguration().getFfmpegPath(),
                        "-i", chukasaModel.getSystemConfiguration().getFilePath() + FILE_SEPARATOR + chukasaModel.getChukasaSettings().getFileName(),
                        "-acodec", "aac",
                        "-ab", chukasaModel.getChukasaSettings().getAudioBitrate() + "k",
                        "-ac", "2",
                        "-ar", "44100",
                        "-s", chukasaModel.getChukasaSettings().getVideoResolution(),
                        "-vcodec", "libx264",
                        "-profile:v", "high",
                        "-level", "4.1",
                        "-preset:v", "superfast",
                        "-b:v", chukasaModel.getChukasaSettings().getVideoBitrate() + "k",
                        "-threads", Integer.toString(chukasaModel.getSystemConfiguration().getFfmpegThreads()),
                        "-x264opts", "keyint=10:min-keyint=10",
                        "-f", "segment",
                        "-segment_format", "mpegts",
                        "-segment_time", Integer.toString(chukasaModel.getHlsConfiguration().getDuration()),
                        ffmpegOutputPath
                };
            } else {
                commandArray = new String[]{};
            }

        } else if (chukasaModel.getChukasaSettings().getStreamingType() == StreamingType.OKKAKE) {

            if (chukasaModel.getChukasaSettings().isCanEncrypt()) {

                if (hardwareAccelerationType == HardwareAccelerationType.H264_QSV) {
                    commandArray = new String[]{

                            chukasaModel.getSystemConfiguration().getFfmpegPath(),
                            "-i", chukasaModel.getTempEncPath() + FILE_SEPARATOR + STREAM_FILE_NAME_PREFIX + sequenceMediaSegment + STREAM_FILE_EXTENSION,
                            "-acodec", "aac",
                            "-ab", chukasaModel.getChukasaSettings().getAudioBitrate() + "k",
                            "-ac", "2",
                            "-ar", "44100",
                            "-s", chukasaModel.getChukasaSettings().getVideoResolution(),
                            "-vcodec", "h264_qsv",
                            "-profile:v", "high",
                            "-level", "4.1",
                            "-b:v", chukasaModel.getChukasaSettings().getVideoBitrate() + "k",
                            "-threads", Integer.toString(chukasaModel.getSystemConfiguration().getFfmpegThreads()),
                            "-f", "mpegts",
                            "-y", chukasaModel.getTempEncPath() + FILE_SEPARATOR + "fileSequenceEncoded" + sequenceMediaSegment + STREAM_FILE_EXTENSION // TODO
                    };
                } else if (hardwareAccelerationType == HardwareAccelerationType.H264) {
                    commandArray = new String[]{

                            chukasaModel.getSystemConfiguration().getFfmpegPath(),
                            "-i", chukasaModel.getTempEncPath() + FILE_SEPARATOR + STREAM_FILE_NAME_PREFIX + sequenceMediaSegment + STREAM_FILE_EXTENSION,
                            "-acodec", "aac",
                            "-ab", chukasaModel.getChukasaSettings().getAudioBitrate() + "k",
                            "-ac", "2",
                            "-ar", "44100",
                            "-s", chukasaModel.getChukasaSettings().getVideoResolution(),
                            "-vcodec", "libx264",
                            "-profile:v", "high",
                            "-level", "4.1",
                            "-preset:v", "superfast",
                            "-b:v", chukasaModel.getChukasaSettings().getVideoBitrate() + "k",
                            "-threads", Integer.toString(chukasaModel.getSystemConfiguration().getFfmpegThreads()),
                            "-f", "mpegts",
                            "-x264opts", "keyint=10:min-keyint=10",
                            "-y", chukasaModel.getTempEncPath() + FILE_SEPARATOR + "fileSequenceEncoded" + sequenceMediaSegment + STREAM_FILE_EXTENSION // TODO
                    };
                } else {
                    commandArray = new String[]{};
                }

            } else {

                if (hardwareAccelerationType == HardwareAccelerationType.H264_QSV) {
                    commandArray = new String[]{

                            chukasaModel.getSystemConfiguration().getFfmpegPath(),
                            "-i", chukasaModel.getTempEncPath() + FILE_SEPARATOR + STREAM_FILE_NAME_PREFIX + sequenceMediaSegment + STREAM_FILE_EXTENSION,
                            "-acodec", "aac",
                            "-ab", chukasaModel.getChukasaSettings().getAudioBitrate() + "k",
                            "-ac", "2",
                            "-ar", "44100",
                            "-s", chukasaModel.getChukasaSettings().getVideoResolution(),
                            "-vcodec", "h264_qsv",
                            "-profile:v", "high",
                            "-level", "4.1",
                            "-b:v", chukasaModel.getChukasaSettings().getVideoBitrate() + "k",
                            "-threads", Integer.toString(chukasaModel.getSystemConfiguration().getFfmpegThreads()),
                            "-f", "mpegts",
                            "-y", chukasaModel.getStreamPath() + FILE_SEPARATOR + STREAM_FILE_NAME_PREFIX + sequenceMediaSegment + STREAM_FILE_EXTENSION
                    };
                } else if (hardwareAccelerationType == HardwareAccelerationType.H264) {
                    commandArray = new String[]{

                            chukasaModel.getSystemConfiguration().getFfmpegPath(),
                            "-i", chukasaModel.getTempEncPath() + FILE_SEPARATOR + STREAM_FILE_NAME_PREFIX + sequenceMediaSegment + STREAM_FILE_EXTENSION,
                            "-acodec", "aac",
                            "-ab", chukasaModel.getChukasaSettings().getAudioBitrate() + "k",
                            "-ac", "2",
                            "-ar", "44100",
                            "-s", chukasaModel.getChukasaSettings().getVideoResolution(),
                            "-vcodec", "libx264",
                            "-profile:v", "high",
                            "-level", "4.1",
                            "-preset:v", "superfast",
                            "-b:v", chukasaModel.getChukasaSettings().getVideoBitrate() + "k",
                            "-threads", Integer.toString(chukasaModel.getSystemConfiguration().getFfmpegThreads()),
                            "-f", "mpegts",
                            "-x264opts", "keyint=10:min-keyint=10",
                            "-y", chukasaModel.getStreamPath() + FILE_SEPARATOR + STREAM_FILE_NAME_PREFIX + sequenceMediaSegment + STREAM_FILE_EXTENSION
                    };
                } else {
                    commandArray = new String[]{};
                }

            }

        } else {
            commandArray = new String[]{};
        }

        String command = "";
        for(int i = 0; i < commandArray.length; i++){
            command += commandArray[i] + " ";
        }
        log.info("{}", command);

        ProcessBuilder processBuilder = new ProcessBuilder(commandArray);
        try {
            Process process = processBuilder.start();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            if (process.getClass().getName().equals("java.lang.UNIXProcess")) {
                Field field = process.getClass().getDeclaredField("pid");
                field.setAccessible(true);
                long pid = field.getLong(process);
                // TODO: final
                chukasaModel = chukasaModelManagementComponent.get(adaptiveBitrateStreaming);
                chukasaModel.setFfmpegPID(pid);
                chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);
                field.setAccessible(false);
            }

            String str;
            boolean isTranscoding = false;
            while ((str = bufferedReader.readLine()) != null) {
                log.debug(str);
                // TODO Input/output error (in use...)
                if (chukasaModel.getChukasaSettings().getStreamingType() == StreamingType.WEBCAM || chukasaModel.getChukasaSettings().getStreamingType() == StreamingType.FILE) {
                    if (str.startsWith("frame=")) {
                        if (!isTranscoding) {
                            isTranscoding = true;
                            // TODO: final
                            chukasaModel = chukasaModelManagementComponent.get(adaptiveBitrateStreaming);
                            chukasaModel.setTrascoding(true);
                            chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);
                        }
                    }
                }
            }
            chukasaModel = chukasaModelManagementComponent.get(adaptiveBitrateStreaming);
            chukasaModel.setTrascoding(false);
            chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);
            process.getInputStream().close();
            process.getErrorStream().close();
            process.getOutputStream().close();
            bufferedReader.close();
            process.destroy();

            if(chukasaModel.getChukasaSettings().getStreamingType() == StreamingType.OKKAKE){
                if(chukasaModel.getChukasaSettings().isCanEncrypt()){
                    chukasaHLSEncrypter.encrypt();
                }else{
                    playlistBuilder.build();
                }
            }

            if (chukasaModel.getChukasaSettings().getStreamingType() == StreamingType.FILE) {
                int sequenceLastMediaSegment = -1;
                chukasaModel = chukasaModelManagementComponent.get(adaptiveBitrateStreaming);
                if (chukasaModel.getChukasaSettings().isCanEncrypt()) {
                    String encryptedStreamTemporaryPath = chukasaModel.getTempEncPath();
                    File[] temporaryfiles = new File(encryptedStreamTemporaryPath).listFiles();
                    assert temporaryfiles != null;
                    for (File file : temporaryfiles) {
                        log.info(file.getName());
                        String fileName = file.getName();
                        if(fileName.startsWith(ChukasaConstant.STREAM_FILE_NAME_PREFIX)
                                && fileName.endsWith(ChukasaConstant.STREAM_FILE_EXTENSION)){
                            log.info("... {}", fileName.split(ChukasaConstant.M3U8_FILE_NAME)[1]);
                            String sequenceString = fileName.split(ChukasaConstant.M3U8_FILE_NAME)[1].split(ChukasaConstant.STREAM_FILE_EXTENSION)[0];
                            log.info(sequenceString);
                            int sequence = Integer.parseInt(sequenceString);
                            if(sequence > sequenceLastMediaSegment){
                                sequenceLastMediaSegment = sequence;
                            }
                        }
                    }
                }else{
                    String streamPath = chukasaModel.getStreamPath();
                    File[] files = new File(streamPath).listFiles();
                    assert files != null;
                    for (File file : files){
                        log.info(file.getName());
                    }
                }
                log.info("sequenceLastMediaSegment = {}.", sequenceLastMediaSegment);
                chukasaModel.setSeqTsLast(sequenceLastMediaSegment);
                chukasaModel.setSequenceLastMediaSegment(sequenceLastMediaSegment);
                chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);
                return new AsyncResult<>(sequenceLastMediaSegment);
            }

        } catch (IOException | IllegalAccessException | NoSuchFieldException e) {
            log.error("{}", e.getMessage());
        }
        return null;
    }

    @Override
    public void cancel(int adaptiveBitrateStreaming) {

    }
}
