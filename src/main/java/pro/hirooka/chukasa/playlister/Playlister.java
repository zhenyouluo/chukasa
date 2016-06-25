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
    final String M3U8_FILE_NAME_PREFIX = ChukasaConstant.M3U8_FILE_NAME_PREFIX;
    final String M3U8_FILE_EXTENSION = ChukasaConstant.M3U8_FILE_EXTENSION;
    final String M3U8_FILE_NAME = M3U8_FILE_NAME_PREFIX + M3U8_FILE_EXTENSION;

    private int adaptiveBitrateStreaming;

    private IChukasaModelManagementComponent chukasaModelManagementComponent;

    public Playlister(int adaptiveBitrateStreaming, IChukasaModelManagementComponent chukasaModelManagementComponent) {
        this.adaptiveBitrateStreaming = adaptiveBitrateStreaming;
        this.chukasaModelManagementComponent = requireNonNull(chukasaModelManagementComponent, "chukasaModelManagementComponent");
    }

    // TODO too redundant

    @Override
    public void run() {

        try {

            ChukasaModel chukasaModel = chukasaModelManagementComponent.get(adaptiveBitrateStreaming);

            int targetDuration = chukasaModel.getHlsConfiguration().getDuration() + 1;
            int sequenceTs = chukasaModel.getSeqTs();
            log.info("sequenceTs = {}", sequenceTs);


            if(sequenceTs >= 0) {

                PlaylistType playlistType = chukasaModel.getPlaylistType();
                double segmentedTsDuration = (double) chukasaModel.getHlsConfiguration().getDuration();
                int uriInPlaylist = chukasaModel.getHlsConfiguration().getUriInPlaylist();
                String playlistPath = chukasaModel.getStreamPath() + FILE_SEPARATOR + M3U8_FILE_NAME;

                int seqPl = 0;
                seqPl = chukasaModel.getSeqPl();

                if (chukasaModel.getChukasaSettings().isEncrypted()) {

                    File f = new File(playlistPath);
                    FileWriter fw = new FileWriter(f);
                    BufferedWriter bw = new BufferedWriter(fw);

                    bw.write("#EXTM3U");
                    bw.newLine();
                    bw.write("#EXT-X-VERSION:7");
                    bw.newLine();
                    bw.write("#EXT-X-TARGETDURATION:" + targetDuration);
                    bw.newLine();

                    if(!(sequenceTs >= (uriInPlaylist - 1))) {

                        // MIX STREAM //

                        int iInitial = chukasaModel.getSequenceInitialPlaylist();
                        iInitial++;

                        if (playlistType == PlaylistType.LIVE) {
                            bw.write("#EXT-X-MEDIA-SEQUENCE:" + iInitial);
                            bw.newLine();
                            bw.write("#EXT-X-DISCONTINUITY-SEQUENCE:" + 1);
                        } else if (playlistType == PlaylistType.EVENT) {
                            bw.write("#EXT-X-MEDIA-SEQUENCE:0");
                        }

                        bw.newLine();

                        int nInitial = 0;
                        if (playlistType == PlaylistType.LIVE) {
                            for (int i = iInitial; i < iInitial + uriInPlaylist - sequenceTs - 1; i++) {
                                bw.write("#EXTINF:" + (double) chukasaModel.getHlsConfiguration().getDuration() + ",");
                                bw.newLine();
                                bw.write(initialStreamPath + "/" + "i" + i + STREAM_FILE_EXTENSION);
                                bw.newLine();
                                nInitial++;
                            }
                        }else if (playlistType == PlaylistType.EVENT) {
                            for (int i = 0; i < iInitial + uriInPlaylist - sequenceTs - 1; i++) {
                                bw.write("#EXTINF:" + (double) chukasaModel.getHlsConfiguration().getDuration() + ",");
                                bw.newLine();
                                bw.write(initialStreamPath + "/" + "i" + i + STREAM_FILE_EXTENSION);
                                bw.newLine();
                                nInitial++;
                            }
                        }

                        bw.write("#EXT-X-DISCONTINUITY");
                        bw.newLine();

                        chukasaModel.setSequenceInitialPlaylist(iInitial);
                        chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);

                        if (playlistType == PlaylistType.LIVE) {
                            for (int i = 0 ; i < sequenceTs + 1; i++) {
                                bw.write("#EXT-X-KEY:METHOD=AES-128,URI=");
                                bw.write("\"" + "" + chukasaModel.getKeyArrayList().get(i) + i + ".key\"" + ",IV=0x");
                                bw.write(chukasaModel.getIvArrayList().get(i));
                                bw.newLine();
                                //bw.write("#EXTINF:" + Double.toString(segmentedTsDuration) + ",");
                                bw.write("#EXTINF:" + chukasaModel.getExtinfList().get(i) + ",");
                                bw.newLine();
                                bw.write(STREAM_FILE_NAME_PREFIX + i + STREAM_FILE_EXTENSION);
                                bw.newLine();
                            }
                        }else if (playlistType == PlaylistType.EVENT) {
                            for (int i = 0 ; i < sequenceTs + 1; i++) {
                                bw.write("#EXT-X-KEY:METHOD=AES-128,URI=");
                                bw.write("\"" + "" + chukasaModel.getKeyArrayList().get(i) + i + ".key\"" + ",IV=0x");
                                bw.write(chukasaModel.getIvArrayList().get(i));
                                bw.newLine();
                                bw.write("#EXTINF:" + Double.toString(segmentedTsDuration) + ",");
                                bw.newLine();
                                bw.write(STREAM_FILE_NAME_PREFIX + i + STREAM_FILE_EXTENSION);
                                bw.newLine();
                            }
                        }

                    }else{

                        // ONLY LIVE STREAM

                        if (playlistType == PlaylistType.LIVE) {
                            int mediaSequence = seqPl - (uriInPlaylist - 1);
                            mediaSequence = mediaSequence + chukasaModel.getSequenceInitialPlaylist() + uriInPlaylist - 1 - 1;
                            bw.write("#EXT-X-MEDIA-SEQUENCE:" + mediaSequence);
                            bw.newLine();
                            bw.write("#EXT-X-DISCONTINUITY-SEQUENCE:" + 1);
                        } else if (playlistType == PlaylistType.EVENT) {
                            bw.write("#EXT-X-MEDIA-SEQUENCE:0");
                        }

                        bw.newLine();

                        int initSeqPl = 0;
                        if (playlistType == PlaylistType.LIVE) {
                            initSeqPl = seqPl;
                        } else if (playlistType == PlaylistType.EVENT) {
                            initSeqPl = 0;
                            for (int i = 0; i < chukasaModel.getSequenceInitialPlaylist() + uriInPlaylist - 1 - 1 ; i++) {
                                bw.write("#EXTINF:" + (double) chukasaModel.getHlsConfiguration().getDuration() + ",");
                                bw.newLine();
                                bw.write(initialStreamPath + "/" + "i" + i + STREAM_FILE_EXTENSION);
                                bw.newLine();

                            }
                            bw.write("#EXT-X-DISCONTINUITY");
                            bw.newLine();
                        }

                        if (playlistType == PlaylistType.LIVE) {
                            initSeqPl = initSeqPl - (uriInPlaylist - 1);
                            for (int i = initSeqPl; i < (initSeqPl + uriInPlaylist); i++) {
                                bw.write("#EXT-X-KEY:METHOD=AES-128,URI=");
                                bw.write("\"" + "" + chukasaModel.getKeyArrayList().get(i) + i + ".key\"" + ",IV=0x");
                                bw.write(chukasaModel.getIvArrayList().get(i));
                                bw.newLine();
                                //bw.write("#EXTINF:" + Double.toString(segmentedTsDuration) + ",");
                                bw.write("#EXTINF:" + chukasaModel.getExtinfList().get(i) + ",");
                                bw.newLine();
                                bw.write(STREAM_FILE_NAME_PREFIX + i + STREAM_FILE_EXTENSION);
                                bw.newLine();
                            }
                        }else if(playlistType == PlaylistType.EVENT){
                            initSeqPl = 0;
                            for (int i = initSeqPl; i < (sequenceTs + 1); i++) {
                                bw.write("#EXT-X-KEY:METHOD=AES-128,URI=");
                                bw.write("\"" + "" + chukasaModel.getKeyArrayList().get(i) + i + ".key\"" + ",IV=0x");
                                bw.write(chukasaModel.getIvArrayList().get(i));
                                bw.newLine();
                                bw.write("#EXTINF:" + Double.toString(segmentedTsDuration) + ",");
                                bw.newLine();
                                bw.write(STREAM_FILE_NAME_PREFIX + i + STREAM_FILE_EXTENSION);
                                bw.newLine();
                            }
                        }

                    }

                    if (chukasaModel.isFlagLastTs()) {
                        if (seqPl >= (chukasaModel.getSeqTsLast() - (uriInPlaylist - 1))) {
                            bw.write("#EXT-X-ENDLIST");
                            log.info("end of playlist: {}", (seqPl + uriInPlaylist - 1));
                            chukasaModel.setFlagLastPl(true);
                            chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);
                        }
                    }
                    bw.close();
                    fw.close();

                    if (chukasaModel.isFlagLastPl()) {
                        chukasaModel.setFlagTimerPlaylister(true);
                        chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);
                    }

                } else {

                    BufferedWriter bw = new BufferedWriter(new FileWriter(new File(playlistPath)));

                    bw.write("#EXTM3U");
                    bw.newLine();
                    bw.write("#EXT-X-VERSION:7");
                    bw.newLine();
                    bw.write("#EXT-X-TARGETDURATION:" + targetDuration);
                    bw.newLine();

//                    if(!(sequenceTs >= (uriInPlaylist - 1))){
                    if(!(seqPl >= (uriInPlaylist - 1))){

                        // MIX STREAM //

                        int iInitial = chukasaModel.getSequenceInitialPlaylist();
                        iInitial++;

                        if (playlistType.equals(PlaylistType.LIVE)) {
                            bw.write("#EXT-X-MEDIA-SEQUENCE:" + iInitial);
                            bw.newLine();
                            bw.write("#EXT-X-DISCONTINUITY-SEQUENCE:" + 1);
                        } else if (playlistType.equals(PlaylistType.EVENT)) {
                            bw.write("#EXT-X-MEDIA-SEQUENCE:0");
                        }

                        bw.newLine();

                        if (playlistType.equals(PlaylistType.LIVE)) {
                            for (int i = iInitial; i < iInitial + uriInPlaylist - sequenceTs - 1; i++) {
                                bw.write("#EXTINF:" + (double) chukasaModel.getHlsConfiguration().getDuration() + ",");
                                bw.newLine();
                                bw.write(initialStreamPath + "/" + "i" + i + STREAM_FILE_EXTENSION);
                                bw.newLine();
                            }
                        }else if (playlistType == PlaylistType.EVENT) {
                            for (int i = 0; i < iInitial + uriInPlaylist - sequenceTs - 1; i++) {
                                bw.write("#EXTINF:" + (double) chukasaModel.getHlsConfiguration().getDuration() + ",");
                                bw.newLine();
                                bw.write(initialStreamPath + "/" + "i" + i + STREAM_FILE_EXTENSION);
                                bw.newLine();
                            }
                        }

                        bw.write("#EXT-X-DISCONTINUITY");
                        bw.newLine();

                        chukasaModel.setSequenceInitialPlaylist(iInitial);
//                        chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);

                        if (playlistType.equals(PlaylistType.LIVE)) {
                            for (int i = 0 ; i < sequenceTs + 1; i++) {
                                //bw.write("#EXTINF:" + Double.toString(segmentedTsDuration) + ",");
                                bw.write("#EXTINF:" + chukasaModel.getExtinfList().get(i) + ",");
                                bw.newLine();
                                bw.write(STREAM_FILE_NAME_PREFIX + i + STREAM_FILE_EXTENSION);
                                bw.newLine();
                            }
                        }else if (playlistType.equals(PlaylistType.EVENT)) {
                            for (int i = 0; i < sequenceTs + 1; i++) {
                                bw.write("#EXTINF:" + Double.toString(segmentedTsDuration) + ",");
                                bw.newLine();
                                bw.write(STREAM_FILE_NAME_PREFIX + i + STREAM_FILE_EXTENSION);
                                bw.newLine();
                            }
                        }

                    }else{

                        // ONLY LIVE STREAM

                        if (playlistType.equals(PlaylistType.LIVE)) {
                            int mediaSequence = seqPl - (uriInPlaylist - 1);
                            mediaSequence = mediaSequence + chukasaModel.getSequenceInitialPlaylist() + uriInPlaylist - 1 - 1;
                            bw.write("#EXT-X-MEDIA-SEQUENCE:" + mediaSequence);
                            bw.newLine();
                            bw.write("#EXT-X-DISCONTINUITY-SEQUENCE:" + 1);
                        } else if (playlistType == PlaylistType.EVENT) {
                            bw.write("#EXT-X-MEDIA-SEQUENCE:0");
                        }

                        bw.newLine();

                        int initSeqPl = 0;
                        if (playlistType == PlaylistType.LIVE) {
                            initSeqPl = seqPl;
                        } else if (playlistType == PlaylistType.EVENT) {
                            initSeqPl = 0;
                            for (int i = 0; i < chukasaModel.getSequenceInitialPlaylist() + uriInPlaylist - 1 - 1 ; i++) {
                                bw.write("#EXTINF:" + (double) chukasaModel.getHlsConfiguration().getDuration() + ",");
                                bw.newLine();
                                bw.write(initialStreamPath + "/" + "i" + i + STREAM_FILE_EXTENSION);
                                bw.newLine();

                            }
                            bw.write("#EXT-X-DISCONTINUITY");
                            bw.newLine();
                        }

                        if (playlistType == PlaylistType.LIVE) {
                            initSeqPl = initSeqPl - (uriInPlaylist - 1);
                            for (int i = initSeqPl; i < (initSeqPl + uriInPlaylist); i++) {
                                //bw.write("#EXTINF:" + Double.toString(segmentedTsDuration) + ",");
                                bw.write("#EXTINF:" + chukasaModel.getExtinfList().get(i) + ",");
                                bw.newLine();
                                bw.write(STREAM_FILE_NAME_PREFIX + i + STREAM_FILE_EXTENSION);
                                bw.newLine();
                            }
                        }else if(playlistType == PlaylistType.EVENT){
                            initSeqPl = 0;
                            for (int i = initSeqPl; i < (sequenceTs + 1); i++) {
                                bw.write("#EXTINF:" + Double.toString(segmentedTsDuration) + ",");
                                bw.newLine();
                                bw.write(STREAM_FILE_NAME_PREFIX + i + STREAM_FILE_EXTENSION);
                                bw.newLine();
                            }
                        }
                    }

                    if (chukasaModel.isFlagLastTs()) {
                        if (seqPl >= (chukasaModel.getSeqTsLast() - (uriInPlaylist - 1))) {
                            bw.write("#EXT-X-ENDLIST");
                            log.info("end of playlist: {}", (seqPl + uriInPlaylist - 1));
                            chukasaModel.setFlagLastPl(true);
                            chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);
                        }
                    }
                    bw.close();

                    if (chukasaModel.isFlagLastPl()) {
                        chukasaModel.setFlagTimerPlaylister(true);
                        chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);
                    }

                }

                seqPl++;
                chukasaModel.setSeqPl(seqPl);
                chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);

            } else {

                // ONLY INITIAL STREAM //

                PlaylistType playlistType = chukasaModel.getPlaylistType();
                double segmentedTsDuration = (double) chukasaModel.getHlsConfiguration().getDuration();
                int uriInPlaylist = chukasaModel.getHlsConfiguration().getUriInPlaylist();
                String playlistPath = chukasaModel.getStreamPath() + FILE_SEPARATOR + M3U8_FILE_NAME;

                int seqPl = chukasaModel.getSequenceInitialPlaylist();
                seqPl++;

                BufferedWriter bw = new BufferedWriter(new FileWriter(new File(playlistPath)));

                bw.write("#EXTM3U");
                bw.newLine();
                bw.write("#EXT-X-VERSION:7");
                bw.newLine();
                bw.write("#EXT-X-ALLOW-CACHE:NO");
                bw.newLine();
                bw.write("#EXT-X-TARGETDURATION:" + targetDuration);
                bw.newLine();

                if (playlistType == PlaylistType.LIVE) {
                    bw.write("#EXT-X-MEDIA-SEQUENCE:" + seqPl);
                } else if (playlistType == PlaylistType.EVENT) {
                    bw.write("#EXT-X-MEDIA-SEQUENCE:0");
                }

                bw.newLine();

                int initSeqPl = 0;
                if (playlistType.equals(PlaylistType.LIVE)) {
                    initSeqPl = seqPl;
                }

                for (int i = initSeqPl; i < (seqPl + uriInPlaylist); i++) {
                    bw.write("#EXTINF:" + Double.toString(segmentedTsDuration) + ",");
                    bw.newLine();
                    bw.write(initialStreamPath + "/" + "i" + i + STREAM_FILE_EXTENSION);
                    bw.newLine();
                }

                bw.close();

                chukasaModel.setSequenceInitialPlaylist(seqPl);
                chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
