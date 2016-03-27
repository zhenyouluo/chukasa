package pro.hirooka.chukasa.capture;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import pro.hirooka.chukasa.domain.ChukasaModel;
import pro.hirooka.chukasa.segmenter.SegmenterRunner;
import pro.hirooka.chukasa.service.IChukasaModelManagementComponent;

import java.io.*;

import static java.util.Objects.requireNonNull;

@Slf4j
public class CaptureRunner implements Runnable {

    static final String FILE_SEPARATOR = System.getProperty("file.separator");

    private int adaptiveBitrateStreaming;

    private final IChukasaModelManagementComponent chukasaModelManagementComponent;

    @Autowired
    public CaptureRunner(int adaptiveBitrateStreaming, IChukasaModelManagementComponent chukasaModelManagementComponent) {
        this.adaptiveBitrateStreaming = adaptiveBitrateStreaming;
        this.chukasaModelManagementComponent = requireNonNull(chukasaModelManagementComponent, "chukasaModelManagementComponent");
    }

    @Override
    public void run() {

        ChukasaModel chukasaModel = chukasaModelManagementComponent.get(adaptiveBitrateStreaming);

        String[] cmdArray = null;

        String[] cmdArrayBase = {
                chukasaModel.getSystemConfiguration().getCaptureProgramPath(),
                "--b25", "--strip",
                Integer.toString(chukasaModel.getChukasaSettings().getCh()),
                "-", "-",
                "|",
                chukasaModel.getSystemConfiguration().getCaptureFfmpegPath(),
                "-i", "-",
                "-acodec", "libfdk_aac",
                "-ab", chukasaModel.getChukasaSettings().getAudioBitrate() + "k",
                "-ar", "44100",
                "-ac", "2",
                "-s", chukasaModel.getChukasaSettings().getVideoResolutionType().getName(),
                "-vcodec", "libx264",
                "-profile:v", "high",
                "-level", "4.2",
                "-b:v", chukasaModel.getChukasaSettings().getVideoBitrate()+"k",
                "-preset:v", "superfast",
                "-threads", Integer.toString(chukasaModel.getSystemConfiguration().getFfmpegThreads()),
                "-f", "mpegts",
                "-x264opts", "keyint=10:min-keyint=10",
                "-y", chukasaModel.getSystemConfiguration().getTempPath() + FILE_SEPARATOR + chukasaModel.getChukasaConfiguration().getStreamFileNamePrefix() + chukasaModel.getChukasaSettings().getVideoBitrate() + chukasaModel.getHlsConfiguration().getStreamExtension()
        };
        cmdArray = cmdArrayBase;

        String cmd = "";
        for(int i = 0; i < cmdArray.length; i++){
            cmd += cmdArray[i] + " ";
        }
        log.info("{}", cmd);

        String capSh = chukasaModel.getSystemConfiguration().getTempPath() + FILE_SEPARATOR + "cap.sh";
        File f = new File(capSh);
        BufferedWriter bw;
        try {
            bw = new BufferedWriter(new FileWriter(f));
            bw.write("#!/bin/bash");
            bw.newLine();
            bw.write(cmd);
            bw.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        // chmod 755 cap.sh
        if(true){
            String[] cmdArrayChmod = {"chmod", "755", capSh};
            ProcessBuilder pb = new ProcessBuilder(cmdArrayChmod);
            Process pr;
            try {
                pr = pb.start();
                //InputStream is = pr.getInputStream();
                InputStream is = pr.getErrorStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String s = "";
                while((s = br.readLine()) != null){
                    log.debug("{}", s);
                }
                br.close();
                isr.close();
                is.close();
                pr.destroy();
                pr = null;
                pb = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } // cmdArrayChmod

        // run cap.sh
        if(true){
            String[] cmdArrayCapSh = {capSh};
            ProcessBuilder pb = new ProcessBuilder(cmdArrayCapSh);
            Process pr;
            try {
                pr = pb.start();
                //InputStream is = pr.getInputStream();
                InputStream is = pr.getErrorStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String s = "";
                boolean isTranscoding = false;
                boolean isSegmenterStarted = false;
                while((s = br.readLine()) != null){
                    log.info("{}", s);
                    if(s.startsWith("frame=")){
                        if(!isTranscoding){
                            isTranscoding = true;
                            chukasaModel.setTrascoding(isTranscoding);
                            chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);
                            if(!isSegmenterStarted) {
                                isSegmenterStarted = true;
                                SegmenterRunner segmenterRunner = new SegmenterRunner(adaptiveBitrateStreaming, chukasaModelManagementComponent);
                                Thread sThread = new Thread(segmenterRunner, "__SegmenterRunner__");
                                sThread.start();
                            }
                        }
                    }
                    if(s.startsWith("pid = ")){
                        String pidString = s.split("pid = ")[1].trim();
                        int pid = Integer.parseInt(pidString);
                        chukasaModel.setFfmpegPID(pid);
                        chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);
                    }
                }
                isTranscoding = false;
                chukasaModel.setTrascoding(isTranscoding);
                chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);
                isSegmenterStarted = false;
                br.close();
                isr.close();
                is.close();
                pr.destroy();
                pr = null;
                pb = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } // cmdArrayCapSh

    }
}
