package com.CAM.DataCollection.TwitchOwned.CurseForge;

import com.CAM.AddonManagement.Addon;
import com.CAM.DataCollection.DataCollectionException;
import com.CAM.DataCollection.TwitchOwned.CurseForge.CurseAddonReponse.CurseAddonResponse;
import com.CAM.HelperTools.AddonSource;
import com.CAM.HelperTools.Log;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class CurseForgeAPISearcher {

    private String baseUrl = "https://addons-ecs.forgesvc.net/api/v2/addon/search?categoryId=0&gameId=1&pageSize=0&searchFilter=";

    public ArrayList<CurseAddonResponse> search(String searchFilter) throws DataCollectionException {
        System.out.println("Searching...");
        String encodedSearchFilter = encodeValue(searchFilter);
        String url = baseUrl + encodedSearchFilter;
        Page page = jsonScrape(url);
        String json = page.getWebResponse().getContentAsString();
        Gson gson = new Gson();
        return gson.fromJson(json, new TypeToken<ArrayList<CurseAddonResponse>>(){}.getType());
    }

    public CurseAddonResponse findCorrespondingAddon(Addon addon) throws DataCollectionException {
        ArrayList<CurseAddonResponse> addons = search(addon.getName());
        for(CurseAddonResponse response : addons){
            if(addon.getOrigin().startsWith(response.websiteUrl)) {
                return response;
            }
        }
        return null;
    }

    private static String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }

    private Page jsonScrape(String url) throws DataCollectionException {
        WebClient client = new WebClient();
        client.getOptions().setJavaScriptEnabled(false);
        client.getOptions().setCssEnabled(false);
        client.getOptions().setUseInsecureSSL(true);

        Page page;

        try {
            page = client.getPage(url);
        } catch (FailingHttpStatusCodeException e){
            Log.verbose("Scrape resulted in " + e.getStatusCode());
            throw new DataCollectionException(getAddonSource(), e);
        } catch (IOException e) {
            throw new DataCollectionException(getAddonSource(), e);
        }
        return page;
    }

    public AddonSource getAddonSource(){
        return AddonSource.CURSEFORGE;
    }
}
