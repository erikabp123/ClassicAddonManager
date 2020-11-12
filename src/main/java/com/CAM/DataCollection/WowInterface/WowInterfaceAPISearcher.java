package com.CAM.DataCollection.WowInterface;

import com.CAM.AddonManagement.Addon;
import com.CAM.DataCollection.APISearcher;
import com.CAM.DataCollection.DataCollectionException;
import com.CAM.DataCollection.SearchedAddonRequest;
import com.CAM.DataCollection.WowInterface.WowInterfaceAddonResponse.WowInterfaceAddonResponse;
import com.CAM.HelperTools.GameSpecific.AddonSource;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

public class WowInterfaceAPISearcher extends APISearcher {

    private String baseUrl = "https://api.mmoui.com/v3/game/WOW/filelist.json";

    public ArrayList<SearchedAddonRequest> search(String searchFilter) throws DataCollectionException {
        System.out.println("Searching...");
        ArrayList<WowInterfaceAddonResponse> responses;
        String json = fetchJson(baseUrl);
        Gson gson = new Gson();
        responses = gson.fromJson(json, new TypeToken<ArrayList<WowInterfaceAddonResponse>>(){}.getType());
        return filterResponse(responses, searchFilter);
    }

    public WowInterfaceAddonResponse findCorrespondingAddon(Addon addon) throws DataCollectionException {
        ArrayList<SearchedAddonRequest> addons = search(addon.getName());
        for(SearchedAddonRequest response : addons){
            WowInterfaceAddonResponse castResponse = (WowInterfaceAddonResponse) response;
            if(addon.getOrigin().startsWith(castResponse.UIFileInfoURL)) {
                return castResponse;
            }
        }
        return null;
    }

    @Override
    public AddonSource getAddonSource(){
        return AddonSource.WOWINTERFACE;
    }


}
