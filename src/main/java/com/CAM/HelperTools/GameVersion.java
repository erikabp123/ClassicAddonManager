package com.CAM.HelperTools;

public enum GameVersion {
    CLASSIC("_classic_",
            "WowClassic.exe",
            "1.",
            "wow_classic",
            "classic-addons"),
    RETAIL("_retail_",
            "Wow.exe",
            "9.",
            "wow_retail",
            "addons"),
    PTR_RETAIL("_ptr_",
            "WowT.exe",
            "9.",
            "wow_retail",
            "addons"),
    PTR_CLASSIC("_classic_ptr_",
            "WowClassicT.exe",
            "1.",
            "wow_classic",
            "classic-addons");

    private final String path;
    private final String exeName;
    private final String prefix;
    private final String curseFlavor;
    private final String tukuiSuffix;
    GameVersion(final String path, final String exeName, final  String prefix, String curseFlavor, String tukuiSuffix) {
        this.path = path;
        this.exeName = exeName;
        this.prefix = prefix;
        this.curseFlavor = curseFlavor;
        this.tukuiSuffix = tukuiSuffix;
    }
    public String getPath() { return this.path; }
    public String getExeName() { return this.exeName; }
    public String getPrefix() { return this.prefix; }
    public String getCurseFlavor() { return this.curseFlavor; }
    public String getTukuiSuffix() { return this.tukuiSuffix; }
    public String getTukuiSpecificSuffix(){ return this.tukuiSuffix.substring(0, tukuiSuffix.length()-1); }
}
