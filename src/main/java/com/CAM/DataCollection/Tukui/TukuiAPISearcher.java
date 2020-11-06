package com.CAM.DataCollection.Tukui;

import com.CAM.DataCollection.APISearcher;
import com.CAM.DataCollection.DataCollectionException;
import com.CAM.DataCollection.SearchedAddonRequest;
import com.CAM.DataCollection.Tukui.TukuiAddonResponse.TukuiAddonResponse;
import com.CAM.DataCollection.TwitchOwned.CurseForge.CurseAddonReponse.CurseAddonResponse;
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

    public ArrayList<SearchedAddonRequest> search(String searchFilter) throws DataCollectionException {
        Log.verbose("Performing Tukui search ...");
        String json = fetchJson(baseUrl);
        Gson gson = new Gson();
        ArrayList<TukuiAddonResponse> unfilteredResponse = gson.fromJson(json, new TypeToken<ArrayList<TukuiAddonResponse>>(){}.getType());
        ArrayList<SearchedAddonRequest> filtered = filterResponse(unfilteredResponse, searchFilter);
        Log.verbose("Finished Tukui search!");
        return filtered;
    }

    @Override
    public AddonSource getAddonSource() {
        return AddonSource.TUKUI;
    }
}
