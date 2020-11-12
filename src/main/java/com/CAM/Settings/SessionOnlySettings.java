package com.CAM.Settings;

import com.CAM.HelperTools.Logging.Log;

public class SessionOnlySettings {
    private static boolean logging = false;
    private static boolean skipGithubDownloads = false;
    private static boolean forceUpdateChecking = false;
    private static boolean forceReDownloads = false;

    public static void toggleGithubDownloads(){
        skipGithubDownloads = !skipGithubDownloads;
        Log.log("Set skip github downloads to " + skipGithubDownloads);
    }

    public static void toggleLogging(){
        logging = !logging;
        Log.log("Set debug to " + logging);
    }

    public static void toggleForceUpdateChecking(){
        forceUpdateChecking = !forceUpdateChecking;
        Log.log("Set force update checking to " + forceUpdateChecking);
    }

    public static void toggleForceReDownloads(){
        forceReDownloads= !forceReDownloads;
        Log.log("Set force re-downloads to " + forceReDownloads);
    }

    public static boolean isLogging(){
        return logging;
    }

    public static boolean isSkipGithubDownloads(){
        return skipGithubDownloads;
    }

    public static boolean isForceUpdateChecking(){
        return forceUpdateChecking;
    }

    public static boolean isForceReDownloads() {
        return forceReDownloads;
    }
}
