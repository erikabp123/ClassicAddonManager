package HelperTools;

public class Log {

    public static boolean logging = false;

    public static void log(String text){
        if(!logging){
            return;
        }
        System.out.println(text);
    }



}
