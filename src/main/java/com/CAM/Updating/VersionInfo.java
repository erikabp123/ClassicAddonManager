package com.CAM.Updating;

import com.google.gson.Gson;

import java.io.*;

public class VersionInfo {
    public static final double CAM_VERSION = 0.77;
    public static final double EXE_VERSION = 0.1;
    public static final double AUTOUPDATER_VERSION = 0.2;

    public double expectedCAM;
    public double expectedExe;
    public double expectedAutoUpdate;

    public VersionInfo(){
        this.expectedCAM = CAM_VERSION;
        this.expectedExe = EXE_VERSION;
        this.expectedAutoUpdate = AUTOUPDATER_VERSION;
    }

    public static void saveVersioningFile(){
        VersionInfo versionInfo = new VersionInfo();
        try {
            Gson gson = new Gson();
            File file = new File("system/VERSIONING");
            file.getParentFile().mkdirs();
            Writer writer = new FileWriter(file);
            gson.toJson(versionInfo, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static VersionInfo readVersioningFile(){
        File file = new File("system/VERSIONING");
        if(!file.exists()){
            return null;
        }
        VersionInfo versionInfo = null;
        try {
            Reader reader = new FileReader("system/VERSIONING");
            Gson gson = new Gson();
            versionInfo = gson.fromJson(reader, VersionInfo.class);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return versionInfo;
    }

}
