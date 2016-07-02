package pro.hirooka.chukasa.playlister;

import lombok.extern.slf4j.Slf4j;
import pro.hirooka.chukasa.ChukasaConstant;
import pro.hirooka.chukasa.domain.chukasa.ChukasaModel;
import pro.hirooka.chukasa.domain.chukasa.type.PlaylistType;
import pro.hirooka.chukasa.service.chukasa.IChukasaModelManagementComponent;

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

            int targetDuration = chukasaModel.getHlsConfiguration().getDuration() + 1;
            int sequenceTs = chukasaModel.getSeqTs();
            log.info("sequenceTs = {}", sequenceTs);

            // イニシャルストリームのみか否か
            if(sequenceTs >= 0) {

                PlaylistType playlistType = chukasaModel.getPlaylistType();
                double segmentedTsDuration = (double) chukasaModel.getHlsConfiguration().getDuration();
                int uriInPlaylist = chukasaModel.getHlsConfiguration().getUriInPlaylist();
                String playlistFilePath = chukasaModel.getStreamPath() + FILE_SEPARATOR + M3U8_FILE_NAME + M3U8_FILE_EXTENSION;

                int sequenceInPlaylist = chukasaModel.getSeqPl();

                // 暗号化ストリームか否か
                if (chukasaModel.getChukasaSettings().isEncrypted()) {

                    try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(playlistFilePath)))) {

                        bufferedWriter.write("#EXTM3U");
                        bufferedWriter.newLine();
                        bufferedWriter.write("#EXT-X-VERSION:7");
                        bufferedWriter.newLine();
                        bufferedWriter.write("#EXT-X-TARGETDURATION:" + targetDuration);
                        bufferedWriter.newLine();

                        if (!(sequenceTs >= (uriInPlaylist - 1))) {

                            // MIX STREAM //

                            int initialSequenceInPlaylist = chukasaModel.getSequenceInitialPlaylist();
                            initialSequenceInPlaylist++;

                            if (playlistType.equals(PlaylistType.LIVE)) {
                                bufferedWriter.write("#EXT-X-MEDIA-SEQUENCE:" + initialSequenceInPlaylist);
                                bufferedWriter.newLine();
                                bufferedWriter.write("#EXT-X-DISCONTINUITY-SEQUENCE:" + 1);
                            } else if (playlistType.equals(PlaylistType.EVENT)) {
                                bufferedWriter.write("#EXT-X-MEDIA-SEQUENCE:0");
                            }

                            bufferedWriter.newLine();

                            if (playlistType.equals(PlaylistType.LIVE)) {
                                for (int i = initialSequenceInPlaylist; i < initialSequenceInPlaylist + uriInPlaylist - sequenceTs - 1; i++) {
                                    bufferedWriter.write("#EXTINF:" + (double) chukasaModel.getHlsConfiguration().getDuration() + ",");
                                    bufferedWriter.newLine();
                                    bufferedWriter.write(initialStreamPath + "/" + "i" + i + STREAM_FILE_EXTENSION);
                                    bufferedWriter.newLine();
                                }
                            } else if (playlistType.equals(PlaylistType.EVENT)) {
                                for (int i = 0; i < initialSequenceInPlaylist + uriInPlaylist - sequenceTs - 1; i++) {
                                    bufferedWriter.write("#EXTINF:" + (double) chukasaModel.getHlsConfiguration().getDuration() + ",");
                                    bufferedWriter.newLine();
                                    bufferedWriter.write(initialStreamPath + "/" + "i" + i + STREAM_FILE_EXTENSION);
                                    bufferedWriter.newLine();
                                }
                            }

                            bufferedWriter.write("#EXT-X-DISCONTINUITY");
                            bufferedWriter.newLine();

                            chukasaModel.setSequenceInitialPlaylist(initialSequenceInPlaylist);
                            chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);

                            if (playlistType.equals(PlaylistType.LIVE)) {
                                for (int i = 0; i < sequenceTs + 1; i++) {
                                    bufferedWriter.write("#EXT-X-KEY:METHOD=AES-128,URI=");
                                    bufferedWriter.write("\"" + "" + chukasaModel.getKeyArrayList().get(i) + i + ".key\"" + ",IV=0x");
                                    bufferedWriter.write(chukasaModel.getIvArrayList().get(i));
                                    bufferedWriter.newLine();
                                    bufferedWriter.write("#EXTINF:" + chukasaModel.getExtinfList().get(i) + ",");
                                    bufferedWriter.newLine();
                                    bufferedWriter.write(STREAM_FILE_NAME_PREFIX + i + STREAM_FILE_EXTENSION);
                                    bufferedWriter.newLine();
                                }
                            } else if (playlistType.equals(PlaylistType.EVENT)) {
                                for (int i = 0; i < sequenceTs + 1; i++) {
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

                            if (playlistType.equals(PlaylistType.LIVE)) {
                                int mediaSequence = sequenceInPlaylist - (uriInPlaylist - 1);
                                mediaSequence = mediaSequence + chukasaModel.getSequenceInitialPlaylist() + uriInPlaylist - 1 - 1;
                                bufferedWriter.write("#EXT-X-MEDIA-SEQUENCE:" + mediaSequence);
                                bufferedWriter.newLine();
                                bufferedWriter.write("#EXT-X-DISCONTINUITY-SEQUENCE:" + 1);
                            } else if (playlistType.equals(PlaylistType.EVENT)) {
                                bufferedWriter.write("#EXT-X-MEDIA-SEQUENCE:0");
                            }

                            bufferedWriter.newLine();

                            int initialSequenceInPlaylist = 0;
                            if (playlistType.equals(PlaylistType.LIVE)) {
                                initialSequenceInPlaylist = sequenceInPlaylist;
                            } else if (playlistType.equals(PlaylistType.EVENT)) {
                                initialSequenceInPlaylist = 0;
                                for (int i = 0; i < chukasaModel.getSequenceInitialPlaylist() + uriInPlaylist - 1 - 1; i++) {
                                    bufferedWriter.write("#EXTINF:" + (double) chukasaModel.getHlsConfiguration().getDuration() + ",");
                                    bufferedWriter.newLine();
                                    bufferedWriter.write(initialStreamPath + "/" + "i" + i + STREAM_FILE_EXTENSION);
                                    bufferedWriter.newLine();
                                }
                                bufferedWriter.write("#EXT-X-DISCONTINUITY");
                                bufferedWriter.newLine();
                            }

                            if (playlistType.equals(PlaylistType.LIVE)) {
                                initialSequenceInPlaylist = initialSequenceInPlaylist - (uriInPlaylist - 1);
                                initialSequenceInPlaylist = sequenceInPlaylist - (uriInPlaylist - 1);
                                for (int i = initialSequenceInPlaylist; i < (initialSequenceInPlaylist + uriInPlaylist); i++) {
                                    bufferedWriter.write("#EXT-X-KEY:METHOD=AES-128,URI=");
                                    bufferedWriter.write("\"" + "" + chukasaModel.getKeyArrayList().get(i) + i + ".key\"" + ",IV=0x");
                                    bufferedWriter.write(chukasaModel.getIvArrayList().get(i));
                                    bufferedWriter.newLine();
                                    bufferedWriter.write("#EXTINF:" + chukasaModel.getExtinfList().get(i) + ",");
                                    bufferedWriter.newLine();
                                    bufferedWriter.write(STREAM_FILE_NAME_PREFIX + i + STREAM_FILE_EXTENSION);
                                    bufferedWriter.newLine();
                                }
                            } else if (playlistType.equals(PlaylistType.EVENT)) {
                                initialSequenceInPlaylist = 0;
                                for (int i = initialSequenceInPlaylist; i < (sequenceTs + 1); i++) {
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
                            if (sequenceInPlaylist >= (chukasaModel.getSeqTsLast() - (uriInPlaylist - 1))) {
                                bufferedWriter.write("#EXT-X-ENDLIST");
                                log.info("end of playlist: {}", (sequenceInPlaylist + uriInPlaylist - 1));
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

                    BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(playlistFilePath)));

                    bufferedWriter.write("#EXTM3U");
                    bufferedWriter.newLine();
                    bufferedWriter.write("#EXT-X-VERSION:7");
                    bufferedWriter.newLine();
                    bufferedWriter.write("#EXT-X-TARGETDURATION:" + targetDuration);
                    bufferedWriter.newLine();

//                    if(!(sequenceTs >= (uriInPlaylist - 1))){
                    if(!(sequenceInPlaylist >= (uriInPlaylist - 1))){

                        // MIX STREAM //

                        int iInitial = chukasaModel.getSequenceInitialPlaylist();
                        iInitial++;

                        if (playlistType.equals(PlaylistType.LIVE)) {
                            bufferedWriter.write("#EXT-X-MEDIA-SEQUENCE:" + iInitial);
                            bufferedWriter.newLine();
                            bufferedWriter.write("#EXT-X-DISCONTINUITY-SEQUENCE:" + 1);
                        } else if (playlistType.equals(PlaylistType.EVENT)) {
                            bufferedWriter.write("#EXT-X-MEDIA-SEQUENCE:0");
                        }

                        bufferedWriter.newLine();

                        if (playlistType.equals(PlaylistType.LIVE)) {
                            for (int i = iInitial; i < iInitial + uriInPlaylist - sequenceTs - 1; i++) {
                                bufferedWriter.write("#EXTINF:" + (double) chukasaModel.getHlsConfiguration().getDuration() + ",");
                                bufferedWriter.newLine();
                                bufferedWriter.write(initialStreamPath + "/" + "i" + i + STREAM_FILE_EXTENSION);
                                bufferedWriter.newLine();
                            }
                        }else if (playlistType == PlaylistType.EVENT) {
                            for (int i = 0; i < iInitial + uriInPlaylist - sequenceTs - 1; i++) {
                                bufferedWriter.write("#EXTINF:" + (double) chukasaModel.getHlsConfiguration().getDuration() + ",");
                                bufferedWriter.newLine();
                                bufferedWriter.write(initialStreamPath + "/" + "i" + i + STREAM_FILE_EXTENSION);
                                bufferedWriter.newLine();
                            }
                        }

                        bufferedWriter.write("#EXT-X-DISCONTINUITY");
                        bufferedWriter.newLine();

                        chukasaModel.setSequenceInitialPlaylist(iInitial);
//                        chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);

                        if (playlistType.equals(PlaylistType.LIVE)) {
                            for (int i = 0 ; i < sequenceTs + 1; i++) {
                                //bufferedWriter.write("#EXTINF:" + Double.toString(segmentedTsDuration) + ",");
                                bufferedWriter.write("#EXTINF:" + chukasaModel.getExtinfList().get(i) + ",");
                                bufferedWriter.newLine();
                                bufferedWriter.write(STREAM_FILE_NAME_PREFIX + i + STREAM_FILE_EXTENSION);
                                bufferedWriter.newLine();
                            }
                        }else if (playlistType.equals(PlaylistType.EVENT)) {
                            for (int i = 0; i < sequenceTs + 1; i++) {
                                bufferedWriter.write("#EXTINF:" + Double.toString(segmentedTsDuration) + ",");
                                bufferedWriter.newLine();
                                bufferedWriter.write(STREAM_FILE_NAME_PREFIX + i + STREAM_FILE_EXTENSION);
                                bufferedWriter.newLine();
                            }
                        }

                    }else{

                        // ONLY LIVE STREAM

                        if (playlistType.equals(PlaylistType.LIVE)) {
                            int mediaSequence = sequenceInPlaylist - (uriInPlaylist - 1);
                            mediaSequence = mediaSequence + chukasaModel.getSequenceInitialPlaylist() + uriInPlaylist - 1 - 1;
                            bufferedWriter.write("#EXT-X-MEDIA-SEQUENCE:" + mediaSequence);
                            bufferedWriter.newLine();
                            bufferedWriter.write("#EXT-X-DISCONTINUITY-SEQUENCE:" + 1);
                        } else if (playlistType == PlaylistType.EVENT) {
                            bufferedWriter.write("#EXT-X-MEDIA-SEQUENCE:0");
                        }

                        bufferedWriter.newLine();

                        int initialSequenceInPlaylist = 0;
                        if (playlistType.equals(PlaylistType.LIVE)) {
                            initialSequenceInPlaylist = sequenceInPlaylist;
                        } else if (playlistType.equals(PlaylistType.EVENT)) {
                            initialSequenceInPlaylist = 0;
                            for (int i = 0; i < chukasaModel.getSequenceInitialPlaylist() + uriInPlaylist - 1 - 1 ; i++) {
                                bufferedWriter.write("#EXTINF:" + (double) chukasaModel.getHlsConfiguration().getDuration() + ",");
                                bufferedWriter.newLine();
                                bufferedWriter.write(initialStreamPath + "/" + "i" + i + STREAM_FILE_EXTENSION);
                                bufferedWriter.newLine();

                            }
                            bufferedWriter.write("#EXT-X-DISCONTINUITY");
                            bufferedWriter.newLine();
                        }

                        if (playlistType.equals(PlaylistType.LIVE)) {
                            initialSequenceInPlaylist = initialSequenceInPlaylist - (uriInPlaylist - 1);
                            for (int i = initialSequenceInPlaylist; i < (initialSequenceInPlaylist + uriInPlaylist); i++) {
                                bufferedWriter.write("#EXTINF:" + chukasaModel.getExtinfList().get(i) + ",");
                                bufferedWriter.newLine();
                                bufferedWriter.write(STREAM_FILE_NAME_PREFIX + i + STREAM_FILE_EXTENSION);
                                bufferedWriter.newLine();
                            }
                        }else if(playlistType.equals(PlaylistType.EVENT)){
                            initialSequenceInPlaylist = 0;
                            for (int i = initialSequenceInPlaylist; i < (sequenceTs + 1); i++) {
                                bufferedWriter.write("#EXTINF:" + chukasaModel.getExtinfList().get(i) + ",");
                                bufferedWriter.newLine();
                                bufferedWriter.write(STREAM_FILE_NAME_PREFIX + i + STREAM_FILE_EXTENSION);
                                bufferedWriter.newLine();
                            }
                        }
                    }

                    if (chukasaModel.isFlagLastTs()) {
                        if (sequenceInPlaylist >= (chukasaModel.getSeqTsLast() - (uriInPlaylist - 1))) {
                            bufferedWriter.write("#EXT-X-ENDLIST");
                            log.info("end of playlist: {}", (sequenceInPlaylist + uriInPlaylist - 1));
                            chukasaModel.setFlagLastPl(true);
                            chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);
                        }
                    }
                    bufferedWriter.close();

                    if (chukasaModel.isFlagLastPl()) {
                        chukasaModel.setFlagTimerPlaylister(true);
                        chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);
                    }

                }

                sequenceInPlaylist++;
                chukasaModel.setSeqPl(sequenceInPlaylist);
                chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);

            } else {

                // ONLY INITIAL STREAM //

                PlaylistType playlistType = chukasaModel.getPlaylistType();
                double segmentedTsDuration = (double) chukasaModel.getHlsConfiguration().getDuration();
                int uriInPlaylist = chukasaModel.getHlsConfiguration().getUriInPlaylist();
                String playlistFilePath = chukasaModel.getStreamPath() + FILE_SEPARATOR + M3U8_FILE_NAME + M3U8_FILE_EXTENSION;

                int sequenceInPlaylist = chukasaModel.getSequenceInitialPlaylist();
                sequenceInPlaylist++;

                try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(playlistFilePath)))) {

                    bufferedWriter.write("#EXTM3U");
                    bufferedWriter.newLine();
                    bufferedWriter.write("#EXT-X-VERSION:7");
                    bufferedWriter.newLine();
                    bufferedWriter.write("#EXT-X-ALLOW-CACHE:NO");
                    bufferedWriter.newLine();
                    bufferedWriter.write("#EXT-X-TARGETDURATION:" + targetDuration);
                    bufferedWriter.newLine();

                    if (playlistType.equals(PlaylistType.LIVE)) {
                        bufferedWriter.write("#EXT-X-MEDIA-SEQUENCE:" + sequenceInPlaylist);
                    } else if (playlistType.equals(PlaylistType.EVENT)) {
                        bufferedWriter.write("#EXT-X-MEDIA-SEQUENCE:0");
                    }

                    bufferedWriter.newLine();

                    int initialSequenceInPlaylist = 0;
                    if (playlistType.equals(PlaylistType.LIVE)) {
                        initialSequenceInPlaylist = sequenceInPlaylist;
                    }

                    for (int i = initialSequenceInPlaylist; i < (sequenceInPlaylist + uriInPlaylist); i++) {
                        bufferedWriter.write("#EXTINF:" + Double.toString(segmentedTsDuration) + ",");
                        bufferedWriter.newLine();
                        bufferedWriter.write(initialStreamPath + "/" + "i" + i + STREAM_FILE_EXTENSION);
                        bufferedWriter.newLine();
                    }
                }

                chukasaModel.setSequenceInitialPlaylist(sequenceInPlaylist);
                chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);
            }

        } catch (IOException e) {
            log.error("{} {}", e.getMessage(), e);
        }
    }
}
