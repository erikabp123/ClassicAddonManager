package com.CAM.HelperTools;

public class Log {

    public static boolean logging = false;

    public static void log(String text){
        System.out.println(text);
    }

    public static void verbose(String text){
        if(!logging){
            return;
        }
        System.out.println(text);
    }



}
