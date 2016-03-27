package pro.hirooka.chukasa.playlister;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import pro.hirooka.chukasa.domain.ChukasaModel;
import pro.hirooka.chukasa.domain.type.PlaylistType;
import pro.hirooka.chukasa.service.IChukasaModelManagementComponent;

import java.io.*;
import java.util.TimerTask;

import static java.util.Objects.requireNonNull;

@Slf4j
public class Playlister extends TimerTask {

    static final String FILE_SEPARATOR = System.getProperty("file.separator");

    private int adaptiveBitrateStreaming;

    private final IChukasaModelManagementComponent chukasaModelManagementComponent;


    @Autowired
    public Playlister(int adaptiveBitrateStreaming, IChukasaModelManagementComponent chukasaModelManagementComponent) {
        this.adaptiveBitrateStreaming = adaptiveBitrateStreaming;
        this.chukasaModelManagementComponent = requireNonNull(chukasaModelManagementComponent, "chukasaModelManagementComponent");
    }

    // TODO too redundant

    @Override
    public void run() {

        try {

            ChukasaModel chukasaModel = chukasaModelManagementComponent.get(adaptiveBitrateStreaming);

            int sequenceTs = chukasaModel.getSeqTs();


            if(sequenceTs >= 0) {

                PlaylistType playlistType = chukasaModel.getPlaylistType();
                double segmentedTsDuration = (double) chukasaModel.getHlsConfiguration().getDuration();
                int uriInPlaylist = chukasaModel.getHlsConfiguration().getUriInPlaylist();
                String playlistPath = chukasaModel.getStreamPath() + FILE_SEPARATOR + chukasaModel.getChukasaConfiguration().getM3u8PlaylistName();

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
                    bw.write("#EXT-X-TARGETDURATION:" + Long.toString(Math.round(segmentedTsDuration)));
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
                                bw.write("/istream/500-" + chukasaModel.getHlsConfiguration().getDuration() + "/" + "i" + i + chukasaModel.getHlsConfiguration().getStreamExtension());
                                bw.newLine();
                                nInitial++;
                            }
                        }else if (playlistType == PlaylistType.EVENT) {
                            for (int i = 0; i < iInitial + uriInPlaylist - sequenceTs - 1; i++) {
                                bw.write("#EXTINF:" + (double) chukasaModel.getHlsConfiguration().getDuration() + ",");
                                bw.newLine();
                                bw.write("/istream/500-" + chukasaModel.getHlsConfiguration().getDuration() + "/" + "i" + i + chukasaModel.getHlsConfiguration().getStreamExtension());
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
                                bw.write(chukasaModel.getChukasaConfiguration().getStreamFileNamePrefix() + i + chukasaModel.getHlsConfiguration().getStreamExtension());
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
                                bw.write(chukasaModel.getChukasaConfiguration().getStreamFileNamePrefix() + i + chukasaModel.getHlsConfiguration().getStreamExtension());
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
                                bw.write("/istream/500-" + chukasaModel.getHlsConfiguration().getDuration() + "/" + "i" + i + chukasaModel.getHlsConfiguration().getStreamExtension());
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
                                bw.write(chukasaModel.getChukasaConfiguration().getStreamFileNamePrefix() + i + chukasaModel.getHlsConfiguration().getStreamExtension());
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
                                bw.write(chukasaModel.getChukasaConfiguration().getStreamFileNamePrefix() + i + chukasaModel.getHlsConfiguration().getStreamExtension());
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

                    File f = new File(playlistPath);
                    FileWriter fw = new FileWriter(f);
                    BufferedWriter bw = new BufferedWriter(fw);

                    bw.write("#EXTM3U");
                    bw.newLine();
                    bw.write("#EXT-X-VERSION:7");
                    bw.newLine();
                    bw.write("#EXT-X-TARGETDURATION:" + Long.toString(Math.round(segmentedTsDuration)));
                    bw.newLine();

                    if(!(sequenceTs >= (uriInPlaylist - 1))){

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
                                bw.write("/istream/500-" + chukasaModel.getHlsConfiguration().getDuration() + "/" + "i" + i + chukasaModel.getHlsConfiguration().getStreamExtension());
                                bw.newLine();
                                nInitial++;
                            }
                        }else if (playlistType == PlaylistType.EVENT) {
                            for (int i = 0; i < iInitial + uriInPlaylist - sequenceTs - 1; i++) {
                                bw.write("#EXTINF:" + (double) chukasaModel.getHlsConfiguration().getDuration() + ",");
                                bw.newLine();
                                bw.write("/istream/500-" + chukasaModel.getHlsConfiguration().getDuration() + "/" + "i" + i + chukasaModel.getHlsConfiguration().getStreamExtension());
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
                                //bw.write("#EXTINF:" + Double.toString(segmentedTsDuration) + ",");
                                bw.write("#EXTINF:" + chukasaModel.getExtinfList().get(i) + ",");
                                bw.newLine();
                                bw.write(chukasaModel.getChukasaConfiguration().getStreamFileNamePrefix() + i + chukasaModel.getHlsConfiguration().getStreamExtension());
                                bw.newLine();
                            }
                        }else if (playlistType == PlaylistType.EVENT) {
                            for (int i = 0; i < sequenceTs + 1; i++) {
                                bw.write("#EXTINF:" + Double.toString(segmentedTsDuration) + ",");
                                bw.newLine();
                                bw.write(chukasaModel.getChukasaConfiguration().getStreamFileNamePrefix() + i + chukasaModel.getHlsConfiguration().getStreamExtension());
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
                                bw.write("/istream/500-" + chukasaModel.getHlsConfiguration().getDuration() + "/" + "i" + i + chukasaModel.getHlsConfiguration().getStreamExtension());
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
                                bw.write(chukasaModel.getChukasaConfiguration().getStreamFileNamePrefix() + i + chukasaModel.getHlsConfiguration().getStreamExtension());
                                bw.newLine();
                            }
                        }else if(playlistType == PlaylistType.EVENT){
                            initSeqPl = 0;
                            for (int i = initSeqPl; i < (sequenceTs + 1); i++) {
                                bw.write("#EXTINF:" + Double.toString(segmentedTsDuration) + ",");
                                bw.newLine();
                                bw.write(chukasaModel.getChukasaConfiguration().getStreamFileNamePrefix() + i + chukasaModel.getHlsConfiguration().getStreamExtension());
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

                }

                seqPl++;
                chukasaModel.setSeqPl(seqPl);
                chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);

            } else {

                // ONLY INITIAL STREAM //

                PlaylistType playlistType = chukasaModel.getPlaylistType();
                double segmentedTsDuration = (double) chukasaModel.getHlsConfiguration().getDuration();
                int uriInPlaylist = chukasaModel.getHlsConfiguration().getUriInPlaylist();
                String playlistPath = chukasaModel.getStreamPath() + FILE_SEPARATOR + chukasaModel.getChukasaConfiguration().getM3u8PlaylistName();

                int seqPl =
                 chukasaModel.getSequenceInitialPlaylist();
                seqPl++;

                File f = new File(playlistPath);
                FileWriter fw = new FileWriter(f);
                BufferedWriter bw = new BufferedWriter(fw);

                bw.write("#EXTM3U");
                bw.newLine();
                bw.write("#EXT-X-VERSION:7");
                bw.newLine();
                bw.write("#EXT-X-ALLOW-CACHE:NO");
                bw.newLine();
                bw.write("#EXT-X-TARGETDURATION:" + Long.toString(Math.round(segmentedTsDuration)));
                bw.newLine();

                if (playlistType == PlaylistType.LIVE) {
                    bw.write("#EXT-X-MEDIA-SEQUENCE:" + seqPl);
                } else if (playlistType == PlaylistType.EVENT) {
                    bw.write("#EXT-X-MEDIA-SEQUENCE:0");
                }

                bw.newLine();

                int initSeqPl = 0;
                if (playlistType == PlaylistType.LIVE) {
                    initSeqPl = seqPl;
                } else if (playlistType == PlaylistType.EVENT) {
                    initSeqPl = 0;
                }

                for (int i = initSeqPl; i < (seqPl + uriInPlaylist); i++) {
                    bw.write("#EXTINF:" + Double.toString(segmentedTsDuration) + ",");
                    bw.newLine();
                    bw.write("/istream/500-" + chukasaModel.getHlsConfiguration().getDuration() + "/" + "i" + i + chukasaModel.getHlsConfiguration().getStreamExtension());
                    bw.newLine();
                }

                bw.close();
                fw.close();

                //seqPl++;
                chukasaModel.setSequenceInitialPlaylist(seqPl);
                chukasaModel = chukasaModelManagementComponent.update(adaptiveBitrateStreaming, chukasaModel);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
