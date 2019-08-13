package com.CAM.HelperTools;

import java.net.URL;

public class UrlInfo {

    public static boolean isValidAddonUrl(String origin){
        Log.verbose("validating addon url ...");
        if(!isValidURL(origin)){
            Log.verbose("Not valid url format!");
            return false;
        }
        if(origin.contains("curseforge.com")){
            Log.verbose("curseforge link detected!");
            return isValidCurseForgeUrl(origin);
        } if(origin.contains("github.com")){
            Log.verbose("github link detected!");
            return isValidGithubUrl(origin);
        }
        Log.verbose("Invalid website choice!");
        return false;
    }

    private static Boolean isValidCurseForgeUrl(String origin){
        //TODO: Change to regex?
        String curseforgePrefix = "https://www.curseforge.com/wow/addons/";
        int prefixLength = curseforgePrefix.length();
        if(!isValidURL(origin)){
            return false;
        }
        if(!origin.startsWith(curseforgePrefix)){
            return false;
        }
        if(origin.length() == prefixLength){
            return false;
        }
        return true;
    }

    private static boolean isValidGithubUrl(String origin) {
        return false;
    }

    private static boolean isValidURL(String url) {
        try {
            new URL(url).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String trimCurseForgeUrl(String origin){
        String[] parts = origin.split("/");
        if(parts.length < 5){
            return origin;
        }
        String frankenstein = parts[0];
        for(int i=1; i<6; i++){
            frankenstein =  frankenstein + "/" + parts[i];
        }
        return frankenstein;
    }


}
