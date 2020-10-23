package com.CAM.DataCollection.WowInterface;

import com.CAM.DataCollection.API;
import com.CAM.DataCollection.DataCollectionException;
import com.CAM.DataCollection.WowInterface.WowInterfaceAddonResponse.WowInterfaceFile;
import com.CAM.HelperTools.AddonSource;
import com.gargoylesoftware.htmlunit.Page;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WowInterfaceAPI extends API {

    private final String addonBaseUrl = "https://api.mmoui.com/v3/game/WOW/filedetails/";
    private int projectID;
    private WowInterfaceFile fileToUse;

    public WowInterfaceAPI(int projectID) throws DataCollectionException {
        super(null, AddonSource.WOWINTERFACE);
        this.projectID = projectID;
        fileToUse = fetchAddonInfo();
    }

    public static int extractAddonNumber(String origin) {
        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(origin);
        if(m.find()) return Integer.parseInt(m.group(0));
        else throw new IllegalArgumentException("URL for WowInterface invalid as it contains no addon number! " + origin);
    }

    private WowInterfaceFile fetchAddonInfo() throws DataCollectionException {
        String url = addonBaseUrl + projectID + ".json";
        System.out.println(url);
        Page page = fetchJson(url);
        String jsonResponse = page.getWebResponse().getContentAsString();
        Gson gson = new Gson();
        ArrayList<WowInterfaceFile> files = gson.fromJson(jsonResponse, new TypeToken<ArrayList<WowInterfaceFile>>(){}.getType());
        return files.get(0);
    }

    @Override
    public String getDownloadLink() throws DataCollectionException {
        return fileToUse.UIDownload;
    }

    @Override
    public Date getLastUpdated() throws DataCollectionException {
        return new Date(fileToUse.UIDate);
    }

    @Override
    public String getName() throws DataCollectionException {
        return fileToUse.UIName;
    }

    @Override
    public String getAuthor() throws DataCollectionException {
        return fileToUse.UIAuthorName;
    }

    @Override
    public String getFileName() throws DataCollectionException {
        return fileToUse.UIFileName;
    }

    @Override
    public boolean isValidLink() throws DataCollectionException {
        return false;
    }

    @Override
    protected boolean apiFound() throws DataCollectionException {
        return false;
    }
}
