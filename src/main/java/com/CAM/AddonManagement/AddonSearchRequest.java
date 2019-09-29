package com.CAM.AddonManagement;

import com.CAM.DataCollection.TwitchOwned.CurseForge.CurseAddonReponse.CurseAddonResponse;
import com.CAM.HelperTools.AddonSource;

import java.util.ArrayList;

public class AddonSearchRequest {
    public int projectId;
    public AddonSource addonSource;
    public ArrayList<CurseAddonResponse> twitchResponse;
}
