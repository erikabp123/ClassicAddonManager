package com.CAM.HelperTools.GameSpecific;

public enum GameVersion {
    CLASSIC("_classic_era_",
            "WowClassic.exe",
            "1.13",
            "wow_classic",
            "classic-addons",
            "Classic"),
    TBC("_classic_",
            "WowClassic.exe",
            "2.5",
            "wow_burning_crusade",
            "classic-tbc-addons",
            "TBC"),
    RETAIL("_retail_",
            "Wow.exe",
            "9.",
            "wow_retail",
            "addons",
            "Retail"),
    PTR_RETAIL("_ptr_",
            "WowT.exe",
            "9.",
            "wow_retail",
            "addons",
            "Retail PTR"),
    PTR_CLASSIC("_classic_ptr_",
            "WowClassicT.exe",
            "1.13",
            "wow_classic",
            "classic-addons",
            "Classic PTR");

    private final String path;
    private final String exeName;
    private final String prefix;
    private final String curseFlavor;
    private final String tukuiSuffix;
    private final String formattedString;
    GameVersion(final String path, final String exeName, final  String prefix, final String curseFlavor, final String tukuiSuffix, final String formattedString) {
        this.path = path;
        this.exeName = exeName;
        this.prefix = prefix;
        this.curseFlavor = curseFlavor;
        this.tukuiSuffix = tukuiSuffix;
        this.formattedString = formattedString;
    }

    public String getPath() {
        return this.path;
    }

    public String getExeName() {
        return this.exeName;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public String getCurseFlavor() {
        return this.curseFlavor;
    }

    public String getTukuiSuffix() {
        return this.tukuiSuffix;
    }

    public String getTukuiSpecificSuffix() {
        return this.tukuiSuffix.substring(0, tukuiSuffix.length() - 1);
    }

    public String getFormattedString() {
        return this.formattedString;
    }

    public static GameVersion[] downloadableVersions() {
        return new GameVersion[]{RETAIL, CLASSIC, TBC};
    }
}
