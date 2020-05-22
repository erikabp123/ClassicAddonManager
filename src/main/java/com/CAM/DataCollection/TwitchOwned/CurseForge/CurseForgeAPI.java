package com.CAM.DataCollection.TwitchOwned.CurseForge;

import com.CAM.DataCollection.API;
import com.CAM.DataCollection.ScrapeException;
import com.CAM.DataCollection.TwitchOwned.CurseForge.CurseAddonReponse.CurseAddonResponse;
import com.CAM.DataCollection.TwitchOwned.CurseForge.CurseAddonReponse.CurseFile;
import com.CAM.DataCollection.TwitchOwned.TwitchSite;
import com.CAM.HelperTools.AddonSource;
import com.CAM.HelperTools.DateConverter;
import com.CAM.Settings.Preferences;
import com.gargoylesoftware.htmlunit.Page;
import com.google.gson.Gson;

import java.util.Date;

public class CurseForgeAPI extends API implements TwitchSite {

    private final String addonBaseUrl = "https://addons-ecs.forgesvc.net/api/v2/addon/";
    private int projectID;
    private CurseAddonResponse response;
    private CurseFile latestClassicFile;
    private CurseFile latestRetailFile;
    private CurseFile fileToUse;

    public CurseForgeAPI(int projectID) throws ScrapeException {
        super(null, AddonSource.CURSEFORGE);
        this.projectID = projectID;
        response = fetchAddonInfo();
        latestClassicFile = determineLatestFileByFlavor("wow_classic");
        latestRetailFile = determineLatestFileByFlavor("wow_retail");
        fileToUse = isClassicSupported()
                ? latestClassicFile
                : latestRetailFile;
    }

    @Override
    public boolean isClassicSupported(){
        return latestClassicFile != null;
    }

    private CurseAddonResponse fetchAddonInfo() throws ScrapeException {
        String url = addonBaseUrl + projectID;
        System.out.println(url);
        Page page = jsonScrape(url);
        String jsonResponse = page.getWebResponse().getContentAsString();
        Gson gson = new Gson();
        return gson.fromJson(jsonResponse, CurseAddonResponse.class);
    }

    private CurseFile determineLatestFileByFlavor(String flavor){
        CurseFile latestFile = null;
        for(CurseFile file : response.latestFiles){
            if(!file.gameVersionFlavor.equals(flavor)){
                continue;
            }
            if(latestFile == null){
                latestFile = file;
                continue;
            }
            if(Preferences.getInstance().cfReleasesOnly && file.releaseType != 1) {
                System.out.println("Not a release, skipping!");
                continue;
            } //skip non-releases if set in preferences
            Date curFileDate = DateConverter.convertFromCurseAPI(latestFile.fileDate);
            Date fileDate = DateConverter.convertFromCurseAPI(file.fileDate);
            if(DateConverter.isNewerDate(fileDate, curFileDate)){
                if(!latestFile.isAlternate && file.isAlternate){
                    continue;
                }
                latestFile = file;
            } else if(latestFile.isAlternate && !file.isAlternate){
                latestFile = file;
            }
        }
        return latestFile;
    }

    @Override
    public String getDownloadLink() {
        return fileToUse.downloadUrl;
    }

    @Override
    public Date getLastUpdated() {
        return DateConverter.convertFromCurseAPI(fileToUse.fileDate);
    }

    @Override
    public String getName() {
        return response.name;
    }

    @Override
    public String getAuthor() {
        return response.authors.get(0).name;
    }

    @Override
    public String getFileName() {
        return fileToUse.fileName;
    }

    @Override
    public String getUrl(){
        return response.websiteUrl;
    }

    // These aren't actually necessary for this API

    @Override
    public boolean isValidLink() {
        return false;
    }

    @Override
    protected boolean apiFound() {
        return false;
    }
}
