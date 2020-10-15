package com.CAM.HelperTools;

public enum GameVersion {
    CLASSIC("_classic_", "WowClassic.exe", "1."),
    RETAIL("_retail_", "Wow.exe", "9."),
    PTR_RETAIL("_ptr_", "WowT.exe", "9."),
    PTR_CLASSIC("_classic_ptr_", "WowClassicT.exe", "1.");

    private final String path;
    private final String exeName;
    private final String prefix;
    GameVersion(final String path, final String exeName, final  String prefix) {
        this.path = path;
        this.exeName = exeName;
        this.prefix = prefix;
    }
    public String getPath() { return this.path; }
    public String getExeName() { return this.exeName; }
    public String getPrefix() { return this.prefix; }
}
