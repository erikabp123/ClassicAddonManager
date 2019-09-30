package com.CAM.DataCollection.Tukui;

import com.CAM.DataCollection.ScrapeException;
import com.CAM.DataCollection.Tukui.TukuiAddonResponse.TukuiAddonResponse;
import com.CAM.HelperTools.AddonSource;
import com.CAM.HelperTools.Log;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class TukuiAPISearcher {

    private String baseUrl = "https://www.tukui.org/api.php?classic-addons=all";

    public ArrayList<TukuiAddonResponse> search(String searchFilter) throws ScrapeException {
        System.out.println("Searching...");
        Page page = jsonScrape(baseUrl);
        String json = page.getWebResponse().getContentAsString();
        Gson gson = new Gson();
        ArrayList<TukuiAddonResponse> unfilteredResponse = gson.fromJson(json, new TypeToken<ArrayList<TukuiAddonResponse>>() {
        }.getType());
        ArrayList<TukuiAddonResponse> filteredResponse = filterResponse(unfilteredResponse, searchFilter);
        return filteredResponse;
    }

    private ArrayList<TukuiAddonResponse> filterResponse(ArrayList<TukuiAddonResponse> unfilteredResponse, String searchFilter) {
        ArrayList<TukuiAddonResponse> filteredResponse = new ArrayList<>();

        for (TukuiAddonResponse response : unfilteredResponse) {
            response.searchFilter = searchFilter;
            double weight = response.determineWeight();
            if (weight > 0) {
                filteredResponse.add(response);
            }
        }
        Collections.sort(filteredResponse);
        return filteredResponse;
    }

    private Page jsonScrape(String url) throws ScrapeException {
        WebClient client = new WebClient();
        client.getOptions().setJavaScriptEnabled(false);
        client.getOptions().setCssEnabled(false);
        client.getOptions().setUseInsecureSSL(true);

        Page page;

        try {
            page = client.getPage(url);
        } catch (FailingHttpStatusCodeException e) {
            Log.verbose("Scrape resulted in " + e.getStatusCode());
            throw new ScrapeException(getAddonSource(), e);
        } catch (IOException e) {
            throw new ScrapeException(getAddonSource(), e);
        }
        return page;
    }

    public AddonSource getAddonSource() {
        return AddonSource.TUKUI;
    }
}
