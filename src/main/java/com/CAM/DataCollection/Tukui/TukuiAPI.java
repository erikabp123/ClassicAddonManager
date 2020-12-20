package com.CAM.DataCollection.Tukui;

import com.CAM.DataCollection.API;
import com.CAM.DataCollection.DataCollectionException;
import com.CAM.DataCollection.Tukui.TukuiAddonResponse.TukuiAddonResponse;
import com.CAM.HelperTools.GameSpecific.AddonSource;
import com.CAM.HelperTools.DateConverter;
import com.CAM.HelperTools.GameSpecific.GameVersion;
import com.CAM.HelperTools.UrlInfo;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Date;

public class TukuiAPI extends API {

    private JsonObject repoObject;
    private int addonNumber;
    private String baseUrl;
    private TukuiAddonResponse latestResponse;

    public TukuiAPI(int addonNumber, GameVersion gameVersion) throws DataCollectionException {
        super(null, AddonSource.TUKUI);
        this.repoObject = null;
        this.addonNumber = addonNumber;
        baseUrl = "https://www.tukui.org/api.php?" + gameVersion.getTukuiSuffix() + "=all";
        String json = fetchJson(baseUrl);
        Gson gson = new Gson();
        ArrayList<TukuiAddonResponse> responses = gson.fromJson(json, new TypeToken<ArrayList<TukuiAddonResponse>>(){}.getType());

        latestResponse = getResponseById(responses);
        super.setUrl(latestResponse.getOrigin());
    }

    private TukuiAddonResponse getResponseById(ArrayList<TukuiAddonResponse> responses) throws DataCollectionException {
        for(TukuiAddonResponse response: responses){
            if(response.getProjectId() == addonNumber) return response;
        }
        throw new DataCollectionException(AddonSource.TUKUI, "Tukui project ID not found in response: (" + addonNumber + ")!");
    }

    public static int extractAddonNumber(String url) {
        String suffix = url.split("id=")[1];
        final StringBuilder sb = new StringBuilder(suffix.length());
        for (int i = 0; i < suffix.length(); i++) {
            final char c = suffix.charAt(i);
            if (c < 48 || c > 57) {
                break;
            }
            sb.append(c);
        }
        return Integer.parseInt(sb.toString());
    }

    @Override
    public String getDownloadLink() {
        return latestResponse.url;
    }

    @Override
    public Date getLastUpdated() {
        return DateConverter.convertFromTukui(latestResponse.lastupdate);
    }

    private JsonObject getJsonObject(String json) {
        return new Gson().fromJson(json, JsonObject.class);
    }

    private JsonObject getRepoObject() throws DataCollectionException {
        if (repoObject != null) {
            return repoObject;
        }

        String url = baseUrl + addonNumber;
        repoObject = getJsonObject(fetchJson(url));
        return repoObject;
    }

    @Override
    public String getName() { ;
        return latestResponse.getName();
    }

    @Override
    public String getAuthor() {
        return latestResponse.getAuthor();
    }

    @Override
    public String getFileName() {
        String fileName = getName() + "-" + "tukui";
        return fileName;
    }

    @Override
    public boolean isValidLink() throws DataCollectionException {
        if(!UrlInfo.isValidTukuiUrl(getUrl())){
            return false;
        }
        if (!apiFound()) {
            return false;
        }
        return true;
    }

    @Override
    public AddonSource getAddonSource() {
        return AddonSource.TUKUI;
    }

    @Override
    protected boolean apiFound() throws DataCollectionException {
        String api = baseUrl + addonNumber;
        String response = fetchJson(api);
        if (response == null || response.equals("")) {
            return false;
        }
        repoObject = getJsonObject(response);
        return true;
    }

}
