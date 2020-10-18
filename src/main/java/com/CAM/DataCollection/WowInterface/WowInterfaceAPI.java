package com.CAM.DataCollection.WowInterface;

import com.CAM.DataCollection.API;
import com.CAM.DataCollection.ScrapeException;
import com.CAM.DataCollection.TwitchOwned.CurseForge.CurseAddonReponse.CurseAddonResponse;
import com.CAM.DataCollection.WowInterface.WowInterfaceAddonResponse.WowInterfaceFile;
import com.CAM.HelperTools.AddonSource;
import com.gargoylesoftware.htmlunit.Page;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Date;

public class WowInterfaceAPI extends API {

    private final String addonBaseUrl = "https://api.mmoui.com/v3/game/WOW/filedetails/";
    private int projectID;
    private WowInterfaceFile fileToUse;

    public WowInterfaceAPI(int projectID) throws ScrapeException {
        super(null, AddonSource.WOWINTERFACE);
        this.projectID = projectID;
        fileToUse = fetchAddonInfo();
    }

    private WowInterfaceFile fetchAddonInfo() throws ScrapeException {
        String url = addonBaseUrl + projectID + ".json";
        System.out.println(url);
        Page page = jsonScrape(url);
        String jsonResponse = page.getWebResponse().getContentAsString();
        Gson gson = new Gson();
        ArrayList<WowInterfaceFile> files = gson.fromJson(jsonResponse, new TypeToken<ArrayList<WowInterfaceFile>>(){}.getType());
        return files.get(0);
    }

    @Override
    public String getDownloadLink() throws ScrapeException {
        return fileToUse.UIDownload;
    }

    @Override
    public Date getLastUpdated() throws ScrapeException {
        return new Date(fileToUse.UIDate);
    }

    @Override
    public String getName() throws ScrapeException {
        return fileToUse.UIName;
    }

    @Override
    public String getAuthor() throws ScrapeException {
        return fileToUse.UIAuthorName;
    }

    @Override
    public String getFileName() throws ScrapeException {
        return fileToUse.UIFileName;
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
