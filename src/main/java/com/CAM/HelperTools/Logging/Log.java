package com.CAM.HelperTools.Logging;

import com.CAM.Settings.SessionOnlySettings;

import java.util.ArrayList;

public class Log {

    private static ArrayList<LogListener> listeners = new ArrayList<>();

    public static void log(String text){
        System.out.println(text);
        notifyListeners(text);
    }

    public static void verbose(String text){
        if(!SessionOnlySettings.isLogging()){
            return;
        }
        System.out.println(text);
        notifyListeners(text);
    }

    public static void listen(LogListener listener){
        listeners.add(listener);
    }

    private static void notifyListeners(String text){
        for(LogListener listener : listeners){
            listener.notify(text);
        }
    }



}
