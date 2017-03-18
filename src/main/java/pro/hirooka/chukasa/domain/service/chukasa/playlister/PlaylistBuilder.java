package pro.hirooka.chukasa.domain.service.chukasa.playlister;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.hirooka.chukasa.domain.model.chukasa.ChukasaModel;
import pro.hirooka.chukasa.domain.model.chukasa.constants.ChukasaConstant;
import pro.hirooka.chukasa.domain.model.chukasa.enums.PlaylistType;
import pro.hirooka.chukasa.domain.service.chukasa.IChukasaModelManagementComponent;

import java.io.*;

import static java.util.Objects.requireNonNull;

@Slf4j
@Component
public class PlaylistBuilder implements IPlaylistBuilder{

    final String FILE_SEPARATOR = ChukasaConstant.FILE_SEPARATOR;
    final String initialStreamPath = ChukasaConstant.INITIAL_STREAM_PATH;
    final String STREAM_FILE_NAME_PREFIX = ChukasaConstant.STREAM_FILE_NAME_PREFIX;
    final String STREAM_FILE_EXTENSION = ChukasaConstant.STREAM_FILE_EXTENSION;
    final String M3U8_FILE_NAME = ChukasaConstant.M3U8_FILE_NAME;
    final String M3U8_FILE_EXTENSION = ChukasaConstant.M3U8_FILE_EXTENSION;

    @Setter
    private int adaptiveBitrateStreaming;

    private final IChukasaModelManagementComponent chukasaModelManagementComponent;

    @Autowired
    public PlaylistBuilder(IChukasaModelManagementComponent chukasaModelManagementComponent) {
        this.chukasaModelManagementComponent = requireNonNull(chukasaModelManagementComponent, "chukasaModelManagementComponent");
    }

    public void build() {

        try {

            final ChukasaModel chukasaModel = chukasaModelManagementComponent.get(adaptiveBitrateStreaming);

            final int URI_IN_PLAYLIST = chukasaModel.getHlsConfiguration().getUriInPlaylist();
            final int TARGET_DURATION = chukasaModel.getHlsConfiguration().getDuration() + 1;
            final double DURATION = (double) chukasaModel.getHlsConfiguration().getDuration();
            final PlaylistType playlistType = chukasaModel.getChukasaSettings().getPlaylistType();
            final String playlistPath = chukasaModel.getStreamPath() + FILE_SEPARATOR + M3U8_FILE_NAME + M3U8_FILE_EXTENSION;
            final boolean canEncrypt = chukasaModel.getChukasaSettings().isCanEncrypt();

            final int sequenceMediaSegment = chukasaModel.getSequenceMediaSegment();
            final int sequencePlaylist = chukasaModel.getSequencePlaylist();
            log.info("sequenceMediaSegment = {}, sequencePlaylist = {}", sequenceMediaSegment, sequencePlaylist);

            // イニシャルストリームのみか否か。
            // sequenceMediaSegment が 0 以上にならない限りイニシャルストリームを流し続ける。
            if(sequenceMediaSegment >= 0){

                try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(playlistPath)))) {

                    bufferedWriter.write("#EXTM3U");
                    bufferedWriter.newLine();
                    bufferedWriter.write("#EXT-X-VERSION:7");
                    bufferedWriter.newLine();
                    bufferedWriter.write("#EXT-X-TARGETDURATION:" + TARGET_DURATION);
                    bufferedWriter.newLine();

                    // MIX STREAM or ONLY LIVE STREAM
                    if (URI_IN_PLAYLIST - 1 > sequenceMediaSegment) {
                        // MIX STREAM
                        log.info("MIX STREAM");

                        final int initialSequenceInPlaylist = chukasaModel.getSequenceInitialPlaylist() + 1;
                        chukasaModel.setSequenceInitialPlaylist(initialSequenceInPlaylist);

                        if (playlistType == PlaylistType.LIVE) {
                            bufferedWriter.write("#EXT-X-MEDIA-SEQUENCE:" + initialSequenceInPlaylist);
                            bufferedWriter.newLine();
                            bufferedWriter.write("#EXT-X-DISCONTINUITY-SEQUENCE:" + 1);
                        } else if (playlistType == PlaylistType.EVENT) {
                            bufferedWriter.write("#EXT-X-MEDIA-SEQUENCE:0");
                        }
                        bufferedWriter.newLine();

                        if (playlistType == PlaylistType.LIVE) {
                            for (int i = initialSequenceInPlaylist; i < initialSequenceInPlaylist + URI_IN_PLAYLIST - (sequenceMediaSegment + 1); i++) {
                                bufferedWriter.write("#EXTINF:" + DURATION + ",");
                                bufferedWriter.newLine();
                                bufferedWriter.write(initialStreamPath + "/" + "i" + i + STREAM_FILE_EXTENSION);
                                bufferedWriter.newLine();
                            }
                        } else if (playlistType == PlaylistType.EVENT) {
                            for (int i = 0; i < initialSequenceInPlaylist + URI_IN_PLAYLIST - (sequenceMediaSegment + 1); i++) {
                                bufferedWriter.write("#EXTINF:" + DURATION + ",");
                                bufferedWriter.newLine();
                                bufferedWriter.write(initialStreamPath + "/" + "i" + i + STREAM_FILE_EXTENSION);
                                bufferedWriter.newLine();
                            }
                        }

                        bufferedWriter.write("#EXT-X-DISCONTINUITY");
                        bufferedWriter.newLine();

                        for (int i = 0; i < sequenceMediaSegment + 1; i++) {
                            if(canEncrypt) {
                                bufferedWriter.write("#EXT-X-KEY:METHOD=AES-128,URI=");
                                bufferedWriter.write("\"" + "" + chukasaModel.getKeyArrayList().get(i) + i + ".key\"" + ",IV=0x");
                                bufferedWriter.write(chukasaModel.getIvArrayList().get(i));
                                bufferedWriter.newLine();
                            }
                            bufferedWriter.write("#EXTINF:" + chukasaModel.getExtinfList().get(i) + ",");
                            bufferedWriter.newLine();
                            bufferedWriter.write(STREAM_FILE_NAME_PREFIX + i + STREAM_FILE_EXTENSION);
                            bufferedWriter.newLine();
                        }

                    } else {
                        // ONLY LIVE STREAM
                        log.info("ONLY LIVE STREAM");

                        if (playlistType == PlaylistType.LIVE) {
                            final int extXMmediaSequence = sequenceMediaSegment - (URI_IN_PLAYLIST - 1) + chukasaModel.getSequenceInitialPlaylist() + 1;
                            bufferedWriter.write("#EXT-X-MEDIA-SEQUENCE:" + extXMmediaSequence);
                            bufferedWriter.newLine();
                            bufferedWriter.write("#EXT-X-DISCONTINUITY-SEQUENCE:" + 1);
                        } else if (playlistType == PlaylistType.EVENT) {
                            bufferedWriter.write("#EXT-X-MEDIA-SEQUENCE:0");
                        }
                        bufferedWriter.newLine();

                        if (playlistType == PlaylistType.EVENT) {
                            final int sequenceInitialPlaylist = chukasaModel.getSequenceInitialPlaylist();
                            for (int i = 0; i < sequenceInitialPlaylist + 1; i++) {
                                bufferedWriter.write("#EXTINF:" + DURATION + ",");
                                bufferedWriter.newLine();
                                bufferedWriter.write(initialStreamPath + "/" + "i" + i + STREAM_FILE_EXTENSION);
                                bufferedWriter.newLine();
                            }
                            bufferedWriter.write("#EXT-X-DISCONTINUITY");
                            bufferedWriter.newLine();
                        }

                        if (playlistType == PlaylistType.LIVE) {
                            for (int i = sequenceMediaSegment - (URI_IN_PLAYLIST - 1); i < sequenceMediaSegment + URI_IN_PLAYLIST - (URI_IN_PLAYLIST - 1); i++) {
                                if(canEncrypt) {
                                    bufferedWriter.write("#EXT-X-KEY:METHOD=AES-128,URI=");
                                    bufferedWriter.write("\"" + "" + chukasaModel.getKeyArrayList().get(i) + i + ".key\"" + ",IV=0x");
                                    bufferedWriter.write(chukasaModel.getIvArrayList().get(i));
                                    bufferedWriter.newLine();
                                }
                                bufferedWriter.write("#EXTINF:" + chukasaModel.getExtinfList().get(i) + ",");
                                bufferedWriter.newLine();
                                bufferedWriter.write(STREAM_FILE_NAME_PREFIX + i + STREAM_FILE_EXTENSION);
                                bufferedWriter.newLine();
                            }
                        } else if (playlistType == PlaylistType.EVENT) {
                            for (int i = 0; i < sequenceMediaSegment + URI_IN_PLAYLIST; i++) {
                                if(canEncrypt) {
                                    bufferedWriter.write("#EXT-X-KEY:METHOD=AES-128,URI=");
                                    bufferedWriter.write("\"" + "" + chukasaModel.getKeyArrayList().get(i) + i + ".key\"" + ",IV=0x");
                                    bufferedWriter.write(chukasaModel.getIvArrayList().get(i));
                                    bufferedWriter.newLine();
                                }
                                bufferedWriter.write("#EXTINF:" + chukasaModel.getExtinfList().get(i) + ",");
                                bufferedWriter.newLine();
                                bufferedWriter.write(STREAM_FILE_NAME_PREFIX + i + STREAM_FILE_EXTENSION);
                                bufferedWriter.newLine();
                            }
                        }
                    }
                    final int lastSequenceMediaSegment = chukasaModel.getSequenceLastMediaSegment();
                    if(lastSequenceMediaSegment > -1){
                        if(sequenceMediaSegment >= lastSequenceMediaSegment - (URI_IN_PLAYLIST - 1)){
                            bufferedWriter.write("#EXT-X-ENDLIST");
                            log.info("end of playlist: {}", lastSequenceMediaSegment);
                        }
                    }
                }
                chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);

            } else {

                // FFmpeg で生成されるストリームを検出できないため、イニシャルストリームのみを流すプレイリスト

                // ONLY INITIAL STREAM //
                log.info("ONLY INITIAL STREAM");

                final int sequenceInitialPlaylist;
                if (playlistType == PlaylistType.LIVE) {
                    sequenceInitialPlaylist = chukasaModel.getSequenceInitialPlaylist() + 1;
                } else if(playlistType == PlaylistType.EVENT) {
                    sequenceInitialPlaylist = 0;
                } else {
                    sequenceInitialPlaylist = 0;
                }

                try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(playlistPath)))) {

                    bufferedWriter.write("#EXTM3U");
                    bufferedWriter.newLine();
                    bufferedWriter.write("#EXT-X-VERSION:7");
                    bufferedWriter.newLine();
                    bufferedWriter.write("#EXT-X-TARGETDURATION:" + TARGET_DURATION);
                    bufferedWriter.newLine();
                    bufferedWriter.write("#EXT-X-MEDIA-SEQUENCE:" + sequenceInitialPlaylist);
                    bufferedWriter.newLine();

                    if(playlistType == PlaylistType.LIVE) {
                        for (int i = sequenceInitialPlaylist; i < sequenceInitialPlaylist + URI_IN_PLAYLIST; i++) {
                            bufferedWriter.write("#EXTINF:" + Double.toString(DURATION) + ",");
                            bufferedWriter.newLine();
                            bufferedWriter.write(initialStreamPath + "/" + "i" + i + STREAM_FILE_EXTENSION);
                            bufferedWriter.newLine();
                        }
                    }else if(playlistType == PlaylistType.EVENT){
                        for (int i = 0; i < sequenceInitialPlaylist + URI_IN_PLAYLIST; i++) {
                            bufferedWriter.write("#EXTINF:" + Double.toString(DURATION) + ",");
                            bufferedWriter.newLine();
                            bufferedWriter.write(initialStreamPath + "/" + "i" + i + STREAM_FILE_EXTENSION);
                            bufferedWriter.newLine();
                        }
                    }else{

                    }
                }

                chukasaModel.setSequenceInitialPlaylist(sequenceInitialPlaylist);
                chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);
            }

            BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(playlistPath)));
            String s;
            while((s = bufferedReader.readLine()) != null){
                System.out.println(s);
            }

        } catch (IOException e) {
            log.error("{} {}", e.getMessage(), e);
        }
    }
}
