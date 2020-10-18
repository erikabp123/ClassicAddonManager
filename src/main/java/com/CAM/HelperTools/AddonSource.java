package com.CAM.HelperTools;

public enum AddonSource {
    CURSEFORGE(true),
    GITHUB(false),
    WOWINTERFACE(true),
    TUKUI(true),
    WOWACE(false);

    private final boolean isSearchable;
    AddonSource(boolean isSearchable) {
        this.isSearchable = isSearchable;
    }
    public boolean isSearchable() { return this.isSearchable; }
}