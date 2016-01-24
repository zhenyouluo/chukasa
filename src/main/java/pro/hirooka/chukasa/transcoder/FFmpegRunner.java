package pro.hirooka.chukasa.transcoder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import pro.hirooka.chukasa.domain.*;
import pro.hirooka.chukasa.domain.type.StreamingType;
import pro.hirooka.chukasa.encrypter.Encrypter;
import pro.hirooka.chukasa.service.IChukasaModelManagementComponent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static java.util.Objects.requireNonNull;

@Slf4j
public class FFmpegRunner implements Runnable {

    static final String FILE_SEPARATOR = System.getProperty("file.separator");

    private int adaptiveBitrateStreaming;

    private final IChukasaModelManagementComponent chukasaModelManagementComponent;

    @Autowired
    public FFmpegRunner(int adaptiveBitrateStreaming, IChukasaModelManagementComponent chukasaModelManagementComponent){
        this.chukasaModelManagementComponent = requireNonNull(chukasaModelManagementComponent, "chukasaModelManagementComponent");
        this.adaptiveBitrateStreaming = adaptiveBitrateStreaming;
    }

    @Override
    public void run() {

        ChukasaModel chukasaModel = chukasaModelManagementComponent.get(adaptiveBitrateStreaming);

        int seqCapturedTimeShifted = chukasaModel.getSeqTsOkkake();

        String[] cmdArray = null;

        if(chukasaModel.getChukasaSettings().getStreamingType() == StreamingType.USB_CAMERA) {

            String[] cmdArrayTemporary = {

                    chukasaModel.getSystemConfiguration().getUsbCameraFfmpegPath(),
                    "-f", "video4linux2",
                    "-s", chukasaModel.getChukasaSettings().getCaptureResolutionType().getName(),
                    "-r", "30",
                    "-i", chukasaModel.getSystemConfiguration().getUsbCameraDeviceName(),
                    "-f", "alsa",
                    "-i", "hw:0,0",
                    "-acodec", "aac", "-strict", "experimental",
                    "-ab", chukasaModel.getChukasaSettings().getAudioBitrate() + "k",
                    "-ac", "2",
                    "-ar", "44100",
                    "-s", chukasaModel.getChukasaSettings().getVideoResolutionType().getName(),
                    "-vcodec", "libx264",
                    "-profile:v", "high",
                    "-level", "4.2",
                    "-preset", "ultrafast",
                    "-b:v", chukasaModel.getChukasaSettings().getVideoBitrate() + "k",
                    "-pix_fmt", "yuv420p",
                    "-threads", Integer.toString(chukasaModel.getSystemConfiguration().getFfmpegThreads()),
                    "-t", Integer.toString(chukasaModel.getChukasaSettings().getTotalUSBCameraLiveduration()),
                    "-f", "mpegts",
                    "-y", chukasaModel.getSystemConfiguration().getTempPath() + FILE_SEPARATOR + chukasaModel.getChukasaConfiguration().getStreamFileNamePrefix() + chukasaModel.getChukasaSettings().getVideoBitrate() + chukasaModel.getHlsConfiguration().getStreamExtension()
            };
            cmdArray = cmdArrayTemporary;

        }else if(chukasaModel.getChukasaSettings().getStreamingType() == StreamingType.FILE){

            String[] cmdArrayTemporary = {

                    chukasaModel.getSystemConfiguration().getFileFfmpegPath(),
                    "-i", chukasaModel.getSystemConfiguration().getFilePath() + FILE_SEPARATOR + chukasaModel.getChukasaSettings().getFileName(),
                    "-acodec", "libfdk_aac",
                    "-ab", chukasaModel.getChukasaSettings().getAudioBitrate() + "k",
                    "-ac", "2",
                    "-ar", "44100",
                    "-s", chukasaModel.getChukasaSettings().getVideoResolutionType().getName(),
                    "-vcodec", "libx264",
                    "-profile:v", "high",
                    "-level", "4.2",
                    "-preset", "ultrafast",
                    "-b:v", chukasaModel.getChukasaSettings().getVideoBitrate() + "k",
                    "-threads", Integer.toString(chukasaModel.getSystemConfiguration().getFfmpegThreads()),
                    "-f", "mpegts",
                    "-y", chukasaModel.getSystemConfiguration().getTempPath() + FILE_SEPARATOR + chukasaModel.getChukasaConfiguration().getStreamFileNamePrefix() + chukasaModel.getChukasaSettings().getVideoBitrate() + chukasaModel.getHlsConfiguration().getStreamExtension()
            };
            cmdArray = cmdArrayTemporary;

        }else if(chukasaModel.getChukasaSettings().getStreamingType() == StreamingType.OKKAKE){

            if(chukasaModel.getChukasaSettings().isEncrypted()){

                String[] cmdArrayTemporary = {

                        chukasaModel.getSystemConfiguration().getFileFfmpegPath(),
                        "-i", chukasaModel.getTempEncPath() + FILE_SEPARATOR + chukasaModel.getChukasaConfiguration().getStreamFileNamePrefix() + seqCapturedTimeShifted + chukasaModel.getHlsConfiguration().getStreamExtension(),
                        "-acodec", "libfdk_aac",
                        "-ab", chukasaModel.getChukasaSettings().getAudioBitrate() + "k",
                        "-ac", "2",
                        "-ar", "44100",
                        "-s", chukasaModel.getChukasaSettings().getVideoResolutionType().getName(),
                        "-vcodec", "libx264",
                        "-profile:v", "high",
                        "-level", "4.2",
                        "-preset", "ultrafast",
                        "-b:v", chukasaModel.getChukasaSettings().getVideoBitrate() + "k",
                        "-threads", Integer.toString(chukasaModel.getSystemConfiguration().getFfmpegThreads()),
                        "-f", "mpegts",
                        "-y", chukasaModel.getTempEncPath() + FILE_SEPARATOR + "fileSequenceEncoded" + seqCapturedTimeShifted + chukasaModel.getHlsConfiguration().getStreamExtension() // TODO
                };
                cmdArray = cmdArrayTemporary;

            }else{

                // TODO:

            }

        }

        String cmd = "";
        for(int i = 0; i < cmdArray.length; i++){
            cmd += cmdArray[i] + " ";
        }
        log.info("{}", cmd);

        ProcessBuilder pb = new ProcessBuilder(cmdArray);
        try {

            log.info("Begin FFmpeg");
            Process pr = pb.start();
            InputStream is = pr.getErrorStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);

            String str = "";
            while((str = br.readLine()) != null){
                log.info(str);
                // TODO Input/output error (in use...)
            }
            br.close();
            isr.close();
            is.close();
            pr.destroy();
            //pr = null;
            //pb = null;
            log.info("End FFmpeg");
            log.info("{} is completed.", this.getClass().getName());

            if(chukasaModel.getChukasaSettings().getStreamingType() == StreamingType.OKKAKE){
                if(chukasaModel.getChukasaSettings().isEncrypted()){
                    seqCapturedTimeShifted = seqCapturedTimeShifted + 1;
                    chukasaModel.setSeqTsOkkake(seqCapturedTimeShifted);
                    chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);

                    Encrypter encrypter = new Encrypter(adaptiveBitrateStreaming, chukasaModelManagementComponent);
                    Thread thread = new Thread(encrypter);
                    thread.start();
                }
            }

        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }
}