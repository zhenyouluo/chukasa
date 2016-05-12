package pro.hirooka.chukasa.transcoder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import pro.hirooka.chukasa.domain.ChukasaModel;
import pro.hirooka.chukasa.service.IChukasaModelManagementComponent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

@Slf4j
public class FFmpegStopper implements Runnable {

    private final IChukasaModelManagementComponent chukasaModelManagementComponent;

    @Autowired
    public FFmpegStopper(IChukasaModelManagementComponent chukasaModelManagementComponent) {
        this.chukasaModelManagementComponent = requireNonNull(chukasaModelManagementComponent, "chukasaModelManagementComponent");
    }

    @Override
    public void run() {

        // Show Process
        String[] cmdArray = {
                "ps", "aux"
        };

        ProcessBuilder pb = new ProcessBuilder(cmdArray);

        Process pr = null;
        try {
            pr = pb.start();
            InputStream is = pr.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);

            // Search FFmpeg Process and Identify its Process ID
            String s = "";
            while ((s = br.readLine()) != null) {
                log.debug(" {}", s);
                String sTrim = s.trim();
                ArrayList<String> arrayPID = null;

                if ((sTrim.matches(".*libx264.*") && sTrim.matches(".*mpegts.*")) || (sTrim.matches(".*h264_qsv.*") && sTrim.matches(".*mpegts.*"))) { // TODO: more
                    String[] sTrimSplit = sTrim.split(" ");
                    arrayPID = new ArrayList<>();
                    for(int i = 0; i < sTrimSplit.length; i++ ) {
                        if (!(sTrimSplit[i].equals(""))) {
                            arrayPID.add(sTrimSplit[i]);
                        }
                    }
                    String PID = arrayPID.get(1);
                    log.debug("{}", PID);
                    stopPID(PID);
                }

            }

            br.close();
            isr.close();
            is.close();
            pr.destroy();

        } catch (IOException e) {
            log.error("{} {}", e.getMessage(), e);
        }

        chukasaModelManagementComponent.deleteAll();
        log.info("all ChukasaModels have been deleted.");

    }

    void stopPID(String pid){

        List<ChukasaModel> chukasaModelList = chukasaModelManagementComponent.get();
        for(ChukasaModel chukasaModel : chukasaModelList){
            chukasaModel.setFlagRemoveFile(true);
            chukasaModelManagementComponent.update(chukasaModel.getAdaptiveBitrateStreaming(), chukasaModel);
        }

        String[] cmdArrayPID = {"kill", "-KILL", pid };
        ProcessBuilder pbPID = new ProcessBuilder(cmdArrayPID);
        try {
            Process prPID = pbPID.start();
            log.info("{} stopped ffmpeg (PID: {}).", this.getClass().getName(), pid);
        } catch (IOException e) {
            log.error("{} {}", e.getMessage(), e);
        }
    }
}
