package com.CAM.HelperTools.Logging;

import com.CAM.DataCollection.DataCollectionException;
import com.CAM.HelperTools.IO.FileOperations;
import com.CAM.Settings.SessionOnlySettings;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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
        if(e.getClass() == DataCollectionException.class){
            DataCollectionException dataCollectionException = (DataCollectionException) e;
            message = message + "Addon: " + dataCollectionException.getAddon() + "\n";
            message = message + "Source: " + dataCollectionException.getSource() + "\n";
            message = message + "Message: " + dataCollectionException.getMessage() + "\n";
        }
        message = message + sw.toString() + "\n";
        message = message + "I------------------------------------------------------I\n\n";

        writeToFile("errors.txt", message);
        e.printStackTrace();

        try {
            pw.close();
            sw.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public synchronized static void writeToFile(String fileName, String contents){
        try {
            File errorFile = new File(fileName);
            errorFile.createNewFile();
            Files.write(Paths.get(fileName), contents.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
