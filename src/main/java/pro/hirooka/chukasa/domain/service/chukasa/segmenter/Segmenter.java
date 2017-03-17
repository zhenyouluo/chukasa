package pro.hirooka.chukasa.domain.service.chukasa.segmenter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import pro.hirooka.chukasa.domain.model.chukasa.constants.ChukasaConstant;
import pro.hirooka.chukasa.domain.model.chukasa.ChukasaModel;
import pro.hirooka.chukasa.domain.model.chukasa.enums.StreamingType;
import pro.hirooka.chukasa.domain.service.chukasa.encrypter.Encrypter;
import pro.hirooka.chukasa.domain.service.chukasa.IChukasaModelManagementComponent;
import pro.hirooka.chukasa.domain.service.chukasa.transcoder.FFmpegRunner;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;

@Deprecated
@Slf4j
public class Segmenter extends TimerTask {

    static final String FILE_SEPARATOR = System.getProperty("file.separator");

    final String STREAM_FILE_NAME_PREFIX = ChukasaConstant.STREAM_FILE_NAME_PREFIX;
    final String STREAM_FILE_EXTENSION = ChukasaConstant.STREAM_FILE_EXTENSION;
    final int MPEG2_TS_PACKET_LENGTH = ChukasaConstant.MPEG2_TS_PACKET_LENGTH;

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
//        long[] infoReadData = readDTS(chukasaModel.getReadBytes(), chukasaModel.getSeqTs());
        chukasaModel.setReadBytes(infoReadData[0]);
        chukasaModel.setSeqTs((int) infoReadData[1]);
        chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);
    }

    // NOTE; VOD では問題ないが，EVENT で Segment の先頭で再生が一時的に止まるケースがあり，解決できていない
    long[] readDTS(long readByteInput, int seqTsInput){

        List<byte []> temporaryBufList = new ArrayList<>();

        int patPosition = 0;
        int pmtPosition = 0;
        int toBeSegmentedPostion = 0;
        boolean canSegment = false;

        boolean flagPMTExists = false;
        Set<Integer> programMapPIDSet = new HashSet<>();
        Set<Integer> elementaryPIDSet = new HashSet<>();

        ChukasaModel chukasaModel = chukasaModelManagementComponent.get(adaptiveBitrateStreaming);

        long readByte = readByteInput;
        int seqTs = seqTsInput;

        double segmentedTsDuration = (double) chukasaModel.getHlsConfiguration().getDuration();

        try {
            FileInputStream fis = null;
            if(chukasaModel.getChukasaSettings().getStreamingType() != StreamingType.OKKAKE) {
                fis = new FileInputStream(chukasaModel.getSystemConfiguration().getTemporaryPath() + FILE_SEPARATOR + STREAM_FILE_NAME_PREFIX + chukasaModel.getChukasaSettings().getVideoBitrate() + STREAM_FILE_EXTENSION);
            }else if(chukasaModel.getChukasaSettings().getStreamingType() == StreamingType.OKKAKE){
                fis = new FileInputStream(chukasaModel.getSystemConfiguration().getFilePath() + FILE_SEPARATOR + chukasaModel.getChukasaSettings().getFileName());
            }

            BufferedInputStream bis = new BufferedInputStream(fis);
            bis.skip(readByte);
            byte[] buf = new byte[MPEG2_TS_PACKET_LENGTH];

            boolean ignorePAT = false;
            boolean ignorePMT = false;

            boolean flagCreateFile = true;
            FileOutputStream f = null;
            BufferedOutputStream bos = null;

            chukasaModel.setFlagSegFullDuration(false);
            chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);

            int countPacket = 0;
            boolean isPAT = false;
            boolean flagFirstPCR = false;
            boolean isElapsed = false;

            int ch;
            loop:
            while ((ch = bis.read(buf)) != -1) {

                boolean flagPMT = false;
                boolean flagPayloadOnly = false;
                boolean flagAdaptationFieldOnly = false;
                boolean flagAdaptationFieldAndPayload = false;
                boolean flagNoCRCInPMT = false;

                //countPacket++;
                String syncWordString = String.format("%02x", buf[0]).toUpperCase();

                if (flagCreateFile) {

                    seqTs++;

                    if(chukasaModel.getChukasaSettings().getStreamingType() != StreamingType.OKKAKE) {

                        if (chukasaModel.getChukasaSettings().isCanEncrypt()) {

                            File tempSegDir = new File(chukasaModel.getTempEncPath());
                            tempSegDir.mkdirs();
                            f = new FileOutputStream(chukasaModel.getTempEncPath() + FILE_SEPARATOR + STREAM_FILE_NAME_PREFIX + seqTs + STREAM_FILE_EXTENSION);
                            bos = new BufferedOutputStream(f, MPEG2_TS_PACKET_LENGTH );
                            log.info("Begin Segmentation of seqTs : {}", seqTs);
                            flagCreateFile = false;

                        } else {

                            f = new FileOutputStream(chukasaModel.getStreamPath() + FILE_SEPARATOR + STREAM_FILE_NAME_PREFIX + seqTs + STREAM_FILE_EXTENSION);
                            bos = new BufferedOutputStream(f, MPEG2_TS_PACKET_LENGTH );
                            log.info("Begin Segmentation of seqTs : {}", seqTs);
                            flagCreateFile = false;

                        }

                    }else{

                        if (chukasaModel.getChukasaSettings().isCanEncrypt()) {

                            File tempSegDir = new File(chukasaModel.getTempEncPath());
                            tempSegDir.mkdirs();
                            f = new FileOutputStream(chukasaModel.getTempEncPath() + FILE_SEPARATOR + STREAM_FILE_NAME_PREFIX + seqTs + STREAM_FILE_EXTENSION);
                            bos = new BufferedOutputStream(f, MPEG2_TS_PACKET_LENGTH );
                            log.info("Begin Segmentation of seqTs : {}", seqTs);
                            flagCreateFile = false;

                        } else {

                            File tempSegDir = new File(chukasaModel.getTempEncPath());
                            tempSegDir.mkdirs();
                            f = new FileOutputStream(chukasaModel.getTempEncPath() + FILE_SEPARATOR + STREAM_FILE_NAME_PREFIX + seqTs + STREAM_FILE_EXTENSION);
                            bos = new BufferedOutputStream(f, MPEG2_TS_PACKET_LENGTH );
                            log.info("Begin Segmentation of seqTs : {}", seqTs);
                            flagCreateFile = false;

                        }
                    }
                }

                int payloadUnitStartIndicator = ((buf[1] >>> 6) & 0x01);

                int pid = ((buf[1] << 8) & 0x1f00 | buf[2]);

                if(payloadUnitStartIndicator == 1 && flagPMTExists){
                    for(int i : programMapPIDSet){
                        if(i == pid){
                            flagPMT = true;
                            if(countPacket - patPosition == 1) {
                                pmtPosition = countPacket;
                            }
                            if(ignorePAT) {
                                ignorePMT = true;
                                ignorePAT = false;
                            }
                        }
                    }
                }

                if((payloadUnitStartIndicator == 1) && (pid == 0)){
                    isPAT = true;
                    patPosition = countPacket;
                    ignorePAT = true;
                }

                if(isElapsed){
                    double duration = chukasaModel.getNextInit().subtract(chukasaModel.getInitPcrSecond()).setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
                    List<Double> extinfList = chukasaModel.getExtinfList();
                    extinfList.add(duration);
                    chukasaModel.setExtinfList(extinfList);
                    chukasaModel.setDuration(duration);

                    bos.flush();
                    bos.close();
                    bis.close();
                    fis.close();
                    f.close();

                    chukasaModel.setSeqTsEnc(seqTs);
                    chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);

                    if(chukasaModel.getChukasaSettings().getStreamingType() != StreamingType.OKKAKE) {
                        // process after spliting MPEG2-TS
                        if (chukasaModel.getChukasaSettings().isCanEncrypt()) {
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


                    countPacket = toBeSegmentedPostion;

                    break loop;
                }

                boolean flagAFE = false; // Adaptation Field
                boolean flagPCR = false;

                if (syncWordString.equals(SYNC_WORD)) {

                    //  Adaptation Field Exist
                    // b00 = 0          Reserved for future use by ISO/IEC
                    // b01 = 1          No adaptation_field, payload only
                    // b10 = 2 is true  Adaptation_field only, no payload
                    // b11 = 3 is true  Adaptation_field followed by payload
                    int adaptationFieldExist = buf[3] >> 4 & 0x3 & 0xff;
                    if (adaptationFieldExist == 2 || adaptationFieldExist == 3) {
                        flagAFE = true;
                    }
                    switch(adaptationFieldExist){
                        case 0: // Reserved for future use by ISO/IEC
                            break;
                        case 1: // No adaptation_field, payload only
                            flagPayloadOnly = true;
                            break;
                        case 2: // Adaptation_field only, no payload
                            flagAdaptationFieldOnly = true;
                            break;
                        case 3: // Adaptation_field followed by payload
                            flagAdaptationFieldAndPayload = true;
                            break;
                        default: //
                            break;
                    }

                    int adaptationFieldLength = 0;
                    if(flagAdaptationFieldOnly || flagAdaptationFieldAndPayload){
                        adaptationFieldLength = buf[4] & 0xff;
                    }

                    // PAT
                    if(payloadUnitStartIndicator == 1 && adaptationFieldLength == 0 && pid == 0){

                        int pointerField = buf[4] & 0xff;
                        int tableID = buf[5] & 0xff; // table_id of PAT must be 0x00

                        if(tableID == 0){
                            // section syntax indicator is b1, 0 is b0, reserved is b11
                            if(((buf[6] >> 7 & 0x1 & 0xff) == 1) && ((buf[6] >> 6 & 0x1 & 0xff) == 0) && ((buf[6] >> 5 & 0x1 & 0xff) == 1) && ((buf[6] >> 4 & 0x1 & 0xff) == 1)){
                                // section length MSB2bit is b00
                                if(((buf[6] >> 3 & 0x1 & 0xff) == 0) && ((buf[6] >> 2 & 0x1 & 0xff) == 0)){
                                    int sectionLength = (buf[6] << 8 & 0x300 | buf[7]) & 0x2ff;

                                    if(sectionLength > 1021){
                                        //break;
                                    }

                                    // PAT は 1 パケット単位のみ見るという制限．
                                    // section_length が 1021 byte 以下であっても 180 byte (188 - 8) より大きい場合は
                                    // プログラム側の都合でエラーとする．
                                    if(sectionLength > 180){
                                        //break;
                                    }

                                    int numberOfPID = (sectionLength - 9) / 4; // 5byte(transport_stream_id, reserved, version_number, current_next_indicator, section_number, last_section_number) + 4byte(CRC32)
                                    if(((sectionLength - 9) % 4) != 0){
                                        //break;
                                    }

                                    int i = 0;
                                    while(i < (numberOfPID * 4)){
                                        int programMapPID = (buf[15 + i] & 0x1f) * 256 + (buf[16 + i] & 0xff);
                                        programMapPIDSet.add(programMapPID);
                                        i += 4;
                                    } // while

                                    flagPMTExists = true;

                                }else{
                                    //break;
                                } // if First two bits of Section Length
                            }else{
                                //break;
                            } // if Syntax Indicator, 0, Reserved
                        }else{
                            //break;
                        } // if table_id
                    }

                    // PMT
                    flagNoCRCInPMT = false;
                    if((payloadUnitStartIndicator == 1) && flagPMT){

                        int pointerField = buf[4] & 0xff;
                        int tableID = buf[5] & 0xff; // table_id of PMT must 0x02

                        if(pointerField == 0 && tableID == 2){

                            // section syntax indicator ?
                            // 0 0
                            // reserved 11
                            if(((buf[6] >> 6 & 0x1 & 0xff) == 0) && ((buf[6] >> 5 & 0x1 & 0xff) == 1) && ((buf[6] >> 4 & 0x1 & 0xff) == 1)){

                                // section length 2bit 00
                                if(((buf[6] >> 3 & 0x1 & 0xff) == 0) && ((buf[6] >> 2 & 0x1 & 0xff) == 0)){

                                    int sectionLength = (buf[6] << 8 & 0x300 | buf[7]) & 0x2ff;

                                    if(sectionLength > 1021){
                                        //break;
                                    }

                                    // PMT は 1 パケット単位のみ見るという制限．
                                    // section_length が 1021 byte 以下であっても 180 byte (188 - 8) より大きい場合は
                                    // プログラム側の都合でエラーとする．
                                    if(sectionLength > 180){
                                        //break;
                                    }

                                    // section_length が 180 以下で 176 より大きい場合は CRC が無いとみなす．
                                    if(sectionLength > 176){
                                        sectionLength = 176;
                                        flagNoCRCInPMT = true;
                                    }

                                    int programNumber = (buf[8] & 0xff) * 256 + (buf[9] & 0xff);
                                    int pcrPID = (buf[13] & 0x1f) * 256 + (buf[14] & 0xff);
                                    int programInfoLength = (buf[15] & 0x3) * 256 + (buf[16] & 0xff);

                                    int readPMT = 17 + programInfoLength; // start from 0
                                    int crcLength = 4;
                                    if(flagNoCRCInPMT) crcLength = 0;

                                    int byteOfPids = sectionLength - (9 + programInfoLength + crcLength) + (17 + programInfoLength);

                                    //int countByteOfPids = 0;
                                    //while(read_pmt < (8 + section_length - crc_length - 5) ){ // 8(to Section Length), 4(CRC), 5(minimum Elementary PID)
                                    while(readPMT < byteOfPids){
                                        int streamType = buf[readPMT] & 0xff;
                                        int reserved = buf[readPMT + 1] >>> 5 & 0x7 & 0xff;
                                        int elementaryPID = (buf[readPMT + 1] & 0x1f) * 256 + (buf[readPMT + 2] & 0xff);
                                        elementaryPIDSet.add(elementaryPID);

                                        int esInfoLength = (buf[readPMT + 3] & 0xf) * 256 + (buf[readPMT + 4] & 0xff);
                                        readPMT = readPMT + 5 + esInfoLength;
                                    }
                                }else{
                                    //break;
                                } // if First two bits of Senction Length
                            }else{
                                //break;
                            } // if Syntax Indicator, 0, Reserved
                        }else{
                            //break;
                        } // if table_id
                        flagPMT = false;
                    }

                    // PCR, OPCR
                    if(flagAdaptationFieldOnly || flagAdaptationFieldAndPayload){

                        if(adaptationFieldLength > 0){

                            int randomAccessIndicator = (buf[5] >> 6 & 0x1) & 0xff;
                            if(randomAccessIndicator == 1){

                            }

                            if(((buf[5] >> 4 & 0x1) & 0xff) == 1){ // PCR Flag

                                //long pcr = (buf[6] << 25 | buf[7] << 17 | buf[8] << 9 | buf[9] << 1 | buf[10] >>> 7);
                                //long pcr = (long)(buf[6] << 25 & 0x1fe000000 | buf[7] << 17 & 0x1fe0000 | buf[8] << 9 & 0x1fe00 | buf[9] << 1 & 0x1e | buf[10] >>> 7 & 0xff);
							/*
							long pcr1 = (buf[6] & 0xff) * 33554432; // 2^25
							int  pcr2 = (buf[7] & 0xff) *   131072; // 2^17
							int  pcr3 = (buf[8] & 0xff) *      512; // 2^ 9
							int  pcr4 = (buf[9] & 0xff) *        2; // 2^ 1
							int  pcr5 = (buf[10] >>> 7) & 0xff;
							pcr = pcr1 + pcr2 + pcr3 + pcr4 + pcr5;
							*/

                                long pcrBaseLong = ((buf[6] & 0xff) * 33554432 + (buf[7] & 0xff) * 131072 + (buf[8] & 0xff) * 512 + (buf[9] & 0xff) * 2 + (buf[10] >>> 7 & 0xff));

                                //double pcr_base = new BigDecimal(((buf[6] & 0xff) * 33554432 + (buf[7] & 0xff) * 131072 + (buf[8] & 0xff) * 512 + (buf[9] & 0xff) * 2 + (buf[10] >>> 7 & 0xff)) / 90000.0).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                                double pcrBase = new BigDecimal(((buf[6] & 0xff) * 33554432 + (buf[7] & 0xff) * 131072 + (buf[8] & 0xff) * 512 + (buf[9] & 0xff) * 2 + (buf[10] >>> 7 & 0xff)) / 90000.0).setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
                                double pcr_extension = new BigDecimal((((buf[10] & 0x1) & 0xff) * 256 + (buf[11] & 0xff))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                            } // if PCR flag

                            if(((buf[5] >> 3 & 0x1) & 0xff ) == 1){ // OPCR flag
                                double opcr = new BigDecimal(((buf[12] & 0xff) * 33554432 + (buf[13] & 0xff) * 131072 + (buf[14] & 0xff) * 512 + (buf[15] & 0xff) * 2 + (buf[16] >>> 7 & 0xff)) / 90000.0).setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
                            }

                        } // if(adaptation_field_length > 0)

                    } // PCR, OPCR

                    // PES
                    if(((payloadUnitStartIndicator == 1) && flagPayloadOnly) || ((payloadUnitStartIndicator == 1) && flagAdaptationFieldAndPayload)){
                        if((pid != 0)){

                            boolean isVideo = false;

                            //int readPes = 4; //TS_PACKET_HEADER_LENGTH
                            int readPES = 4 + adaptationFieldLength;
                            while(readPES < (MPEG2_TS_PACKET_LENGTH  - 4)){ //TS_PACKET_HEADER_LENGTH

                                if(String.format("%02x", buf[readPES]).equals("00") && String.format("%02x", buf[readPES+1]).equals("00") && String.format("%02x", buf[readPES+2]).equals("01")){ // Packet Start Code Prefix 0x000001
                                    // PES Packet Header
                                    //countPesPacket++;

                                    String streamIDHex = String.format("%02x", buf[readPES + 3]).toUpperCase(); // Stream ID (Audio Streams (0xC0-0xDF), Video Streams (0xE0-0xEF))
                                    int streamID  = (buf[readPES + 3] & 0xff);

                                    if((192 <= streamID) && (streamID <= 239)){
                                        if((192 <= streamID) && (streamID <= 223)){ // 0xC0=192 0xDF=223 Audio
                                            //log.info("{}, {}, {}, PES start code (Audo), {}, {}", countPacket, countPacket*MPEG2_TS_PACKET_LENGTH , pid, streamID, streamIDHex);
                                            break ;
                                        }else if((224 <= streamID) && (streamID <= 239)){ // 0xE0=224 0xEF=239 Video
                                            //log.info("{}, {}, {}, PES start code (Video), {}, {}", countPacket, countPacket*MPEG2_TS_PACKET_LENGTH , pid, streamID, streamIDHex);
                                            isVideo = true;
                                        }
                                    }

                                    int pesPacketLength = (buf[readPES + 4] & 0xff) * 256 + (buf[readPES + 5] & 0xff);

                                    if(pesPacketLength == 0 && ((192 <= streamID) && (streamID <= 223))){
                                        break;
                                    }else if(pesPacketLength == 0 && ((224 <= streamID) && (streamID <= 239))){
                                        //PES packet can be of any length
                                    }else if(!((192 <= streamID) && (streamID <= 239))){
                                        break;
                                    }

                                    if(isVideo) {

                                        // Optional PES Header
                                        int pesHeaderMarkerBits = buf[readPES + 6] >> 6 & 0x3 & 0xff;

                                        if ((192 <= streamID) && (streamID <= 239)) {   // 0xC0=192 0xDF=223 0xE0=224 0xEF=239
                                            if ((192 <= streamID) && (streamID <= 223)) { // 0xC0=192 0xDF=223
                                            } else if ((224 <= streamID) && (streamID <= 239)) { // 0xE0=224 0xEF=239
                                            }

                                            if (pesHeaderMarkerBits == 2) {

                                                int ptsDTSIndicator = buf[readPES + 7] >> 6 & 0x3 & 0xff;

                                                int pesHeaderLength = buf[readPES + 8] & 0xff;

                                                // PTS のみ存在する．
                                                if (ptsDTSIndicator == 2) {
                                                    long ptsLong =
                                                            (((buf[readPES + 9] >>> 1) & 0x7 & 0xff) * 1073741824) +
                                                                    ((buf[readPES + 10] & 0xff) * 4194304) +
                                                                    (((buf[readPES + 11] >>> 1) & 0x7f & 0xff) * 32768) +
                                                                    ((buf[readPES + 12] & 0xff) * 128) +
                                                                    ((buf[readPES + 13] >>> 1) & 0x7f & 0xff);
                                                    double pts = new BigDecimal(ptsLong / 90000.0).setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
                                                    //log.info("{}, {}, PTS, {}, {}, {}, {}", countPacket, countPacket*MPEG2_TS_PACKET_LENGTH , Long.toBinaryString(ptsLong), Long.toHexString(ptsLong), ptsLong, String.format("%.4f", pts));

                                                    break;

                                                    // PTS, DTS 両方が存在する．
                                                } else if (ptsDTSIndicator == 3) {
                                                    long ptsLong =
                                                            (((buf[readPES + 9] >>> 1) & 0x7 & 0xff) * 1073741824) +
                                                                    ((buf[readPES + 10] & 0xff) * 4194304) +
                                                                    (((buf[readPES + 11] >>> 1) & 0x7f & 0xff) * 32768) +
                                                                    ((buf[readPES + 12] & 0xff) * 128) +
                                                                    ((buf[readPES + 13] >>> 1) & 0x7f & 0xff);
                                                    double pts = new BigDecimal(ptsLong / 90000.0).setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
                                                    log.info("{}, {}, PTS, {}, {}, {}, {}", countPacket, countPacket*MPEG2_TS_PACKET_LENGTH , Long.toBinaryString(ptsLong), Long.toHexString(ptsLong), ptsLong, String.format("%.4f", pts));

                                                    long dtsLong =
                                                            (((buf[readPES + 14] >>> 1) & 0x7 & 0xff) * 1073741824) +
                                                                    ((buf[readPES + 15] & 0xff) * 4194304) +
                                                                    (((buf[readPES + 16] >>> 1) & 0x7f & 0xff) * 32768) +
                                                                    ((buf[readPES + 17] & 0xff) * 128) +
                                                                    ((buf[readPES + 18] >>> 1) & 0x7f & 0xff);
                                                    double dts = new BigDecimal(dtsLong / 90000.0).setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
                                                    log.info("{}, {}, DTS, {}, {}, {}, {}", countPacket, countPacket * MPEG2_TS_PACKET_LENGTH, Long.toBinaryString(dtsLong), Long.toHexString(dtsLong), dtsLong, String.format("%.4f", dts));

                                                    BigDecimal pcrSec = new BigDecimal(dtsLong / 90000.0).setScale(4, BigDecimal.ROUND_HALF_UP);
                                                    chukasaModel.setLastPcrSec(pcrSec);
                                                    chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);

                                                    if (!flagFirstPCR) {

                                                        //if (seqTs == 1) {
                                                        if (seqTs == 0) {
                                                            chukasaModel.setInitPcrSecond(pcrSec);
                                                            chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);
                                                            flagFirstPCR = true;
                                                        } else {
                                                            //chukasaModel.setInitPcrSecond(chukasaModel.getLastPcrSecond().subtract(chukasaModel.getDiffPcrSecond()));
                                                            chukasaModel.setInitPcrSecond(chukasaModel.getNextInit());
                                                            chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);
                                                            flagFirstPCR = true;
                                                        }
                                                    }

                                                    // Calculate time that has been read before
                                                    BigDecimal readDuration = null;
                                                    readDuration = pcrSec.subtract(chukasaModel.getInitPcrSecond());

                                                    if (countPacket - pmtPosition == 1 && countPacket - patPosition == 2) {
                                                        canSegment = true;
                                                    }else{
                                                        canSegment = false;
                                                    }

                                                    if (Double.parseDouble(readDuration.toString()) >= segmentedTsDuration && canSegment) {
                                                        isElapsed = true;
                                                    }

                                                    if (!isElapsed) {
                                                        if (countPacket - pmtPosition == 1 && countPacket - patPosition == 2) {
                                                            toBeSegmentedPostion = countPacket - 2;
                                                            chukasaModel.setNextInit(new BigDecimal(dtsLong / 90000.0).setScale(4, BigDecimal.ROUND_HALF_UP));
                                                            chukasaModel.setLastPcrSecond(new BigDecimal(dtsLong / 90000.0).setScale(4, BigDecimal.ROUND_HALF_UP));
                                                            double duration = new BigDecimal(dtsLong / 90000.0).setScale(4, BigDecimal.ROUND_HALF_UP).subtract(chukasaModel.getInitPcrSecond()).setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
                                                            chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);
                                                            canSegment = true;
                                                        }else{
                                                            canSegment = false;
                                                        }
                                                    }

                                                    break;
                                                }
                                            }
                                        } else {
                                            if (pesPacketLength != 0) {
                                                readPES = readPES + pesPacketLength;
                                            } else {
                                                readPES = readPES + MPEG2_TS_PACKET_LENGTH; // while PES packet からの追い出し
                                            }
                                        } // if streamid
                                    }else{
                                        break;
                                    }
                                } // if start bit
                                readPES = readPES + 1;
                            } // while PES packet
                        }
                    } // if PES

                    if(!isElapsed) {
                        if(canSegment) {
                            for(int i = 0; i < temporaryBufList.size() - 2; i ++){
                                bos.write(temporaryBufList.get(i), 0, temporaryBufList.get(i).length);
                            }
                            byte[] bufNextPAT = Arrays.copyOf(temporaryBufList.get(temporaryBufList.size() - 2), temporaryBufList.get(temporaryBufList.size() - 2).length);
                            byte[] bufNextPMT = Arrays.copyOf(temporaryBufList.get(temporaryBufList.size() - 1), temporaryBufList.get(temporaryBufList.size() - 1).length);
                            temporaryBufList = new ArrayList<>();
                            byte[] bufDeep = Arrays.copyOf(buf, buf.length);
                            temporaryBufList.add(bufNextPAT);
                            temporaryBufList.add(bufNextPMT);
                            temporaryBufList.add(bufDeep);
                            canSegment = false;
                        }else {
                            if(isPAT) {
                                byte[] bufDeep = Arrays.copyOf(buf, buf.length);
                                temporaryBufList.add(bufDeep);
                            }
                        }
                    }

                    countPacket++;

                } // if SYNC_WORD

            } // loop : while

            bos.flush();
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
                    if (chukasaModel.getChukasaSettings().isCanEncrypt()) {
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
            ret[0] = (MPEG2_TS_PACKET_LENGTH * (countPacket)) + readByte; // MPEG2-TS byte length ever read
            ret[1] = seqTs; // MPEG2-TS sequences ever segmented
            return ret;

        } catch (IOException e) {

            long[] ret = new long[2];
            ret[0] = readByteInput;
            ret[1] = seqTsInput;
            return ret;

        }
    }

    long[] readPCR(long readByteInput, int seqTsInput) {

        ChukasaModel chukasaModel = chukasaModelManagementComponent.get(adaptiveBitrateStreaming);

        long readByte = readByteInput;
        int seqTs = seqTsInput;

        double segmentedTsDuration = (double) chukasaModel.getHlsConfiguration().getDuration();

        try {
            FileInputStream fis = null;
            if(!chukasaModel.getChukasaSettings().getStreamingType().equals(StreamingType.OKKAKE)) {
                fis = new FileInputStream(chukasaModel.getSystemConfiguration().getTemporaryPath() + FILE_SEPARATOR + STREAM_FILE_NAME_PREFIX + chukasaModel.getChukasaSettings().getVideoBitrate() + STREAM_FILE_EXTENSION);
            }else{
                fis = new FileInputStream(chukasaModel.getSystemConfiguration().getFilePath() + FILE_SEPARATOR + chukasaModel.getChukasaSettings().getFileName());
            }

            BufferedInputStream bis = new BufferedInputStream(fis);
            bis.skip(readByte);
            byte[] buf = new byte[MPEG2_TS_PACKET_LENGTH];

            boolean flagCreateFile = true;
            FileOutputStream f = null;
            BufferedOutputStream bos = null;

            chukasaModel.setFlagSegFullDuration(false);
            chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);

            int countPacket = 0;
            boolean isPAT = false;
            boolean flagFirstPCR = false;
            boolean isElapsed = false;
            boolean canSegment = false;
            boolean isNextPAT = false;
            int toBeSegmentedPostion = 0;

            int ch;
            loop:
            while ((ch = bis.read(buf)) != -1) {

                //countPacket++;
                String syncWordString = String.format("%02x", buf[0]).toUpperCase();

                if (flagCreateFile) {

                    seqTs++;

                    if(!chukasaModel.getChukasaSettings().getStreamingType().equals(StreamingType.OKKAKE)) {

                        if (chukasaModel.getChukasaSettings().isCanEncrypt()) {

                            File tempSegDir = new File(chukasaModel.getTempEncPath());
                            tempSegDir.mkdirs();
                            f = new FileOutputStream(chukasaModel.getTempEncPath() + FILE_SEPARATOR + STREAM_FILE_NAME_PREFIX + seqTs + STREAM_FILE_EXTENSION);
                            bos = new BufferedOutputStream(f, MPEG2_TS_PACKET_LENGTH );
                            log.info("Begin Segmentation of seqTs : {}", seqTs);
                            flagCreateFile = false;

                        } else {

                            f = new FileOutputStream(chukasaModel.getStreamPath() + FILE_SEPARATOR + STREAM_FILE_NAME_PREFIX + seqTs + STREAM_FILE_EXTENSION);
                            bos = new BufferedOutputStream(f, MPEG2_TS_PACKET_LENGTH );
                            log.info("Begin Segmentation of seqTs : {}", seqTs);
                            flagCreateFile = false;

                        }

                    }else{

//                        if (chukasaModel.getChukasaSettings().isEncrypted()) {

                            File tempSegDir = new File(chukasaModel.getTempEncPath());
                            tempSegDir.mkdirs();
                            f = new FileOutputStream(chukasaModel.getTempEncPath() + FILE_SEPARATOR + STREAM_FILE_NAME_PREFIX + seqTs + STREAM_FILE_EXTENSION);
                            bos = new BufferedOutputStream(f, MPEG2_TS_PACKET_LENGTH );
                            log.info("Begin Segmentation of seqTs : {}", seqTs);
                            flagCreateFile = false;

//                        } else {
//
//                            File tempSegDir = new File(chukasaModel.getTempEncPath());
//                            tempSegDir.mkdirs();
//                            f = new FileOutputStream(chukasaModel.getTempEncPath() + FILE_SEPARATOR + STREAM_FILE_NAME_PREFIX + seqTs + STREAM_FILE_EXTENSION);
//                            bos = new BufferedOutputStream(f, MPEG2_TS_PACKET_LENGTH );
//                            log.info("Begin Segmentation of seqTs : {}", seqTs);
//                            flagCreateFile = false;
//
//                        }

                    }
                }

                int payloadUnitStartIndicator = ((buf[1] >>> 6) & 0x01);
                int pid = ((buf[1] << 8) & 0x1f00 | buf[2]);
                if((payloadUnitStartIndicator == 1) && (pid == 0)){
                    isPAT = true;
                    if(isElapsed){
                        isNextPAT = true;
                        if(toBeSegmentedPostion == 0) {
                            toBeSegmentedPostion = countPacket;
                        }
                    }
                }

                if(canSegment){

                    double duration = chukasaModel.getNextInit().subtract(chukasaModel.getInitPcrSecond()).setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
                    double diff = chukasaModel.getDiffPcrSecond().setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
                    duration = duration - diff;
                    List<Double> extinfList = chukasaModel.getExtinfList();
                    extinfList.add(segmentedTsDuration); // TODO: temporary
                    chukasaModel.setExtinfList(extinfList);
                    chukasaModel.setDuration(duration);

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

                    if(!chukasaModel.getChukasaSettings().getStreamingType().equals(StreamingType.OKKAKE)) {
                        // process after spliting MPEG2-TS
                        if (chukasaModel.getChukasaSettings().isCanEncrypt()) {
                            Encrypter encrypter = new Encrypter(adaptiveBitrateStreaming, chukasaModelManagementComponent);
                            Thread thread = new Thread(encrypter);
                            thread.start();
                        }
                    }else{
                        chukasaModel.setSeqTsOkkake(chukasaModel.getSeqTsEnc());
                        chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);

                        SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
                        FFmpegRunner fFmpegRunner = new FFmpegRunner(adaptiveBitrateStreaming, chukasaModelManagementComponent);
                        taskExecutor.execute(fFmpegRunner);
                    }

                    flagCreateFile = true;
                    chukasaModel.setFlagSegFullDuration(true);
                    chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);

                    countPacket = toBeSegmentedPostion;

                    break loop;
                }

                if(isPAT && !isNextPAT) {
                    bos.write(buf, 0, buf.length);
                }
                countPacket++;

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

                        if(isNextPAT){
                            chukasaModel.setNextInit(pcrSec);
                            chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);
                            canSegment = true;
                        }

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

                            isElapsed = true;

                            chukasaModel.setLastPcrSecond(pcrSec);
                            chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);/*
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

                            break loop;*/
                        }

                    } // if flagPCR

                } // if SYNC_WORD

            } // loop : while

            if(!canSegment){
                double duration = chukasaModel.getNextInit().subtract(chukasaModel.getInitPcrSecond()).setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
                double diff = chukasaModel.getDiffPcrSecond().setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
                duration = duration - diff;
                List<Double> extinfList = chukasaModel.getExtinfList();
                extinfList.add(segmentedTsDuration); // TODO: temporary
                chukasaModel.setExtinfList(extinfList);
                chukasaModel.setDuration(duration);

                chukasaModel.setDiffPcrSecond(chukasaModel.getLastPcrSecond().subtract(chukasaModel.getInitPcrSecond()).subtract(new BigDecimal(Double.toString(segmentedTsDuration))));
                chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);

                log.info("[chukasaModel.getInitPcrSecond()] {}", chukasaModel.getInitPcrSecond());
                log.info("[chukasaModel.getLastPcrSecond()] {}", chukasaModel.getLastPcrSecond());
                log.info("[chukasaModel.getDiffPcrSecond()] {}", chukasaModel.getDiffPcrSecond());
                log.info("[Double.toString(DURATION))] {}", new BigDecimal(Double.toString(segmentedTsDuration)));
            }

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

                if(!chukasaModel.getChukasaSettings().getStreamingType().equals(StreamingType.OKKAKE)) {
                    if (chukasaModel.getChukasaSettings().isCanEncrypt()) {
                        Encrypter encrypter = new Encrypter(adaptiveBitrateStreaming, chukasaModelManagementComponent);
                        Thread thread = new Thread(encrypter);
                        thread.start();
                    }
                }else{
                    chukasaModel.setSeqTsOkkake(chukasaModel.getSeqTsEnc());
                    chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);
                    SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
                    FFmpegRunner fFmpegRunner = new FFmpegRunner(adaptiveBitrateStreaming, chukasaModelManagementComponent);
                    taskExecutor.execute(fFmpegRunner);
                }

                chukasaModel.setFlagTimerSegmenter(true);
                chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);

            }

            long[] ret = new long[2];
            ret[0] = (MPEG2_TS_PACKET_LENGTH  * (countPacket)) + readByte; // MPEG2-TS byte length ever read
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
