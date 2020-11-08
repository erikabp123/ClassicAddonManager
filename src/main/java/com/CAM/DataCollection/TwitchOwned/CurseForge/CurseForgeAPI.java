package com.CAM.DataCollection.TwitchOwned.CurseForge;

import com.CAM.DataCollection.API;
import com.CAM.DataCollection.DataCollectionException;
import com.CAM.DataCollection.TwitchOwned.CurseForge.CurseAddonReponse.CurseAddonResponse;
import com.CAM.DataCollection.TwitchOwned.CurseForge.CurseAddonReponse.CurseFile;
import com.CAM.DataCollection.TwitchOwned.TwitchSite;
import com.CAM.HelperTools.AddonSource;
import com.CAM.HelperTools.DateConverter;
import com.CAM.HelperTools.GameVersion;
import com.CAM.Settings.Preferences;
import com.gargoylesoftware.htmlunit.Page;
import com.google.gson.Gson;

import java.util.Date;
import java.util.HashMap;

public class CurseForgeAPI extends API implements TwitchSite {

    private final String addonBaseUrl = "https://addons-ecs.forgesvc.net/api/v2/addon/";
    private int projectID;
    private CurseAddonResponse response;
    private CurseFile fileToUse;
    private GameVersion gameVersion;
    private HashMap<GameVersion, CurseFile> latestGameVersionFiles;

    public CurseForgeAPI(int projectID, GameVersion gameVersion) throws DataCollectionException {
        super(null, AddonSource.CURSEFORGE);
        this.gameVersion = gameVersion;
        this.projectID = projectID;
        response = fetchAddonInfo();
        this.latestGameVersionFiles = new HashMap<>();
        for(GameVersion gv: GameVersion.values()){
            CurseFile latestFileByFlavor = determineLatestFileByFlavor(gv.getCurseFlavor());
            if(latestFileByFlavor == null) continue;
            latestGameVersionFiles.put(gv, latestFileByFlavor);
        }
        fileToUse = determineFileToUse();
    }

    private CurseFile determineFileToUse(){
        if(isGameVersionSupported()) return latestGameVersionFiles.get(gameVersion);
        return latestGameVersionFiles.get(latestGameVersionFiles.keySet().iterator().next());
    }

    @Override
    public boolean isGameVersionSupported(){
        return latestGameVersionFiles.containsKey(gameVersion);
    }

    private CurseAddonResponse fetchAddonInfo() throws DataCollectionException {
        String url = addonBaseUrl + projectID;
        String jsonResponse = fetchJson(url);
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
            if(Preferences.getInstance().isCfReleasesOnly() && file.releaseType != 1) {
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
