package pro.hirooka.chukasa.api.v1.helper;

import pro.hirooka.chukasa.domain.model.chukasa.enums.WebBrowserType;

public interface IChukasaBrowserDetector {
    WebBrowserType getBrowserType(String userAgent);
    boolean isSafari(String userAgent);
    boolean isChrome(String userAgent);
    boolean isEdge(String userAgent);
    boolean isIE(String userAgent);
    boolean isFirefox(String userAgent);
    boolean isNativeSupported(String userAgent);
    boolean isAlternativeSupported(String userAgent);
}
