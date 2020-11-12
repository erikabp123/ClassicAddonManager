package com.CAM.HelperTools.GameSpecific;

import javafx.scene.image.Image;

public enum AddonSource {
    CURSEFORGE(true, "curse_logo_small.png"),
    GITHUB(false, "github_logo_small.png"),
    WOWINTERFACE(true, "wowinterface_logo_small.png"),
    TUKUI(true, "tukui_logo_small.png"),
    WOWACE(false, "failure.png");

    private final boolean isSearchable;
    private final String logoName;
    AddonSource(boolean isSearchable, String logoName) {
        this.isSearchable = isSearchable;
        this.logoName = logoName;
    }
    public boolean isSearchable() { return this.isSearchable; }
    public String getLogoName() { return this.logoName; }
    public Image getWebsiteIcon() { return new Image(this.getClass().getClassLoader().getResource(getLogoName()).toExternalForm()); }
}