package pro.hirooka.chukasa.domain.type;

public enum VideoResolutionType {
    FULL_HD("1920x1080"),
    HD("1280x720"),
    WIFI_HIGH("960x540"),
    WIFI_LOW("640x360"),
    CELLULAR("400x224");

    private final String name;

    VideoResolutionType(final String str){
        name = str;
    }

    public String getName(){
        return this.name;
    }
}
