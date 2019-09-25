package com.CAM.Settings;

import com.CAM.HelperTools.Log;

public class SessionOnlySettings {
    private static boolean logging = false;
    private static boolean skipGithubDownloads = false;
    private static boolean forceUpdates = false;

    public static void toggleGithubDownloads(){
        skipGithubDownloads = !skipGithubDownloads;
        Log.log("Set skip github downloads to " + skipGithubDownloads);
    }

    public static void toggleLogging(){
        logging = !logging;
        Log.log("Set debug to " + logging);
    }

    public static void toggleForceUpdates(){
        forceUpdates = !forceUpdates;
        Log.log("Set force updates to " + forceUpdates);
    }

    public static boolean isLogging(){
        return logging;
    }

    public static boolean isSkipGithubDownloads(){
        return skipGithubDownloads;
    }

    public static boolean isForceUpdates(){
        return forceUpdates;
    }

}
