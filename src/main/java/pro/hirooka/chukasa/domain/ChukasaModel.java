package pro.hirooka.chukasa.domain;

import lombok.Data;
import pro.hirooka.chukasa.configuration.ChukasaConfiguration;
import pro.hirooka.chukasa.configuration.HLSConfiguration;
import pro.hirooka.chukasa.configuration.SystemConfiguration;
import pro.hirooka.chukasa.domain.type.PlaylistType;

import java.math.BigDecimal;
import java.util.ArrayList;

@Data
public class ChukasaModel {

    private int adaptiveBitrateStreaming;

    private ChukasaConfiguration chukasaConfiguration;
    private SystemConfiguration systemConfiguration;
    private HLSConfiguration hlsConfiguration;

    // Configuration
    private String streamRootPath;
    private String streamPath;
    private String tempEncPath;
    private int videoBitrate;

    private long timerSegmenterDelay;
    private long timerSegmenterPeriod;
    private long timerPlaylisterDelay;
    private long timerPlaylisterPeriod;

    private ChukasaSettings chukasaSettings;

    // Segmenter
    private long readBytes;
    private int seqTs;
    private int seqTsEnc;
    private int seqTsOkkake;
    private int seqTsLast;
    private boolean flagSegFullDuration;
    private boolean flagLastTs;
    private BigDecimal initPcrSecond;
    private BigDecimal lastPcrSecond;
    private BigDecimal diffPcrSecond;
    private BigDecimal lastPcrSec;

    // Encrypter
    private ArrayList<String> keyArrayList;
    private ArrayList<String> ivArrayList;

    // Playlister
    private PlaylistType playlistType;
    private int seqPl;
    private String namePl;
    private boolean flagLastPl;
    private int sequenceInitialPlaylist;

    // Flag for Timer
    private boolean flagTimerSegmenter;
    private boolean flagTimerPlaylister;

    // Remover
    private boolean flagRemoveFile;

    public ChukasaModel(){

        adaptiveBitrateStreaming = 0;

        this.chukasaConfiguration = null;
        this.systemConfiguration = null;
        this.hlsConfiguration = null;

        // Configuration
        this.streamRootPath = "";
        this.streamPath = "";
        this.tempEncPath = "";
        this.videoBitrate = 0;

        this.timerSegmenterDelay = 0;
        this.timerSegmenterPeriod = 0;
        this.timerPlaylisterDelay = 0;
        this.timerPlaylisterPeriod = 0;

        this.chukasaSettings = null;

        // Segmenter
        this.readBytes = 0;
        this.seqTs = -1;
        this.seqTsEnc = 0;
        this.seqTsOkkake = 0;
        this.seqTsLast = 0;
        this.flagSegFullDuration = false;
        this.flagLastTs = false;
        this.initPcrSecond = new BigDecimal("0.0");
        this.lastPcrSecond = new BigDecimal("0.0");
        this.diffPcrSecond = new BigDecimal("0.0");
        this.lastPcrSec = new BigDecimal("0.0");

        // Encrypter
        keyArrayList = new ArrayList<>();
        ivArrayList = new ArrayList<>();

        // Playlister
        this.playlistType = PlaylistType.LIVE;
        this.seqPl = 0;
        this.namePl = "playlist.m3u8";
        this.flagLastPl = false;
        this.sequenceInitialPlaylist = -1;

        // Flag for Timer
        this.flagTimerSegmenter = false;
        this.flagTimerPlaylister = false;

        // Remover
        this.flagRemoveFile = false;
    }

}
