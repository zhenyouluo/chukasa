package pro.hirooka.chukasa.domain.type;

public enum EncodingSettingsType {

    CELLULAR_LOW("400x224-110-32"),
    CELLULAR_MID("400x224-200-32"),
    CELLULAR_HIGH("480x270-400-64"),
    WIFI_LOW("640x360-600-64"),
    WIFI_MID("640x360-1200-128"),
    WIFI_HIGH("960x540-1800-128"),
    HD_LOW("1280x720-2500-160"),
    HD_HIGH("1280x720-4500-160"),
    FULL_HD_LOW("1920x1080-11000-160"),
    FULL_HD_HIGH("1920x1080-24000-160"),
    FULL_HD_EXTREME("1920x1080-39000-160");

    private final String name;

    EncodingSettingsType(final String str){
        name = str;
    }

    public String getName(){
        return this.name;
    }
}
