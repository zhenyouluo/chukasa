package pro.hirooka.chukasa.detector;

import pro.hirooka.chukasa.domain.chukasa.type.BrowserType;

public interface IChukasaBrowserDetector {
    BrowserType getBrowserType(String userAgent);
    boolean isSafari(String userAgent);
    boolean isChrome(String userAgent);
    boolean isEdge(String userAgent);
    boolean isIE(String userAgent);
    boolean isFirefox(String userAgent);
    boolean isNativeSupported(String userAgent);
    boolean isAlternativeSupported(String userAgent);
}
