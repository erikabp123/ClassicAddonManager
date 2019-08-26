package com.CAM.HelperTools;

import java.util.ArrayList;

public class Log {

    public static boolean logging = false;
    public static ArrayList<LogListener> listeners = new ArrayList<>();
    public static boolean skipGithubDownloads = false;

    public static void toggleLogging(){
        logging = !logging;
        Log.log("Set debug to " + logging);
    }

    public static void toggleGithubDownloads(){
        skipGithubDownloads = !skipGithubDownloads;
        Log.log("Set skip github downloads to " + skipGithubDownloads);
    }

    public static void log(String text){
        System.out.println(text);
        notifyListeners(text);
    }

    public static void verbose(String text){
        if(!logging){
            return;
        }
        System.out.println(text);
        notifyListeners(text);
    }

    public static void listen(LogListener listener){
        listeners.add(listener);
    }

    public static void notifyListeners(String text){
        for(LogListener listener : listeners){
            listener.notify(text);
        }
    }



}
