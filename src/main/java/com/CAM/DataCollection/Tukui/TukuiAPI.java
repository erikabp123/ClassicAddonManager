package com.CAM.DataCollection.Tukui;

import com.CAM.DataCollection.API;
import com.CAM.DataCollection.ScrapeException;
import com.CAM.DataCollection.Tukui.TukuiAddonResponse.TukuiAddonResponse;
import com.CAM.HelperTools.AddonSource;
import com.CAM.HelperTools.DateConverter;
import com.CAM.HelperTools.UrlInfo;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.Date;

public class TukuiAPI extends API {

    private JsonObject repoObject;
    private int addonNumber;
    private String baseUrl = "https://www.tukui.org/api.php?classic-addon=";
    private TukuiAddonResponse latestResponse;

    public TukuiAPI(int addonNumber) throws ScrapeException {
        super(null, AddonSource.TUKUI);
        this.repoObject = null;
        this.addonNumber = addonNumber;
        Gson gson = new Gson();
        latestResponse = gson.fromJson(getRepoObject(), TukuiAddonResponse.class);
        super.setUrl(latestResponse.getOrigin());
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

    private JsonObject getJsonObject(Page page) {
        WebResponse webResponse = page.getWebResponse();
        String json = webResponse.getContentAsString();
        return new Gson().fromJson(json, JsonObject.class);
    }

    private JsonObject getRepoObject() throws ScrapeException {
        if (repoObject != null) {
            return repoObject;
        }

        String url = baseUrl + addonNumber;
        repoObject = getJsonObject(jsonScrape(url));
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
    public boolean isValidLink() throws ScrapeException {
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
    protected boolean apiFound() throws ScrapeException {
        String api = "https://www.tukui.org/api.php?classic-addon=" + addonNumber;
        Page response = jsonScrape(api);
        if (response == null || response.getWebResponse().getContentAsString().equals("")) {
            return false;
        }
        repoObject = getJsonObject(response);
        return true;
    }

}
