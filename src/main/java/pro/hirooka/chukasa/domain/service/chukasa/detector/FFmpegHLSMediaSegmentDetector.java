package pro.hirooka.chukasa.domain.service.chukasa.detector;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import pro.hirooka.chukasa.domain.event.LastMediaSegmentSequenceEvent;
import pro.hirooka.chukasa.domain.model.chukasa.ChukasaModel;
import pro.hirooka.chukasa.domain.model.chukasa.constants.ChukasaConstant;
import pro.hirooka.chukasa.domain.model.chukasa.enums.PlaylistType;
import pro.hirooka.chukasa.domain.service.chukasa.IChukasaModelManagementComponent;
import pro.hirooka.chukasa.domain.service.chukasa.playlister.IPlaylistBuilder;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.security.*;

import static java.util.Objects.requireNonNull;

@Slf4j
@Component
public class FFmpegHLSMediaSegmentDetector implements Runnable {

    private final String FILE_SEPARATOR = ChukasaConstant.FILE_SEPARATOR;
    private final int MPEG2_TS_PACKET_LENGTH = ChukasaConstant.MPEG2_TS_PACKET_LENGTH;
    private final String STREAM_FILE_NAME_PREFIX = ChukasaConstant.STREAM_FILE_NAME_PREFIX;
    private final String STREAM_FILE_EXTENSION = ChukasaConstant.STREAM_FILE_EXTENSION;
    private final String HLS_IV_FILE_EXTENSION = ChukasaConstant.HLS_IV_FILE_EXTENSION;

    @Setter
    private int adaptiveBitrateStreaming;
    private final IChukasaModelManagementComponent chukasaModelManagementComponent;
    private final IPlaylistBuilder playlistBuilder;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public FFmpegHLSMediaSegmentDetector(IChukasaModelManagementComponent chukasaModelManagementComponent, IPlaylistBuilder playlistBuilder, ApplicationEventPublisher applicationEventPublisher) {
        this.chukasaModelManagementComponent = requireNonNull(chukasaModelManagementComponent, "chukasaModelManagementComponent");
        this.playlistBuilder = requireNonNull(playlistBuilder, "playlistBuilder");
        this.applicationEventPublisher = requireNonNull(applicationEventPublisher, "applicationEventPublisher");
    }

    @Override
    public void run() {

        final ChukasaModel chukasaModel = chukasaModelManagementComponent.get(adaptiveBitrateStreaming);
        final int sequenceMediaSegment = chukasaModel.getSequenceMediaSegment();
        final boolean canEncrypt = chukasaModel.getChukasaSettings().isCanEncrypt();
        final String mediaPath = chukasaModel.getStreamPath();
        final String encryptedMediaTemporaryPath = chukasaModel.getTempEncPath();
        log.info("sequenceMediaSegment = {}", sequenceMediaSegment);

        final String commonPath = FILE_SEPARATOR + STREAM_FILE_NAME_PREFIX + (sequenceMediaSegment + 1) + STREAM_FILE_EXTENSION;
        final String mediaSegmentPath;
        if(canEncrypt){
            mediaSegmentPath = encryptedMediaTemporaryPath + commonPath;
        }else{
            mediaSegmentPath = mediaPath + commonPath;
        }
        log.info("mediaSegmentPath = {}", mediaSegmentPath);

        File mediaSegmentFile = new File(mediaSegmentPath);
        if (mediaSegmentFile.exists()) {
            log.info("file exists: {}", mediaSegmentFile.getAbsolutePath());
            final String nextCommonPath = FILE_SEPARATOR + STREAM_FILE_NAME_PREFIX + (sequenceMediaSegment + 2) + STREAM_FILE_EXTENSION;
            final String nextMediaSegmentPath;
            if(canEncrypt){
                nextMediaSegmentPath = encryptedMediaTemporaryPath + nextCommonPath;
            }else{
                nextMediaSegmentPath = mediaPath + nextCommonPath;
            }
            final File nextMediaSegmentFile = new File(nextMediaSegmentPath);
            if (nextMediaSegmentFile.exists()) {
                log.info("file exists: {}", nextMediaSegmentFile.getAbsolutePath());
                if(canEncrypt) {
                    try {
                        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
                        final int HLS_KEY_LENGTH = ChukasaConstant.HLS_KEY_LENGTH;
                        final Key key = makeKey(HLS_KEY_LENGTH);
                        final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
                        cipher.init(Cipher.ENCRYPT_MODE, key);

                        final String keyPrefix = RandomStringUtils.randomAlphabetic(10);
                        final String HLS_KEY_FILE_EXTENSION = ChukasaConstant.HLS_KEY_FILE_EXTENSION;
                        final FileOutputStream keyFileOutputStream = new FileOutputStream(mediaPath + FILE_SEPARATOR + keyPrefix + (sequenceMediaSegment + 1) + HLS_KEY_FILE_EXTENSION);

                        chukasaModel.getKeyArrayList().add(keyPrefix);

                        assert key != null;
                        final byte[] keyByteArray = key.getEncoded();
                        keyFileOutputStream.write(keyByteArray);
                        keyFileOutputStream.close();

                        final byte[] ivArray = cipher.getIV();

                        String ivHex = "";
                        for(byte iv : ivArray){
                            final String ivHexTmp = String.format("%02x", iv).toUpperCase();
                            ivHex = ivHex + ivHexTmp;
                        }

                        final String ivPrefix = RandomStringUtils.randomAlphabetic(10);
                        final FileWriter ivFileWriter = new FileWriter(mediaPath + FILE_SEPARATOR + ivPrefix + (sequenceMediaSegment + 1) + HLS_IV_FILE_EXTENSION);
                        ivFileWriter.write(ivHex);
                        ivFileWriter.close();

                        chukasaModel.getIvArrayList().add(ivHex);

                        final BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(encryptedMediaTemporaryPath + FILE_SEPARATOR + STREAM_FILE_NAME_PREFIX + (sequenceMediaSegment + 1) + STREAM_FILE_EXTENSION));
                        final FileOutputStream fileOutputStream = new FileOutputStream(mediaPath + FILE_SEPARATOR + STREAM_FILE_NAME_PREFIX + (sequenceMediaSegment + 1) + STREAM_FILE_EXTENSION);
                        final CipherOutputStream cipherOutputStream = new CipherOutputStream(fileOutputStream, cipher);

                        final byte[] buf = new byte[MPEG2_TS_PACKET_LENGTH];

                        int ch;
                        while ((ch = bufferedInputStream.read(buf)) != -1) {
                            cipherOutputStream.write(buf, 0, ch);
                        }
                        cipherOutputStream.close();
                        fileOutputStream.close();
                        bufferedInputStream.close();

                        // PlaylistType に関わらず，テンポラリディレクトリ内の過去の不要なファイルを削除する．
                        final File temporaryTSFile = new File(encryptedMediaTemporaryPath + FILE_SEPARATOR + STREAM_FILE_NAME_PREFIX + (sequenceMediaSegment) + STREAM_FILE_EXTENSION);
                        if (temporaryTSFile.exists()) {
                            temporaryTSFile.delete();
                        }
                        // LIVE プレイリストの場合は過去の不要なファイルを削除する．
                        if (chukasaModel.getChukasaSettings().getPlaylistType() == PlaylistType.LIVE) {
                            final int URI_IN_PLAYLIST = chukasaModel.getHlsConfiguration().getUriInPlaylist();
                            for (int i = 0; i < sequenceMediaSegment - 3 * URI_IN_PLAYLIST; i++) {
                                final File oldMediaSegmentFile = new File(mediaPath + FILE_SEPARATOR + STREAM_FILE_NAME_PREFIX + i + STREAM_FILE_EXTENSION);
                                if (oldMediaSegmentFile.exists()) {
                                    oldMediaSegmentFile.delete();
                                }
                                final File oldKeyFile = new File(mediaPath + FILE_SEPARATOR + keyPrefix + i + HLS_KEY_FILE_EXTENSION);
                                if (oldKeyFile.exists()) {
                                    oldKeyFile.delete();
                                }
                                final File oldIVFile = new File(mediaPath + FILE_SEPARATOR + ivPrefix + i + HLS_IV_FILE_EXTENSION);
                                if (oldIVFile.exists()) {
                                    oldIVFile.delete();
                                }
                                final File oldEncryptedMediaSegmentTemporaryFile = new File(encryptedMediaTemporaryPath + FILE_SEPARATOR + STREAM_FILE_NAME_PREFIX + i + STREAM_FILE_EXTENSION);
                                if (oldEncryptedMediaSegmentTemporaryFile.exists()) {
                                    oldEncryptedMediaSegmentTemporaryFile.delete();
                                }
                            }
                        }

                    } catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException | InvalidKeyException | IOException e) {
                        log.error("{} {}", e.getMessage(), e);
                    }
                }else{
                    if(chukasaModel.getChukasaSettings().getPlaylistType() == PlaylistType.LIVE) {
                        final long URI_IN_PLAYLIST = chukasaModel.getHlsConfiguration().getUriInPlaylist();
                        for (int i = 0; i < sequenceMediaSegment - URI_IN_PLAYLIST - URI_IN_PLAYLIST; i++) {
                            final File oldMediaSegmentFile = new File(mediaPath + FILE_SEPARATOR + STREAM_FILE_NAME_PREFIX + i + STREAM_FILE_EXTENSION);
                            if (oldMediaSegmentFile.exists()) {
                                oldMediaSegmentFile.delete();
                            }
                        }
                    }
                }

                final int nextSequenceMediaSegment = sequenceMediaSegment + 1;
                chukasaModel.setSequenceMediaSegment(nextSequenceMediaSegment);
                chukasaModel.getExtinfList().add((double)chukasaModel.getHlsConfiguration().getDuration());
                chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);

                //
                final int lastSequenceMediaSegment = chukasaModel.getSequenceLastMediaSegment();
                log.info("ls = {}", lastSequenceMediaSegment);
                if(lastSequenceMediaSegment > -1){
                    if(sequenceMediaSegment >= lastSequenceMediaSegment - (chukasaModel.getHlsConfiguration().getUriInPlaylist() - 1)){
                        applicationEventPublisher.publishEvent(new LastMediaSegmentSequenceEvent(this, adaptiveBitrateStreaming));
                    }
                }
            }

        }
        playlistBuilder.build();

    }

    private Key makeKey(int keyBit) {
        try {
            final KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            final SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            keyGenerator.init(keyBit, secureRandom);
            final Key generatedKey = keyGenerator.generateKey();
            return generatedKey;
        } catch (NoSuchAlgorithmException e) {
            log.error("{} {}", e.getMessage(), e);
            return null;
        }
    }

}
