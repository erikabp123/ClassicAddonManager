package com.CAM.Settings;

import com.CAM.HelperTools.ReadWriteClassFiles;

public class Preferences {

    public boolean cfReleasesOnly;

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


}
