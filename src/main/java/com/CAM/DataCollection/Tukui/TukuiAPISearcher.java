package com.CAM.DataCollection.Tukui;

import com.CAM.DataCollection.APISearcher;
import com.CAM.DataCollection.DataCollectionException;
import com.CAM.DataCollection.Tukui.TukuiAddonResponse.TukuiAddonResponse;
import com.CAM.HelperTools.AddonSource;
import com.CAM.HelperTools.GameVersion;
import com.CAM.HelperTools.Log;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class TukuiAPISearcher extends APISearcher {

    private String baseUrl;
    private GameVersion gameVersion;

    public TukuiAPISearcher(GameVersion gameVersion){
        this.gameVersion = gameVersion;
        this.baseUrl = "https://www.tukui.org/api.php?" + gameVersion.getTukuiSuffix() + "=all";
    }

    public ArrayList<TukuiAddonResponse> search(String searchFilter) throws DataCollectionException {
        Log.verbose("Performing Tukui search ...");
        Page page = fetchJson(baseUrl);
        String json = page.getWebResponse().getContentAsString();
        Gson gson = new Gson();
        ArrayList<TukuiAddonResponse> unfilteredResponse = gson.fromJson(json, new TypeToken<ArrayList<TukuiAddonResponse>>() {
        }.getType());
        ArrayList<TukuiAddonResponse> filteredResponse = filterResponse(unfilteredResponse, searchFilter);
        Log.verbose("Finished Tukui search!");
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

    @Override
    public AddonSource getAddonSource() {
        return AddonSource.TUKUI;
    }
}
