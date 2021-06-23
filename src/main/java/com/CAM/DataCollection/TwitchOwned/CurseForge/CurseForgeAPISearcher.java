package com.CAM.DataCollection.TwitchOwned.CurseForge;

import com.CAM.AddonManagement.Addon;
import com.CAM.DataCollection.APISearcher;
import com.CAM.DataCollection.DataCollectionException;
import com.CAM.DataCollection.SearchedAddonRequest;
import com.CAM.DataCollection.TwitchOwned.CurseForge.CurseAddonReponse.CurseAddonResponse;
import com.CAM.HelperTools.GameSpecific.AddonSource;
import com.CAM.HelperTools.Logging.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class CurseForgeAPISearcher extends APISearcher {

    private static final int pageSize = 100;

    //private String baseUrl = "https://addons-ecs.forgesvc.net/api/v2/addon/search?categoryId=0&gameId=1&pageSize=" + pageSize + "&searchFilter=";
    private final String baseUrl = "https://addons-ecs.forgesvc.net/api/v2/addon/search?categoryId=0&gameId=1&searchFilter=";

    public ArrayList<SearchedAddonRequest> search(String searchFilter) throws DataCollectionException {
        Log.verbose("Performing Curse search ...");
        String url = baseUrl;
        String json = fetchJson(url + encodeValue(searchFilter));
        Gson gson = new Gson();
        ArrayList<CurseAddonResponse> unfiltered = gson.fromJson(json, new TypeToken<ArrayList<CurseAddonResponse>>() {
        }.getType());
        ArrayList<SearchedAddonRequest> filtered = filterResponse(unfiltered, searchFilter);
        Log.verbose("Finished Curse search!");
        return filtered;
    }

    public CurseAddonResponse findCorrespondingAddon(Addon addon) throws DataCollectionException {
        ArrayList<SearchedAddonRequest> addons = search(addon.getName());
        for(SearchedAddonRequest response : addons){
            CurseAddonResponse castResponse = (CurseAddonResponse) response;
            if(addon.getOrigin().startsWith(castResponse.websiteUrl)) {
                return castResponse;
            }
        }

        return findCorrespondingAddon(addon, 0);
    }

    public CurseAddonResponse findCorrespondingAddon(Addon addon, int index) throws DataCollectionException {
        if(index >= addon.getName().length()) return null;
        ArrayList<SearchedAddonRequest> addons = search(addon.getName().substring(0, index));
        for(SearchedAddonRequest response : addons){
            CurseAddonResponse castResponse = (CurseAddonResponse) response;
            if(addon.getOrigin().startsWith(castResponse.websiteUrl)) {
                return castResponse;
            }
        }

        return findCorrespondingAddon(addon, index+1);
    }

    private static String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }

    @Override
    public AddonSource getAddonSource(){
        return AddonSource.CURSEFORGE;
    }
}
