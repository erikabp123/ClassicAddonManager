package com.CAM.DataCollection;

import com.CAM.HelperTools.AddonSource;
import com.CAM.HelperTools.DateConverter;
import com.CAM.HelperTools.Log;
import com.CAM.HelperTools.UrlInfo;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class TukuiScraper extends Scraper {

    private JsonObject repoObject;
    private int addonNumber;

    public TukuiScraper(String url, boolean updatingAddon) throws ScrapeException {
        super(url);
        this.repoObject = null;
        this.addonNumber = extractAddonNumber(url);
        if (!updatingAddon && !isValidLink()) {
            throw new ScrapeException(AddonSource.TUKUI, "Invalid Tukui URL!");
        }
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
        String downloadPrefix = "https://www.tukui.org/classic-addons.php?download=";
        String downloadSuffix = addonNumber + ".zip";
        return downloadPrefix + downloadSuffix;
    }

    public Page jsonScrape(String url) throws ScrapeException {
        WebClient client = new WebClient();
        client.getOptions().setJavaScriptEnabled(false);
        client.getOptions().setCssEnabled(false);
        client.getOptions().setUseInsecureSSL(true);

        Page page = null;

        try {
            page = client.getPage(url);
        } catch (FailingHttpStatusCodeException e) {
            Log.verbose("Scrape resulted in " + e.getStatusCode());
            throw new ScrapeException(AddonSource.GITHUB, e);
        } catch (IOException e) {
            throw new ScrapeException(AddonSource.GITHUB, e);
        }
        return page;
    }

    @Override
    public Date getLastUpdated() throws ScrapeException {
        JsonObject jsonObject = getRepoObject();
        String tukuiDate = jsonObject.get("lastupdate").getAsString();
        Date date = DateConverter.convertFromTukui(tukuiDate);
        return date;
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
        String prefix = "https://www.tukui.org/api.php?classic-addon=";
        String url = prefix + addonNumber;
        repoObject = getJsonObject(jsonScrape(url));
        return repoObject;
    }

    @Override
    public String getName() throws ScrapeException {
        JsonObject jsonObject = getRepoObject();
        String name = jsonObject.get("name").getAsString();
        return name;
    }

    @Override
    public String getAuthor() throws ScrapeException {
        JsonObject jsonObject = getRepoObject();
        String author = jsonObject.get("author").getAsString();
        return author;
    }

    @Override
    public String getFileName() throws ScrapeException {
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

    private boolean apiFound() throws ScrapeException {
        String api = "https://www.tukui.org/api.php?classic-addon=" + addonNumber;
        Page response = jsonScrape(api);
        if (response == null) {
            return false;
        }
        return true;
    }

}
