package com.CAM.HelperTools.Logging;

import com.CAM.HelperTools.IO.FileOperations;
import com.CAM.Settings.SessionOnlySettings;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;

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

    public static void printStackTrace(Exception e){
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        e.printStackTrace(pw);
        String message = "I------------------------------------------------------I\n";
        message = message + "Error recorded at: " + new Date().toString() + "\n";
        message = message + sw.toString() + "\n";
        message = message + "I------------------------------------------------------I\n\n";

        FileOperations.writeToFile("errors.txt", message);
        e.printStackTrace();

        try {
            pw.close();
            sw.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }



}
