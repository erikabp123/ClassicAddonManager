package com.CAM.Updating;

import com.CAM.HelperTools.IO.ReadWriteClassFiles;

public class VersionInfo {
    public static final double CAM_VERSION = 1.11;
    public static final double EXE_VERSION = 2.0;
    public static final double AUTOUPDATER_VERSION = 0.3;

    public double expectedCAM;
    public double expectedExe;
    public double expectedAutoUpdate;

    public VersionInfo(){
        this.expectedCAM = CAM_VERSION;
        this.expectedExe = EXE_VERSION;
        this.expectedAutoUpdate = AUTOUPDATER_VERSION;
    }

    public static void saveVersioningFile(){
        ReadWriteClassFiles.saveFile("system/VERSIONING", new VersionInfo());
    }

    public static VersionInfo readVersioningFile(){
        return (VersionInfo) ReadWriteClassFiles.readFile("system/VERSIONING", new VersionInfo());
    }

}
