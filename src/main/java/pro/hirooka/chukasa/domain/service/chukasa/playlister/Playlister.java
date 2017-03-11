package pro.hirooka.chukasa.domain.service.chukasa.playlister;

import lombok.extern.slf4j.Slf4j;
import pro.hirooka.chukasa.domain.model.chukasa.constants.ChukasaConstant;
import pro.hirooka.chukasa.domain.model.chukasa.ChukasaModel;
import pro.hirooka.chukasa.domain.model.chukasa.enums.PlaylistType;
import pro.hirooka.chukasa.domain.model.chukasa.enums.StreamingType;
import pro.hirooka.chukasa.domain.service.chukasa.IChukasaModelManagementComponent;

import java.io.*;
import java.util.TimerTask;

import static java.util.Objects.requireNonNull;

@Slf4j
public class Playlister extends TimerTask {

    static final String FILE_SEPARATOR = System.getProperty("file.separator");

    final String initialStreamPath = ChukasaConstant.INITIAL_STREAM_PATH;
    final String STREAM_FILE_NAME_PREFIX = ChukasaConstant.STREAM_FILE_NAME_PREFIX;
    final String STREAM_FILE_EXTENSION = ChukasaConstant.STREAM_FILE_EXTENSION;
    final String M3U8_FILE_NAME = ChukasaConstant.M3U8_FILE_NAME;
    final String M3U8_FILE_EXTENSION = ChukasaConstant.M3U8_FILE_EXTENSION;

    private int adaptiveBitrateStreaming;

    private IChukasaModelManagementComponent chukasaModelManagementComponent;

    public Playlister(int adaptiveBitrateStreaming, IChukasaModelManagementComponent chukasaModelManagementComponent) {
        this.adaptiveBitrateStreaming = adaptiveBitrateStreaming;
        this.chukasaModelManagementComponent = requireNonNull(chukasaModelManagementComponent, "chukasaModelManagementComponent");
    }

    @Override
    public void run() {

        try {

            ChukasaModel chukasaModel = chukasaModelManagementComponent.get(adaptiveBitrateStreaming);

            final int URI_IN_PLAYLIST = chukasaModel.getHlsConfiguration().getUriInPlaylist();
            final int TARGET_DURATION = chukasaModel.getHlsConfiguration().getDuration() + 1;

            int sequenceTS = chukasaModel.getSeqTs();
            int sequencePlaylist = chukasaModel.getSeqPl();
            log.info("sequenceTS = {}, sequencePlaylist = {}", sequenceTS, sequencePlaylist);

            boolean isOkkake = false;
            if(chukasaModel.getChukasaSettings().getStreamingType().equals(StreamingType.OKKAKE)){
                isOkkake = true;
            }

            // イニシャルストリームのみか否か．
            // sequenceTS が 0 以上にならない限りイニシャルストリームを流し続ける．
            if((!isOkkake && sequenceTS >= 0) || (isOkkake && sequenceTS >= URI_IN_PLAYLIST - 1)) {

                if(sequencePlaylist >= sequenceTS){
                    log.warn("skip Playlister");
                    return;
                }

                final PlaylistType PLAYLIST_TYPE = chukasaModel.getPlaylistType();
                final double DURATION = (double) chukasaModel.getHlsConfiguration().getDuration();
                final String PLAYLIST_FILE_PATH = chukasaModel.getStreamPath() + FILE_SEPARATOR + M3U8_FILE_NAME + M3U8_FILE_EXTENSION;

                sequencePlaylist = chukasaModel.getSeqPl();
                sequencePlaylist++;

                // 暗号化ストリームか否か
                if (chukasaModel.getChukasaSettings().isCanEncrypt()) {

                    try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(PLAYLIST_FILE_PATH)))) {

                        bufferedWriter.write("#EXTM3U");
                        bufferedWriter.newLine();
                        bufferedWriter.write("#EXT-X-VERSION:7");
                        bufferedWriter.newLine();
                        bufferedWriter.write("#EXT-X-TARGETDURATION:" + TARGET_DURATION);
                        bufferedWriter.newLine();

                        if (!(sequencePlaylist >= (URI_IN_PLAYLIST - 1))) {

                            // MIX STREAM //
                            log.info("MIX STREAM (Encryption)");

                            int initialSequenceInPlaylist = chukasaModel.getSequenceInitialPlaylist();
                            initialSequenceInPlaylist++;
                            chukasaModel.setSequenceInitialPlaylist(initialSequenceInPlaylist);

                            if (PLAYLIST_TYPE.equals(PlaylistType.LIVE)) {
                                bufferedWriter.write("#EXT-X-MEDIA-SEQUENCE:" + initialSequenceInPlaylist);
                                bufferedWriter.newLine();
                                bufferedWriter.write("#EXT-X-DISCONTINUITY-SEQUENCE:" + 1);
                            } else if (PLAYLIST_TYPE.equals(PlaylistType.EVENT)) {
                                bufferedWriter.write("#EXT-X-MEDIA-SEQUENCE:0");
                            }
                            bufferedWriter.newLine();

                            if (PLAYLIST_TYPE.equals(PlaylistType.LIVE)) {
                                for (int i = initialSequenceInPlaylist; i < initialSequenceInPlaylist + URI_IN_PLAYLIST - (sequencePlaylist + 1); i++) {
                                    bufferedWriter.write("#EXTINF:" + DURATION + ",");
                                    bufferedWriter.newLine();
                                    bufferedWriter.write(initialStreamPath + "/" + "i" + i + STREAM_FILE_EXTENSION);
                                    bufferedWriter.newLine();
                                }
                            } else if (PLAYLIST_TYPE.equals(PlaylistType.EVENT)) {
                                for (int i = 0; i < initialSequenceInPlaylist + URI_IN_PLAYLIST - (sequencePlaylist + 1); i++) {
                                    bufferedWriter.write("#EXTINF:" + DURATION + ",");
                                    bufferedWriter.newLine();
                                    bufferedWriter.write(initialStreamPath + "/" + "i" + i + STREAM_FILE_EXTENSION);
                                    bufferedWriter.newLine();
                                }
                            }

                            bufferedWriter.write("#EXT-X-DISCONTINUITY");
                            bufferedWriter.newLine();

                            if (PLAYLIST_TYPE.equals(PlaylistType.LIVE)) {
                                for (int i = 0; i < sequencePlaylist + 1; i++) {
                                    bufferedWriter.write("#EXT-X-KEY:METHOD=AES-128,URI=");
                                    bufferedWriter.write("\"" + "" + chukasaModel.getKeyArrayList().get(i) + i + ".key\"" + ",IV=0x");
                                    bufferedWriter.write(chukasaModel.getIvArrayList().get(i));
                                    bufferedWriter.newLine();
                                    bufferedWriter.write("#EXTINF:" + chukasaModel.getExtinfList().get(i) + ",");
                                    bufferedWriter.newLine();
                                    bufferedWriter.write(STREAM_FILE_NAME_PREFIX + i + STREAM_FILE_EXTENSION);
                                    bufferedWriter.newLine();
                                }
                            } else if (PLAYLIST_TYPE.equals(PlaylistType.EVENT)) {
                                for (int i = 0; i < sequencePlaylist + 1; i++) {
                                    bufferedWriter.write("#EXT-X-KEY:METHOD=AES-128,URI=");
                                    bufferedWriter.write("\"" + "" + chukasaModel.getKeyArrayList().get(i) + i + ".key\"" + ",IV=0x");
                                    bufferedWriter.write(chukasaModel.getIvArrayList().get(i));
                                    bufferedWriter.newLine();
                                    bufferedWriter.write("#EXTINF:" + chukasaModel.getExtinfList().get(i) + ",");
                                    bufferedWriter.newLine();
                                    bufferedWriter.write(STREAM_FILE_NAME_PREFIX + i + STREAM_FILE_EXTENSION);
                                    bufferedWriter.newLine();
                                }
                            }

                        } else {

                            // ONLY LIVE STREAM
                            log.info("ONLY LIVE STREAM (Encryption)");

                            if (PLAYLIST_TYPE.equals(PlaylistType.LIVE)) {
                                int mediaSequence = sequencePlaylist - (URI_IN_PLAYLIST - 1) + chukasaModel.getSequenceInitialPlaylist() + 1;
                                bufferedWriter.write("#EXT-X-MEDIA-SEQUENCE:" + mediaSequence);
                                bufferedWriter.newLine();
                                bufferedWriter.write("#EXT-X-DISCONTINUITY-SEQUENCE:" + 1);
                            } else if (PLAYLIST_TYPE.equals(PlaylistType.EVENT)) {
                                bufferedWriter.write("#EXT-X-MEDIA-SEQUENCE:0");
                            }
                            bufferedWriter.newLine();

                            if (PLAYLIST_TYPE.equals(PlaylistType.LIVE)) {

                            } else if (PLAYLIST_TYPE.equals(PlaylistType.EVENT)) {
                                for (int i = 0; i < chukasaModel.getSequenceInitialPlaylist() + URI_IN_PLAYLIST - 2; i++) {
                                    bufferedWriter.write("#EXTINF:" + DURATION + ",");
                                    bufferedWriter.newLine();
                                    bufferedWriter.write(initialStreamPath + "/" + "i" + i + STREAM_FILE_EXTENSION);
                                    bufferedWriter.newLine();
                                }
                                bufferedWriter.write("#EXT-X-DISCONTINUITY");
                                bufferedWriter.newLine();
                            }

                            if (PLAYLIST_TYPE.equals(PlaylistType.LIVE)) {
                                for (int i = sequencePlaylist - (URI_IN_PLAYLIST - 1); i < sequencePlaylist - (URI_IN_PLAYLIST - 1) + URI_IN_PLAYLIST; i++) {
                                    bufferedWriter.write("#EXT-X-KEY:METHOD=AES-128,URI=");
                                    bufferedWriter.write("\"" + "" + chukasaModel.getKeyArrayList().get(i) + i + ".key\"" + ",IV=0x");
                                    bufferedWriter.write(chukasaModel.getIvArrayList().get(i));
                                    bufferedWriter.newLine();
                                    bufferedWriter.write("#EXTINF:" + chukasaModel.getExtinfList().get(i) + ",");
                                    bufferedWriter.newLine();
                                    bufferedWriter.write(STREAM_FILE_NAME_PREFIX + i + STREAM_FILE_EXTENSION);
                                    bufferedWriter.newLine();
                                }
                            } else if (PLAYLIST_TYPE.equals(PlaylistType.EVENT)) {
                                for (int i = 0; i < (sequencePlaylist + URI_IN_PLAYLIST); i++) {
                                    bufferedWriter.write("#EXT-X-KEY:METHOD=AES-128,URI=");
                                    bufferedWriter.write("\"" + "" + chukasaModel.getKeyArrayList().get(i) + i + ".key\"" + ",IV=0x");
                                    bufferedWriter.write(chukasaModel.getIvArrayList().get(i));
                                    bufferedWriter.newLine();
                                    bufferedWriter.write("#EXTINF:" + chukasaModel.getExtinfList().get(i) + ",");
                                    bufferedWriter.newLine();
                                    bufferedWriter.write(STREAM_FILE_NAME_PREFIX + i + STREAM_FILE_EXTENSION);
                                    bufferedWriter.newLine();
                                }
                            }
                        }

                        if (chukasaModel.isFlagLastTs()) {
                            if (sequencePlaylist >= (chukasaModel.getSeqTsLast() - (URI_IN_PLAYLIST - 1))) {
                                bufferedWriter.write("#EXT-X-ENDLIST");
                                log.info("end of playlist: {}", (sequencePlaylist + URI_IN_PLAYLIST - 1));
                                chukasaModel.setFlagLastPl(true);
                                chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);
                            }
                        }
                    }

                    if (chukasaModel.isFlagLastPl()) {
                        chukasaModel.setFlagTimerPlaylister(true);
                        chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);
                    }

                } else {

                    // 暗号化されない

                    try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(PLAYLIST_FILE_PATH)))) {

                        bufferedWriter.write("#EXTM3U");
                        bufferedWriter.newLine();
                        bufferedWriter.write("#EXT-X-VERSION:7");
                        bufferedWriter.newLine();
                        bufferedWriter.write("#EXT-X-TARGETDURATION:" + TARGET_DURATION);
                        bufferedWriter.newLine();

                        if (!(sequencePlaylist >= (URI_IN_PLAYLIST - 1))) {

                            // MIX STREAM //
                            log.info("MIX STREAM (No Encryption)");

                            int initialStreamMediaSequencePlaylist = chukasaModel.getSequenceInitialPlaylist();
                            initialStreamMediaSequencePlaylist++;
                            chukasaModel.setSequenceInitialPlaylist(initialStreamMediaSequencePlaylist);

                            if (PLAYLIST_TYPE.equals(PlaylistType.LIVE)) {
                                bufferedWriter.write("#EXT-X-MEDIA-SEQUENCE:" + initialStreamMediaSequencePlaylist);
                                bufferedWriter.newLine();
                                bufferedWriter.write("#EXT-X-DISCONTINUITY-SEQUENCE:" + 1);
                            } else if (PLAYLIST_TYPE.equals(PlaylistType.EVENT)) {
                                bufferedWriter.write("#EXT-X-MEDIA-SEQUENCE:0");
                            }
                            bufferedWriter.newLine();

                            if (PLAYLIST_TYPE.equals(PlaylistType.LIVE)) {
                                for (int i = initialStreamMediaSequencePlaylist; i < initialStreamMediaSequencePlaylist + URI_IN_PLAYLIST - (sequencePlaylist + 1); i++) {
                                    bufferedWriter.write("#EXTINF:" + DURATION + ",");
                                    bufferedWriter.newLine();
                                    bufferedWriter.write(initialStreamPath + "/" + "i" + i + STREAM_FILE_EXTENSION);
                                    bufferedWriter.newLine();
                                }
                            } else if (PLAYLIST_TYPE.equals(PlaylistType.EVENT)) {
                                for (int i = 0; i < initialStreamMediaSequencePlaylist + URI_IN_PLAYLIST - (sequencePlaylist + 1); i++) {
                                    bufferedWriter.write("#EXTINF:" + DURATION + ",");
                                    bufferedWriter.newLine();
                                    bufferedWriter.write(initialStreamPath + "/" + "i" + i + STREAM_FILE_EXTENSION);
                                    bufferedWriter.newLine();
                                }
                            }

                            bufferedWriter.write("#EXT-X-DISCONTINUITY");
                            bufferedWriter.newLine();

                            if (PLAYLIST_TYPE.equals(PlaylistType.LIVE)) {
                                for (int i = 0; i < sequencePlaylist + 1; i++) {
                                    bufferedWriter.write("#EXTINF:" + chukasaModel.getExtinfList().get(i) + ",");
                                    bufferedWriter.newLine();
                                    bufferedWriter.write(STREAM_FILE_NAME_PREFIX + i + STREAM_FILE_EXTENSION);
                                    bufferedWriter.newLine();
                                }
                            } else if (PLAYLIST_TYPE.equals(PlaylistType.EVENT)) {
                                for (int i = 0; i < sequencePlaylist + 1; i++) {
                                    bufferedWriter.write("#EXTINF:" + chukasaModel.getExtinfList().get(i) + ",");
                                    bufferedWriter.newLine();
                                    bufferedWriter.write(STREAM_FILE_NAME_PREFIX + i + STREAM_FILE_EXTENSION);
                                    bufferedWriter.newLine();
                                }
                            }

                        } else {

                            // ONLY LIVE STREAM
                            log.info("ONLY LIVE STREAM (No Encryption)");

                            if (PLAYLIST_TYPE.equals(PlaylistType.LIVE)) {
                                int mediaSequence = sequencePlaylist - (URI_IN_PLAYLIST - 1) + chukasaModel.getSequenceInitialPlaylist() + 1;
                                bufferedWriter.write("#EXT-X-MEDIA-SEQUENCE:" + mediaSequence);
                                bufferedWriter.newLine();
                                bufferedWriter.write("#EXT-X-DISCONTINUITY-SEQUENCE:" + 1);
                            } else if (PLAYLIST_TYPE.equals(PlaylistType.EVENT)) {
                                bufferedWriter.write("#EXT-X-MEDIA-SEQUENCE:0");
                            }
                            bufferedWriter.newLine();

                            if (PLAYLIST_TYPE.equals(PlaylistType.LIVE)) {

                            } else if (PLAYLIST_TYPE.equals(PlaylistType.EVENT)) {
                                for (int i = 0; i < chukasaModel.getSequenceInitialPlaylist() + URI_IN_PLAYLIST - 2; i++) {
                                    bufferedWriter.write("#EXTINF:" + (double) chukasaModel.getHlsConfiguration().getDuration() + ",");
                                    bufferedWriter.newLine();
                                    bufferedWriter.write(initialStreamPath + "/" + "i" + i + STREAM_FILE_EXTENSION);
                                    bufferedWriter.newLine();
                                }
                                bufferedWriter.write("#EXT-X-DISCONTINUITY");
                                bufferedWriter.newLine();
                            }

                            if (PLAYLIST_TYPE.equals(PlaylistType.LIVE)) {
                                for (int i = sequencePlaylist - (URI_IN_PLAYLIST - 1); i < sequencePlaylist - (URI_IN_PLAYLIST - 1) + URI_IN_PLAYLIST; i++) {
                                    bufferedWriter.write("#EXTINF:" + chukasaModel.getExtinfList().get(i) + ",");
                                    bufferedWriter.newLine();
                                    bufferedWriter.write(STREAM_FILE_NAME_PREFIX + i + STREAM_FILE_EXTENSION);
                                    bufferedWriter.newLine();
                                }
                            } else if (PLAYLIST_TYPE.equals(PlaylistType.EVENT)) {
                                for (int i = 0; i < (sequencePlaylist + URI_IN_PLAYLIST); i++) {
                                    bufferedWriter.write("#EXTINF:" + chukasaModel.getExtinfList().get(i) + ",");
                                    bufferedWriter.newLine();
                                    bufferedWriter.write(STREAM_FILE_NAME_PREFIX + i + STREAM_FILE_EXTENSION);
                                    bufferedWriter.newLine();
                                }
                            }
                        }

                        if (chukasaModel.isFlagLastTs()) {
                            if (sequencePlaylist >= (chukasaModel.getSeqTsLast() - (URI_IN_PLAYLIST - 1))) {
                                bufferedWriter.write("#EXT-X-ENDLIST");
                                log.info("end of playlist: {}", (sequencePlaylist + URI_IN_PLAYLIST - 1));
                                chukasaModel.setFlagLastPl(true);
                                chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);
                            }
                        }
                    }

                    if (chukasaModel.isFlagLastPl()) {
                        chukasaModel.setFlagTimerPlaylister(true);
                        chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);
                    }
                }

                chukasaModel.setSeqPl(sequencePlaylist);
                chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);

            } else {

                // FFmpeg で生成されるストリームを検出できないのでイニシャルストリームのみ流す．

                // ONLY INITIAL STREAM //
                log.info("ONLY INITIAL STREAM");

                PlaylistType playlistType = chukasaModel.getPlaylistType();
                double segmentedTsDuration = (double) chukasaModel.getHlsConfiguration().getDuration();
                int uriInPlaylist = chukasaModel.getHlsConfiguration().getUriInPlaylist();
                String playlistFilePath = chukasaModel.getStreamPath() + FILE_SEPARATOR + M3U8_FILE_NAME + M3U8_FILE_EXTENSION;

                sequencePlaylist = chukasaModel.getSequenceInitialPlaylist();
                sequencePlaylist++;

                try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(playlistFilePath)))) {

                    bufferedWriter.write("#EXTM3U");
                    bufferedWriter.newLine();
                    bufferedWriter.write("#EXT-X-VERSION:7");
                    bufferedWriter.newLine();
                    bufferedWriter.write("#EXT-X-ALLOW-CACHE:NO");
                    bufferedWriter.newLine();
                    bufferedWriter.write("#EXT-X-TARGETDURATION:" + TARGET_DURATION);
                    bufferedWriter.newLine();

                    if (playlistType.equals(PlaylistType.LIVE)) {
                        bufferedWriter.write("#EXT-X-MEDIA-SEQUENCE:" + sequencePlaylist);
                    } else if (playlistType.equals(PlaylistType.EVENT)) {
                        bufferedWriter.write("#EXT-X-MEDIA-SEQUENCE:0");
                    }

                    bufferedWriter.newLine();

                    int initialSequencePlaylist = 0;
                    if (playlistType.equals(PlaylistType.LIVE)) {
                        initialSequencePlaylist = sequencePlaylist;
                    } else if(playlistType.equals(PlaylistType.EVENT)) {
                        initialSequencePlaylist = 0;
                    }

                    for (int i = initialSequencePlaylist; i < sequencePlaylist + uriInPlaylist; i++) {
                        bufferedWriter.write("#EXTINF:" + Double.toString(segmentedTsDuration) + ",");
                        bufferedWriter.newLine();
                        bufferedWriter.write(initialStreamPath + "/" + "i" + i + STREAM_FILE_EXTENSION);
                        bufferedWriter.newLine();
                    }
                }

                chukasaModel.setSequenceInitialPlaylist(sequencePlaylist);
                chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);
            }

        } catch (IOException e) {
            log.error("{} {}", e.getMessage(), e);
        }
    }
}
