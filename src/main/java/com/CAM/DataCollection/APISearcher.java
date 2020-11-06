package com.CAM.DataCollection;

import com.CAM.DataCollection.Cache.WebsiteCache;
import com.CAM.DataCollection.TwitchOwned.CurseForge.CurseAddonReponse.CurseAddonResponse;
import com.CAM.HelperTools.AddonSource;
import com.CAM.HelperTools.Log;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public abstract class APISearcher extends PageFetcher {

    public abstract AddonSource getAddonSource();

    public ArrayList<SearchedAddonRequest> filterResponse(ArrayList<? extends SearchedAddonRequest> unfilteredResponse, String searchFilter) {
        ArrayList<SearchedAddonRequest> filteredResponse = new ArrayList<>();

        for (SearchedAddonRequest response : unfilteredResponse) {
            response.setSearchFilter(searchFilter);
            double weight = response.determineWeight();
            if (weight > 0) {
                filteredResponse.add(response);
            }
        }
        Collections.sort(filteredResponse);
        return filteredResponse;
    }

}
