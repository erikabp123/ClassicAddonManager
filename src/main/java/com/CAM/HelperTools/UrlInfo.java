package com.CAM.HelperTools;

import com.CAM.DataCollection.*;
import com.CAM.DataCollection.Github.GitHubScraper;
import com.CAM.DataCollection.Tukui.TukuiScraper;
import com.CAM.DataCollection.TwitchOwned.CurseForge.CurseForgeScraper;
import com.CAM.DataCollection.TwitchOwned.WowAce.WowAceScraper;
import com.CAM.DataCollection.WowInterface.WowInterfaceScraper;

import java.net.URL;

import static com.CAM.HelperTools.AddonSource.*;

public class UrlInfo {

    public boolean isValid;
    public AddonSource addonSource;

    public UrlInfo(boolean isValid, AddonSource addonSource){
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
        } if(origin.contains("tukui.org")){
            Log.verbose("Tukui link detected!");
            urlInfo.addonSource = TUKUI;
            urlInfo.isValid = isValidTukuiUrl(origin);
            return urlInfo;
        } if(origin.contains("wowace.com")){
            Log.verbose("WowAce link detected!");
            urlInfo.addonSource = WOWACE;
            urlInfo.isValid = isValidWowAceUrl(origin);
            return urlInfo;
        }
        Log.verbose("Invalid website choice!");
        return urlInfo;
    }

    public static boolean isValidWowAceUrl(String origin) {
        String wowAcePrefix = "https://www.wowace.com/projects/";
        int prefixLength = wowAcePrefix.length();
        if(!isValidURL(origin)){
            return false;
        }
        if(!origin.startsWith(wowAcePrefix)){
            return false;
        }
        if(origin.length() == prefixLength){
            return false;
        }
        return true;
    }

    public static boolean isValidCurseForgeUrl(String origin){
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

    public static boolean isValidGithubUrl(String origin) {
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

    public static boolean isValidWowInterfaceUrl(String origin) {
        String wowinterfacePrefix = "https://www.wowinterface.com/downloads/";
        if(!isValidURL(origin)){
            return false;
        }
        if(!origin.startsWith(wowinterfacePrefix)){
            return false;
        }
        return true;
    }

    public static boolean isValidTukuiUrl(String origin) {
        boolean correctPrefix = origin.startsWith("https://www.tukui.org/classic-addons.php?id=");
        if(!isValidURL(origin)){
            return false;
        }
        if (!correctPrefix) {
            return false;
        }
        return true;
    }

    public static boolean isValidURL(String url) {
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

    public static String trimWowAceUrl(String origin){
        String[] parts = origin.split("/");
        if(parts.length < 6){
            return origin;
        }
        String frankenstein = parts[0];
        for(int i=1; i<5; i++){
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

    public static String trimTukuiUrl(String origin){
        String prefix = "https://www.tukui.org/classic-addons.php?id=";
        int addonNumber = TukuiScraper.extractAddonNumber(origin);
        return prefix + addonNumber;
    }

    public static AddonSource getAddonSource(String origin){
        if(origin.contains("curseforge.com")){
            return AddonSource.CURSEFORGE;
        } if(origin.contains("github.com")){
            return AddonSource.GITHUB;
        } if(origin.contains("wowinterface.com")){
            return AddonSource.WOWINTERFACE;
        } if(origin.contains("tukui.org")){
            return AddonSource.TUKUI;
        } if(origin.contains("wowace.com")){
            return AddonSource.WOWACE;
        }
        return null;
    }

    public static Scraper getCorrespondingScraper(AddonSource addonSource, String origin, boolean updatingAddon, String branch, boolean releases) throws ScrapeException{
        switch (addonSource){
            case CURSEFORGE:
                return CurseForgeScraper.makeScraper(origin, updatingAddon);
            case GITHUB:
                return new GitHubScraper(origin, branch, releases, updatingAddon);
            case WOWINTERFACE:
                return new WowInterfaceScraper(origin, updatingAddon);
            case TUKUI:
                return new TukuiScraper(origin, updatingAddon);
            case WOWACE:
                return WowAceScraper.makeScraper(origin, updatingAddon);
        }
        return null;
    }

    public static String trimString(String origin, AddonSource addonSource){
        String trimmedOrigin = null;
        switch (addonSource) {
            case CURSEFORGE:
                trimmedOrigin = UrlInfo.trimCurseForgeUrl(origin);
                break;
            case GITHUB:
                trimmedOrigin = UrlInfo.trimGitHubUrl(origin);
                break;
            case WOWINTERFACE:
                trimmedOrigin = UrlInfo.trimWowInterfaceUrl(origin);
                break;
            case TUKUI:
                trimmedOrigin = UrlInfo.trimTukuiUrl(origin);
                break;
            case WOWACE:
                trimmedOrigin = UrlInfo.trimWowAceUrl(origin);
                break;
        }
        return trimmedOrigin;
    }
}
