package com.CAM.DataCollection.TwitchOwned.CurseForge;

import com.CAM.DataCollection.API;
import com.CAM.DataCollection.ScrapeException;
import com.CAM.DataCollection.TwitchOwned.CurseForge.CurseAddonReponse.CurseAddonResponse;
import com.CAM.DataCollection.TwitchOwned.CurseForge.CurseAddonReponse.CurseFile;
import com.CAM.HelperTools.AddonSource;
import com.gargoylesoftware.htmlunit.Page;
import com.google.gson.Gson;

import java.util.Date;

public class CurseForgeAPI extends API {

    private final String addonBaseUrl = "https://addons-ecs.forgesvc.net/api/v2/addon/";
    private int projectID;
    private CurseAddonResponse response;
    private CurseFile latestClassicFile;

    public CurseForgeAPI(int projectID, AddonSource source) throws ScrapeException {
        super(null, source);
        this.projectID = projectID;
        response = fetchAddonInfo();
        latestClassicFile = determineLatestClassicFile();
    }

    private CurseAddonResponse fetchAddonInfo() throws ScrapeException {
        String url = addonBaseUrl + projectID;
        Page page = jsonScrape(url);
        String jsonResponse = page.getWebResponse().getContentAsString();
        Gson gson = new Gson();
        return gson.fromJson(jsonResponse, CurseAddonResponse.class);
    }

    private CurseFile determineLatestClassicFile(){
        for(CurseFile file : response.latestFiles){
            if(file.gameVersionFlavor.equals("wow_classic")){
                return file;
            }
        }
        return null;
    }

    @Override
    public String getDownloadLink() throws ScrapeException {
        return latestClassicFile.downloadUrl;
    }

    @Override
    public Date getLastUpdated() throws ScrapeException {
        return null;
    }

    @Override
    public String getName() throws ScrapeException {
        return null;
    }

    @Override
    public String getAuthor() throws ScrapeException {
        return null;
    }

    @Override
    public String getFileName() throws ScrapeException {
        return null;
    }

    @Override
    public boolean isValidLink() throws ScrapeException {
        return false;
    }

    @Override
    protected boolean apiFound() throws ScrapeException {
        return false;
    }
}
