package pro.hirooka.chukasa.segmenter;

import lombok.extern.slf4j.Slf4j;
import pro.hirooka.chukasa.domain.chukasa.ChukasaModel;
import pro.hirooka.chukasa.service.chukasa.IChukasaModelManagementComponent;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import static java.util.Objects.requireNonNull;

@Slf4j
public class FFmpegHLSSegmenter extends TimerTask {

    static final String FILE_SEPARATOR = System.getProperty("file.separator");

    private int adaptiveBitrateStreaming;

    private IChukasaModelManagementComponent chukasaModelManagementComponent;

    public FFmpegHLSSegmenter(int adaptiveBitrateStreaming, IChukasaModelManagementComponent chukasaModelManagementComponent) {
        this.adaptiveBitrateStreaming = adaptiveBitrateStreaming;
        this.chukasaModelManagementComponent = requireNonNull(chukasaModelManagementComponent, "chukasaModelManagementComponent");
    }

    @Override
    public void run() {

        ChukasaModel chukasaModel = chukasaModelManagementComponent.get(adaptiveBitrateStreaming);
        int sequence = chukasaModel.getSeqTs();
        String streamPath = chukasaModel.getStreamPath();
        log.info("sequence = {}", sequence);
        log.debug("streamPath = {}", streamPath);

        String tsPath = streamPath + FILE_SEPARATOR + "chukasa" + (sequence + 1) + ".ts";
        File file = new File(tsPath);
        if(file.exists()){
            log.debug("file exists: {}", file.getAbsolutePath());
            tsPath = streamPath + FILE_SEPARATOR + "chukasa" + (sequence + 2) + ".ts";
            file = new File(tsPath);
            if(file.exists()) {
                log.debug("file exists: {}", file.getAbsolutePath());
                sequence = sequence + 1;
                chukasaModel.setSeqTs(sequence);

                List<Double> extinfList = chukasaModel.getExtinfList();
                List<Double> ffmpegM3U8EXTINFList = getEXTINFList(streamPath + FILE_SEPARATOR + "ffmpeg.m3u8");
                if(sequence >= 0 && ffmpegM3U8EXTINFList.size() > 0){
                    extinfList.add(ffmpegM3U8EXTINFList.get(sequence));
                }else{
                    extinfList.add((double)chukasaModel.getHlsConfiguration().getDuration());
                }
                chukasaModel.setExtinfList(extinfList);

                chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);
            }
        }

    }

    private List<Double> getEXTINFList(String m3u8Path){
        File m3u8 = new File(m3u8Path);
        if(m3u8.exists()) try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(m3u8));
            final String EXTINF_TAG = "#EXTINF:";
            List<Double> extinfList = new ArrayList<>();
            String str = "";
            while ((str = bufferedReader.readLine()) != null) {
                if (str.startsWith(EXTINF_TAG)) {
                    try {
                        double extinf = Double.parseDouble(str.split(EXTINF_TAG)[1].split(",")[0]);
                        log.debug("extinf = {}", extinf);
                        extinfList.add(extinf);
                    }catch(NumberFormatException e){
                        log.error("{} {}", e.getMessage(), e);
                        break;
                    }
                }
            }
            return extinfList;
        } catch (IOException e) {
            log.error("{} {}", e.getMessage(), e);
        }
        return new ArrayList<>();
    }
}
