package pro.hirooka.chukasa.domain.model.chukasa.constants;

import org.apache.commons.lang3.SystemUtils;

public class ChukasaConstant {
    public static final String FILE_SEPARATOR = SystemUtils.FILE_SEPARATOR;
    public static final String INITIAL_STREAM_PATH = "/istream";
    public static final String STREAM_ROOT_PATH_NAME = "stream";
    public static final String LIVE_PATH_NAME = "live";
    public static final String STREAM_FILE_NAME_PREFIX = "chukasa";
    public static final String STREAM_FILE_EXTENSION = ".ts";
    public static final String M3U8_FILE_NAME = "chukasa";
    public static final String FFMPEG_HLS_M3U8_FILE_NAME = "ffmpeg";
    public static final String M3U8_FILE_EXTENSION = ".m3u8";
    public static final String HLS_KEY_FILE_EXTENSION = ".key";
    public static final String HLS_IV_FILE_EXTENSION = ".iv";
    public static final int HLS_KEY_LENGTH = 128;
    public static int MPEG2_TS_PACKET_LENGTH = 188;
    public static final String ALTERNATIVE_HLS_PLAYER = "hlsjs";
    public static final String USER_AGENT = "chukasa-ios";
}
