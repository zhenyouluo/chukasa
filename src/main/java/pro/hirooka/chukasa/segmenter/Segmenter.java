package pro.hirooka.chukasa.segmenter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import pro.hirooka.chukasa.domain.ChukasaModel;
import pro.hirooka.chukasa.domain.type.StreamingType;
import pro.hirooka.chukasa.encrypter.Encrypter;
import pro.hirooka.chukasa.service.IChukasaModelManagementComponent;
import pro.hirooka.chukasa.transcoder.FFmpegRunner;

import java.io.*;
import java.math.BigDecimal;
import java.util.TimerTask;

@Slf4j
public class Segmenter extends TimerTask {

    static final String FILE_SEPARATOR = System.getProperty("file.separator");

    private final String SYNC_WORD = "47";

    private int adaptiveBitrateStreaming;

    private final IChukasaModelManagementComponent chukasaModelManagementComponent;

    @Autowired
    public Segmenter(int adaptiveBitrateStreaming, IChukasaModelManagementComponent chukasaModelManagementComponent) {
        this.chukasaModelManagementComponent = chukasaModelManagementComponent;
        this.adaptiveBitrateStreaming = adaptiveBitrateStreaming;
    }

    @Override
    public void run() {

        ChukasaModel chukasaModel = chukasaModelManagementComponent.get(adaptiveBitrateStreaming);

        long[] infoReadData = readPCR(chukasaModel.getReadBytes(), chukasaModel.getSeqTs());
        chukasaModel.setReadBytes(infoReadData[0]);
        chukasaModel.setSeqTs((int) infoReadData[1]);
        chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);
    }

    long[] readPCR(long readByteInput, int seqTsInput) {

        ChukasaModel chukasaModel = chukasaModelManagementComponent.get(adaptiveBitrateStreaming);
        int mpeg2TsPacketLength = chukasaModel.getHlsConfiguration().getMpeg2TsPacketLength();

        long readByte = readByteInput;
        int seqTs = seqTsInput;

        double segmentedTsDuration = (double) chukasaModel.getHlsConfiguration().getDuration();

        try {
            FileInputStream fis = new FileInputStream(chukasaModel.getSystemConfiguration().getTempPath() + FILE_SEPARATOR + chukasaModel.getChukasaConfiguration().getStreamFileNamePrefix() + chukasaModel.getChukasaSettings().getVideoBitrate() + chukasaModel.getHlsConfiguration().getStreamExtension());
            if(chukasaModel.getChukasaSettings().getStreamingType() == StreamingType.OKKAKE){
                fis = new FileInputStream(chukasaModel.getSystemConfiguration().getFilePath() + FILE_SEPARATOR + chukasaModel.getChukasaSettings().getFileName());
            }

            BufferedInputStream bis = new BufferedInputStream(fis);
            bis.skip(readByte);
            byte[] buf = new byte[mpeg2TsPacketLength];

            boolean flagCreateFile = true;
            FileOutputStream f = null;
            BufferedOutputStream bos = null;

            chukasaModel.setFlagSegFullDuration(false);
            chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);

            int countPacket = 0;
            boolean flagFirstPCR = false;

            int ch;
            loop:
            while ((ch = bis.read(buf)) != -1) {

                countPacket++;
                String syncWordString = String.format("%02x", buf[0]).toUpperCase();

                if (flagCreateFile) {

                    seqTs++;

                    if(chukasaModel.getChukasaSettings().getStreamingType() != StreamingType.OKKAKE) {

                        if (chukasaModel.getChukasaSettings().isEncrypted()) {

                            File tempSegDir = new File(chukasaModel.getTempEncPath());
                            tempSegDir.mkdirs();
                            f = new FileOutputStream(chukasaModel.getTempEncPath() + FILE_SEPARATOR + chukasaModel.getChukasaConfiguration().getStreamFileNamePrefix() + seqTs + chukasaModel.getHlsConfiguration().getStreamExtension());
                            bos = new BufferedOutputStream(f, mpeg2TsPacketLength);
                            log.info("Begin Segmentation of seqTs : {}", seqTs);
                            flagCreateFile = false;

                        } else {

                            f = new FileOutputStream(chukasaModel.getStreamPath() + FILE_SEPARATOR + chukasaModel.getChukasaConfiguration().getStreamFileNamePrefix() + seqTs + chukasaModel.getHlsConfiguration().getStreamExtension());
                            bos = new BufferedOutputStream(f, mpeg2TsPacketLength);
                            log.info("Begin Segmentation of seqTs : {}", seqTs);
                            flagCreateFile = false;

                        }

                    }else{

                        if (chukasaModel.getChukasaSettings().isEncrypted()) {

                            File tempSegDir = new File(chukasaModel.getTempEncPath());
                            tempSegDir.mkdirs();
                            f = new FileOutputStream(chukasaModel.getTempEncPath() + FILE_SEPARATOR + chukasaModel.getChukasaConfiguration().getStreamFileNamePrefix() + seqTs + chukasaModel.getHlsConfiguration().getStreamExtension());
                            bos = new BufferedOutputStream(f, mpeg2TsPacketLength);
                            log.info("Begin Segmentation of seqTs : {}", seqTs);
                            flagCreateFile = false;

                        } else {

                            File tempSegDir = new File(chukasaModel.getTempEncPath());
                            tempSegDir.mkdirs();
                            f = new FileOutputStream(chukasaModel.getTempEncPath() + FILE_SEPARATOR + chukasaModel.getChukasaConfiguration().getStreamFileNamePrefix() + seqTs + chukasaModel.getHlsConfiguration().getStreamExtension());
                            bos = new BufferedOutputStream(f, mpeg2TsPacketLength);
                            log.info("Begin Segmentation of seqTs : {}", seqTs);
                            flagCreateFile = false;

                        }

                    }
                }

                bos.write(buf, 0, buf.length);

                boolean flagAFE = false; // Adaptation Field
                boolean flagPCR = false;

                if (syncWordString.equals(SYNC_WORD)) {

                    //  Adaptation Field Exist
                    // b00 = 0
                    // b01 = 1
                    // b10 = 2 is true
                    // b11 = 3 is true
                    int adaptation_field_exist = buf[3] >> 4 & 0x3 & 0xff;
                    if (adaptation_field_exist == 2 || adaptation_field_exist == 3) {
                        flagAFE = true;
                    }

                    int adaptation_field_length = buf[4] & 0xff;

                    if ((flagAFE) && (adaptation_field_length > 0)) {

                        // PCR Flag
                        if (((buf[5] >> 4 & 0x1) & 0xff) == 1) {
                            flagPCR = true;
                        }

                    } // if AFE

                    if (flagPCR) {

                        // PCR
                        BigDecimal pcrSec = new BigDecimal(((buf[6] & 0xff) * 33554432 + (buf[7] & 0xff) * 131072 + (buf[8] & 0xff) * 512 + (buf[9] & 0xff) * 2 + (buf[10] >>> 7 & 0xff)) / 90000.0).setScale(2, BigDecimal.ROUND_HALF_UP);
                        chukasaModel.setLastPcrSec(pcrSec);
                        chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);

                        if (!flagFirstPCR) {

                            //if (seqTs == 1) {
                            if (seqTs == 0) {
                                chukasaModel.setInitPcrSecond(pcrSec);
                                chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);
                                flagFirstPCR = true;
                            } else {
                                chukasaModel.setInitPcrSecond(chukasaModel.getLastPcrSecond().subtract(chukasaModel.getDiffPcrSecond()));
                                chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);
                                flagFirstPCR = true;
                            }
                        }

                        // Calculate time that has been read before
                        BigDecimal readDuration = null;
                        readDuration = pcrSec.subtract(chukasaModel.getInitPcrSecond());

                        // if readDuration crosses DURATION to be segmented, Output segmented ts (close OutputStream)
                        if (Double.parseDouble(readDuration.toString()) >= segmentedTsDuration) {

                            chukasaModel.setLastPcrSecond(pcrSec);
                            chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);
                            chukasaModel.setDiffPcrSecond(chukasaModel.getLastPcrSecond().subtract(chukasaModel.getInitPcrSecond()).subtract(new BigDecimal(Double.toString(segmentedTsDuration))));
                            chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);

                            log.info("[chukasaModel.getInitPcrSecond()] {}", chukasaModel.getInitPcrSecond());
                            log.info("[chukasaModel.getLastPcrSecond()] {}", chukasaModel.getLastPcrSecond());
                            log.info("[chukasaModel.getDiffPcrSecond()] {}", chukasaModel.getDiffPcrSecond());
                            log.info("[Double.toString(DURATION))] {}", new BigDecimal(Double.toString(segmentedTsDuration)));

                            bos.close();
                            f.close();

                            //chukasaModel.setSeqTsEnc(seqTs - 1);
                            chukasaModel.setSeqTsEnc(seqTs);
                            chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);

                            if(chukasaModel.getChukasaSettings().getStreamingType() != StreamingType.OKKAKE) {
                                // process after spliting MPEG2-TS
                                if (chukasaModel.getChukasaSettings().isEncrypted()) {
                                    Encrypter encrypter = new Encrypter(adaptiveBitrateStreaming, chukasaModelManagementComponent);
                                    Thread thread = new Thread(encrypter);
                                    thread.start();
                                }
                            }else{
                                chukasaModel.setSeqTsOkkake(chukasaModel.getSeqTsEnc());
                                chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);
                                FFmpegRunner fFmpegRunner = new FFmpegRunner(adaptiveBitrateStreaming, chukasaModelManagementComponent);
                                Thread thread = new Thread(fFmpegRunner);
                                thread.start();
                            }

                            flagCreateFile = true;
                            chukasaModel.setFlagSegFullDuration(true);
                            chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);

                            break loop;
                        }

                    } // if flagPCR

                } // if SYNC_WORD

            } // loop : while

            bos.close();
            bis.close();
            fis.close();
            f.close();

            if (!chukasaModel.isFlagSegFullDuration()) {
                chukasaModel.setFlagLastTs(true);
                chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);
            }

            if (chukasaModel.isFlagLastTs()) {
                //chukasaModel.setSeqTsLast(seqTs - 1);
                chukasaModel.setSeqTsLast(seqTs);

                if(chukasaModel.getChukasaSettings().getStreamingType() != StreamingType.OKKAKE) {
                    if (chukasaModel.getChukasaSettings().isEncrypted()) {
                        Encrypter encrypter = new Encrypter(adaptiveBitrateStreaming, chukasaModelManagementComponent);
                        Thread thread = new Thread(encrypter);
                        thread.start();
                    }
                }else{
                    chukasaModel.setSeqTsOkkake(chukasaModel.getSeqTsEnc());
                    chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);
                    FFmpegRunner fFmpegRunner = new FFmpegRunner(adaptiveBitrateStreaming, chukasaModelManagementComponent);
                    Thread thread = new Thread(fFmpegRunner);
                    thread.start();
                }

                chukasaModel.setFlagTimerSegmenter(true);
                chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);

            }

            long[] ret = new long[2];
            ret[0] = (mpeg2TsPacketLength * (countPacket)) + readByte; // MPEG2-TS byte length ever read
            ret[1] = seqTs; // MPEG2-TS sequences ever segmented
            return ret;

        } catch (IOException e) {

            long[] ret = new long[2];
            ret[0] = readByteInput;
            ret[1] = seqTsInput;
            return ret;

        }

    }
}
