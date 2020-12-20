package com.CAM.Settings;

import com.CAM.HelperTools.IO.ReadWriteClassFiles;

import java.util.ArrayList;
import java.util.List;

public class Preferences {

    private boolean cfReleasesOnly = false;
    private int maxCacheDuration = 120; //min
    private boolean checkForUpdatesOnLaunch = false;
    private boolean scrollToBottomOnUpdate = false;

    private List<PreferencesChangeListener> listeners = new ArrayList<>();
    private static Preferences preferences = null;

    public static Preferences getInstance() {
        if(preferences == null) preferences = readPreferencesFile();
        return preferences;
    }


    public static void savePreferencesFile(){
        if(preferences == null) return;
        ReadWriteClassFiles.saveFile("system/PREFERENCES", preferences);
    }

    private static Preferences readPreferencesFile(){
        Preferences emptyPref = new Preferences();
        Preferences pref = (Preferences) ReadWriteClassFiles.readFile("system/PREFERENCES", emptyPref);
        boolean noPrefFileFound = (pref == null);
        return noPrefFileFound ? emptyPref : pref;
    }

    public boolean isCfReleasesOnly() {
        return cfReleasesOnly;
    }

    public void setCfReleasesOnly(boolean cfReleasesOnly) {
        this.cfReleasesOnly = cfReleasesOnly;
        notifyListeners("cfReleases");
    }

    public int getMaxCacheDuration() {
        return maxCacheDuration;
    }

    public void setMaxCacheDuration(int maxCacheDuration) {
        this.maxCacheDuration = maxCacheDuration;
        notifyListeners("maxCacheDuration");
    }

    private void notifyListeners(String nameOfPreference){
        for(PreferencesChangeListener listener: listeners) listener.notifyChange(nameOfPreference);
    }


    public boolean isCheckForUpdatesOnLaunch() {
        return checkForUpdatesOnLaunch;
    }

    public void setCheckForUpdatesOnLaunch(boolean checkForUpdatesOnLaunch) {
        this.checkForUpdatesOnLaunch = checkForUpdatesOnLaunch;
    }

    public boolean isScrollToBottomOnUpdate() {
        return scrollToBottomOnUpdate;
    }

    public void setScrollToBottomOnUpdate(boolean scrollToBottomOnUpdate) {
        this.scrollToBottomOnUpdate = scrollToBottomOnUpdate;
    }
}
