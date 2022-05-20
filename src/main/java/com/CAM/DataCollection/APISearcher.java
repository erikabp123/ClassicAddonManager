package com.CAM.DataCollection;

import com.CAM.HelperTools.GameSpecific.AddonSource;

import java.util.ArrayList;
import java.util.Collections;

public abstract class APISearcher extends PageFetcher {

    public abstract AddonSource getAddonSource();

    public ArrayList<SearchedAddonRequest> filterResponse(ArrayList<? extends SearchedAddonRequest> unfilteredResponse, String searchFilter) {
        ArrayList<SearchedAddonRequest> filteredResponse = new ArrayList<>();

        if (unfilteredResponse == null) return filteredResponse;

        for (SearchedAddonRequest response : unfilteredResponse) {
            response.setSearchFilter(searchFilter);
            double weight = response.getWeight();
            if (weight > 0) {
                filteredResponse.add(response);
            }
        }
        Collections.sort(filteredResponse);
        return filteredResponse;
    }

}
