package pro.hirooka.chukasa.domain.service.chukasa.encrypter;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import pro.hirooka.chukasa.domain.model.chukasa.constants.ChukasaConstant;
import pro.hirooka.chukasa.domain.model.chukasa.ChukasaModel;
import pro.hirooka.chukasa.domain.model.chukasa.enums.PlaylistType;
import pro.hirooka.chukasa.domain.service.chukasa.IChukasaModelManagementComponent;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.security.*;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import static java.util.Objects.requireNonNull;

@Deprecated
@Slf4j
public class FFmpegHLSEncrypter extends TimerTask {

    static final String FILE_SEPARATOR = System.getProperty("file.separator");

    final String STREAM_FILE_NAME_PREFIX = ChukasaConstant.STREAM_FILE_NAME_PREFIX;
    final String STREAM_FILE_EXTENSION = ChukasaConstant.STREAM_FILE_EXTENSION;
    final String FFMPEG_HLS_M3U8_FILE_NAME = ChukasaConstant.FFMPEG_HLS_M3U8_FILE_NAME;
    final String M3U8_FILE_EXTENSION = ChukasaConstant.M3U8_FILE_EXTENSION;
    final String HLS_KEY_FILE_EXTENSION = ChukasaConstant.HLS_KEY_FILE_EXTENSION;
    final String HLS_IV_FILE_EXTENSION = ChukasaConstant.HLS_IV_FILE_EXTENSION;
    final int HLS_KEY_LENGTH = ChukasaConstant.HLS_KEY_LENGTH;
    final int MPEG2_TS_PACKET_LENGTH = ChukasaConstant.MPEG2_TS_PACKET_LENGTH;

    private int adaptiveBitrateStreaming;

    private IChukasaModelManagementComponent chukasaModelManagementComponent;

    public FFmpegHLSEncrypter(int adaptiveBitrateStreaming, IChukasaModelManagementComponent chukasaModelManagementComponent){
        this.adaptiveBitrateStreaming = adaptiveBitrateStreaming;
        this.chukasaModelManagementComponent = requireNonNull(chukasaModelManagementComponent, "chukasaModelManagementComponent");
    }

    @Override
    public void run() {

        ChukasaModel chukasaModel = chukasaModelManagementComponent.get(adaptiveBitrateStreaming);
        int sequenceTS = chukasaModel.getSeqTs();
        String streamPath = chukasaModel.getStreamPath();
        String temporaryStreamPath = chukasaModel.getTempEncPath();
        log.info("sequenceTS = {}", sequenceTS);
        log.debug("streamPath = {}", streamPath);
        log.debug("temporaryStreamPath = {}", temporaryStreamPath);

        String tsPath = chukasaModel.getTempEncPath() + FILE_SEPARATOR + STREAM_FILE_NAME_PREFIX + (sequenceTS + 1) + STREAM_FILE_EXTENSION;
        File file = new File(tsPath);
        if (file.exists()) {
            log.info("file exists: {}", file.getAbsolutePath());
            String nextTSPath = chukasaModel.getTempEncPath() + FILE_SEPARATOR + STREAM_FILE_NAME_PREFIX + (sequenceTS + 2) + STREAM_FILE_EXTENSION;
            File nextFile = new File(nextTSPath);
            if (nextFile.exists()) {
                log.info("file exists: {}", nextFile.getAbsolutePath());

                try {
                    Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
                    Key key = makeKey(HLS_KEY_LENGTH);
                    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
                    cipher.init(Cipher.ENCRYPT_MODE, key);

                    String keyPrefix = RandomStringUtils.randomAlphabetic(10);
                    FileOutputStream keyFileOutputStream = new FileOutputStream(streamPath + FILE_SEPARATOR + keyPrefix + (sequenceTS + 1) + HLS_KEY_FILE_EXTENSION);

                    chukasaModel.getKeyArrayList().add(keyPrefix);

                    byte[] keyByteArray = key.getEncoded();
                    keyFileOutputStream.write(keyByteArray);
                    keyFileOutputStream.close();

                    byte[] iv = cipher.getIV();

                    String ivHex = "";
                    for(int i = 0; i < iv.length; i++){
                        String ivHexTmp = String.format("%02x", iv[i]).toUpperCase();
                        ivHex = ivHex + ivHexTmp;
                    }

                    String ivPrefix = RandomStringUtils.randomAlphabetic(10);
                    FileWriter ivFileWriter = new FileWriter(streamPath + FILE_SEPARATOR + ivPrefix + (sequenceTS + 1) + HLS_IV_FILE_EXTENSION);
                    ivFileWriter.write(ivHex);
                    ivFileWriter.close();

                    chukasaModel.getIvArrayList().add(ivHex);

                    BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(temporaryStreamPath + FILE_SEPARATOR + STREAM_FILE_NAME_PREFIX + (sequenceTS + 1) + STREAM_FILE_EXTENSION));
                    FileOutputStream fileOutputStream = new FileOutputStream(streamPath + FILE_SEPARATOR + STREAM_FILE_NAME_PREFIX + (sequenceTS + 1) + STREAM_FILE_EXTENSION);
                    CipherOutputStream cipherOutputStream = new CipherOutputStream(fileOutputStream, cipher);

                    byte[] buf = new byte[MPEG2_TS_PACKET_LENGTH];

                    int ch;
                    while((ch = bufferedInputStream.read(buf)) != -1){
                        cipherOutputStream.write(buf, 0, ch);
                    }
                    cipherOutputStream.close();
                    fileOutputStream.close();
                    bufferedInputStream.close();

                    sequenceTS = sequenceTS + 1;
                    chukasaModel.setSeqTs(sequenceTS);

                    // FFmpeg のプレイリストから EXTINF を読み取るのはいったんやめるです．強制的に値を設定するです．
//                    List<Double> extinfList = chukasaModel.getExtinfList();
//                    List<Double> ffmpegM3U8EXTINFList = getEXTINFList(temporaryStreamPath + FILE_SEPARATOR + FFMPEG_HLS_M3U8_FILE_NAME + M3U8_FILE_EXTENSION);
//                    if (sequenceTS >= 0 && ffmpegM3U8EXTINFList.size() > 0) {
//                        extinfList.add(ffmpegM3U8EXTINFList.get(sequenceTS));
//                    } else {
//                        extinfList.add((double) chukasaModel.getHlsConfiguration().getDuration());
//                    }
//                    chukasaModel.setExtinfList(extinfList);
                    chukasaModel.getExtinfList().add((double)chukasaModel.getHlsConfiguration().getDuration());

                    chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);

                    // PlaylistType に関わらず，テンポラリディレクトリ内の過去の不要なファイルを削除する．
                    File temporaryTSFile = new File(temporaryStreamPath + FILE_SEPARATOR + STREAM_FILE_NAME_PREFIX + (sequenceTS) + STREAM_FILE_EXTENSION);
                    if(temporaryTSFile.exists()) {
                        temporaryTSFile.delete();
                    }
                    // LIVE プレイリストの場合は過去の不要なファイルを削除する．
                    if(chukasaModel.getChukasaSettings().getPlaylistType().equals(PlaylistType.LIVE)) {
                        int sequencePlaylist = chukasaModel.getSeqPl();
                        int URI_IN_PLAYLIST = chukasaModel.getHlsConfiguration().getUriInPlaylist();
                        for (int i = 0; i < sequencePlaylist - URI_IN_PLAYLIST - URI_IN_PLAYLIST; i++) {
                            File oldTSFile = new File(streamPath + FILE_SEPARATOR + STREAM_FILE_NAME_PREFIX + i + STREAM_FILE_EXTENSION);
                            if (oldTSFile.exists()) {
                                oldTSFile.delete();
                            }
                            File oldKeyFile = new File(streamPath + FILE_SEPARATOR + keyPrefix + i + HLS_KEY_FILE_EXTENSION);
                            if(oldKeyFile.exists()){
                                oldKeyFile.delete();
                            }
                            File oldIVFile = new File(streamPath + FILE_SEPARATOR + ivPrefix + i + HLS_IV_FILE_EXTENSION);
                            if(oldIVFile.exists()){
                                oldIVFile.delete();
                            }
                        }
                    }

                } catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException | InvalidKeyException | IOException e) {
                    log.error("{} {}", e.getMessage(), e);
                }
            }
        }
    }

    private Key makeKey(int keyBit) {
        try {
            KeyGenerator kg = KeyGenerator.getInstance("AES");
            SecureRandom rd = SecureRandom.getInstance("SHA1PRNG");
            kg.init(keyBit, rd);
            Key key = kg.generateKey();
            return key;
        } catch (NoSuchAlgorithmException e) {
            log.error("{} {}", e.getMessage(), e);
        }
        return null;
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
