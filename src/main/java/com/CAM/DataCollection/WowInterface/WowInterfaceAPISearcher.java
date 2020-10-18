package com.CAM.DataCollection.WowInterface;

import com.CAM.AddonManagement.Addon;
import com.CAM.DataCollection.Cache.Cache;
import com.CAM.DataCollection.ScrapeException;
import com.CAM.DataCollection.Tukui.TukuiAddonResponse.TukuiAddonResponse;
import com.CAM.DataCollection.TwitchOwned.CurseForge.CurseAddonReponse.CurseAddonResponse;
import com.CAM.DataCollection.WowInterface.WowInterfaceAddonResponse.WowInterfaceAddonResponse;
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
import java.util.Collections;

public class WowInterfaceAPISearcher {

    private String baseUrl = "https://api.mmoui.com/v3/game/WOW/filelist.json";

    public ArrayList<WowInterfaceAddonResponse> search(String searchFilter) throws ScrapeException {
        System.out.println("Searching...");
        ArrayList<WowInterfaceAddonResponse> responses;
        Cache cache = Cache.getInstance();
        Object cachedList = cache.getCacheValue(AddonSource.WOWINTERFACE);
        if(cachedList == null){
            Page page = jsonScrape(baseUrl);
            String json = page.getWebResponse().getContentAsString();
            Gson gson = new Gson();
            responses = gson.fromJson(json, new TypeToken<ArrayList<WowInterfaceAddonResponse>>(){}.getType());
            cache.cacheValue(AddonSource.WOWINTERFACE, responses);
        } else {
            responses = (ArrayList<WowInterfaceAddonResponse>) cachedList;
        }
        return filterResponse(responses, searchFilter);
    }

    private ArrayList<WowInterfaceAddonResponse> filterResponse(ArrayList<WowInterfaceAddonResponse> unfilteredResponse, String searchFilter) {
        ArrayList<WowInterfaceAddonResponse> validResponses = new ArrayList<>();
        for(WowInterfaceAddonResponse response: unfilteredResponse){
            if(response.UICompatibility == null) continue;
            validResponses.add(response);
        }

        ArrayList<WowInterfaceAddonResponse> filteredResponse = new ArrayList<>();

        for (WowInterfaceAddonResponse response : validResponses) {
            response.searchFilter = searchFilter;
            double weight = response.determineWeight();
            if (weight > 0) {
                filteredResponse.add(response);
            }
        }
        Collections.sort(filteredResponse);

        if(filteredResponse.size() <= 20) return filteredResponse;
        return new ArrayList<>(filteredResponse.subList(0, 20));
    }

    public WowInterfaceAddonResponse findCorrespondingAddon(Addon addon) throws ScrapeException {
        ArrayList<WowInterfaceAddonResponse> addons = search(addon.getName());
        for(WowInterfaceAddonResponse response : addons){
            if(addon.getOrigin().startsWith(response.UIFileInfoURL)) {
                return response;
            }
        }
        return null;
    }

    private Page jsonScrape(String url) throws ScrapeException {
        WebClient client = new WebClient();
        client.getOptions().setJavaScriptEnabled(false);
        client.getOptions().setCssEnabled(false);
        client.getOptions().setUseInsecureSSL(true);

        Page page;

        try {
            page = client.getPage(url);
        } catch (FailingHttpStatusCodeException e){
            Log.verbose("Scrape resulted in " + e.getStatusCode());
            throw new ScrapeException(getAddonSource(), e);
        } catch (IOException e) {
            throw new ScrapeException(getAddonSource(), e);
        }
        return page;
    }

    public AddonSource getAddonSource(){
        return AddonSource.WOWINTERFACE;
    }


}