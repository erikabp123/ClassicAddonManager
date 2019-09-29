package com.CAM.HelperTools;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateConverter {

    public static Date convertFromCurse(String curseFormat){
        String cleaned = curseFormat.split(" \\(")[0];

        Date date = null;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM dd yyyy hh:mm:ss");
            dateFormat.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
            date = dateFormat.parse(cleaned);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return date;
    }

    public static Date convertFromCurseAPI(String curseAPIFormat){
        Date date = null;
        try {
            SimpleDateFormat dateFormat = null;
            if(curseAPIFormat.contains(".")){
                dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'");
            } else {
                dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");
            }

            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            date = dateFormat.parse(curseAPIFormat);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return date;
    }

    public static boolean isNewerDate(Date date1, Date date2){
        if(date1.compareTo(date2) > 0){
            return true;
        }
        return false;
    }

    public static Date convertFromGithub(String githubFormat){
        String processed = githubFormat.replace("\"", "");
        Date date = null;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            date = dateFormat.parse(processed);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static Date convertFromTukui(String tukuiFormat){
        Date date = null;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            date = dateFormat.parse(tukuiFormat);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static Date convertFromWowInterface(String wowinterfaceFormat){
        Date date = null;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yy hh:mm a", Locale.US);
            date = dateFormat.parse(wowinterfaceFormat);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static Date convertFromWowAce(String wowAceFormat){
        return convertFromCurse(wowAceFormat);
    }

}
