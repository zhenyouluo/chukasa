package pro.hirooka.chukasa.api.v1.helper;

import org.springframework.stereotype.Component;
import pro.hirooka.chukasa.domain.model.chukasa.enums.BrowserType;

@Component
public class ChukasaBrowserDetector implements IChukasaBrowserDetector {

    final String SAFARI = "Safari/";
    final String CHROME = "Chrome/";
    final String CHROMIUM = "Chromium/";
    final String EDGE = "Edge/";
    final String IE = "Trident/7";
    final String FIREFOX = "Firefox/";
    final String SEAMONKEY = "Seamonkey/";

    @Override
    public BrowserType getBrowserType(String userAgent) {
        if(isSafari(userAgent)){
            return BrowserType.SAFARI;
        }else if(isChrome(userAgent)){
            return BrowserType.CHROME;
        }else if(isEdge(userAgent)){
            return BrowserType.EDGE;
        }else if(isIE(userAgent)){
            return BrowserType.IE;
        }else if(isFirefox(userAgent)){
            return BrowserType.FIREFOX;
        }
        return BrowserType.UNKNOWN;
    }

    @Override
    public boolean isSafari(String userAgent) {
        if(userAgent.contains(SAFARI) && !userAgent.contains(CHROME) && !userAgent.contains(CHROMIUM)){
            return true;
        }
        return false;
    }

    @Override
    public boolean isChrome(String userAgent) {
        if(userAgent.contains(CHROME) && !userAgent.contains(CHROMIUM)){
            return true; // TODO: 34
        }
        return false;
    }

    @Override
    public boolean isEdge(String userAgent) {
        if(userAgent.contains(EDGE)){
            return true;
        }
        return false;
    }

    @Override
    public boolean isIE(String userAgent) {
        if(userAgent.contains(IE)){
            return true;
        }
        return false;
    }

    @Override
    public boolean isFirefox(String userAgent) {
        if(userAgent.contains(FIREFOX) && !userAgent.contains(SEAMONKEY)){
            return true; // TODO: 42
        }
        return false;
    }

    @Override
    public boolean isNativeSupported(String userAgent) {
        if(isSafari(userAgent) || isEdge(userAgent)){
            return true;
        }
        return false;
    }

    @Override
    public boolean isAlternativeSupported(String userAgent) {
        if(isChrome(userAgent) || isIE(userAgent) || isFirefox(userAgent)){
            return true;
        }
        return false;
    }
}
