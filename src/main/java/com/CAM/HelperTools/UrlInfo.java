package com.CAM.HelperTools;

import java.net.URL;

import static com.CAM.HelperTools.AddonSource.*;

public class UrlInfo {

    public boolean isValid;
    public AddonSource addonSource;

    public UrlInfo(Boolean isValid, AddonSource addonSource){
        this.isValid = isValid;
        this.addonSource = addonSource;
    }

    public static UrlInfo examineAddonUrl(String origin){
        Log.verbose("validating addon url ...");
        UrlInfo urlInfo = new UrlInfo(false, null);
        if(!isValidURL(origin)){
            Log.verbose("Not valid url format!");
            return urlInfo;
        }
        if(origin.contains("curseforge.com")){
            Log.verbose("curseforge link detected!");
            urlInfo.addonSource = CURSEFORGE;
            urlInfo.isValid = isValidCurseForgeUrl(origin);
            return urlInfo;
        } if(origin.contains("github.com")){
            Log.verbose("github link detected!");
            urlInfo.addonSource = GITHUB;
            urlInfo.isValid = isValidGithubUrl(origin);
            return urlInfo;
        } if(origin.contains("wowinterface.com")){
            Log.verbose("WowInterface link detected!");
            urlInfo.addonSource = WOWINTERFACE;
            urlInfo.isValid = isValidWowInterfaceUrl(origin);
            return urlInfo;
        }
        Log.verbose("Invalid website choice!");
        return urlInfo;
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
        String githubPrefix = "https://github.com/";
        if(!isValidURL(origin)){
            return false;
        }
        if(!origin.startsWith(githubPrefix)){
            return false;
        }
        String[] parts = origin.split("/");
        if(parts.length < 4){
            return false;
        }
        return true;
    }

    private static boolean isValidWowInterfaceUrl(String origin) {
        String wowinterfacePrefix = "https://www.wowinterface.com/downloads/";
        if(!isValidURL(origin)){
            return false;
        }
        if(!origin.startsWith(wowinterfacePrefix)){
            return false;
        }
        return true;
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
        if(parts.length < 7){
            return origin;
        }
        String frankenstein = parts[0];
        for(int i=1; i<6; i++){
            frankenstein =  frankenstein + "/" + parts[i];
        }
        return frankenstein;
    }

    public static String trimGitHubUrl(String origin){
        String[] parts = origin.split("/");
        if(parts.length < 5){
            return origin;
        }
        String frankenstein = parts[0];
        for(int i=1; i<5; i++){
            frankenstein =  frankenstein + "/" + parts[i];
        }
        return frankenstein;
    }

    public static String trimWowInterfaceUrl(String origin){
        String[] parts = origin.split("#");
        return parts[0];
    }

}
