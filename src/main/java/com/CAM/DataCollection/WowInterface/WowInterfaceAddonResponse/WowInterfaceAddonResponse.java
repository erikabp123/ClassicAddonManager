package com.CAM.DataCollection.WowInterface.WowInterfaceAddonResponse;

import com.CAM.DataCollection.SearchedAddonRequest;
import com.CAM.DataCollection.Tukui.TukuiAddonResponse.TukuiAddonResponse;
import com.CAM.HelperTools.AddonSource;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class WowInterfaceAddonResponse extends SearchedAddonRequest {

    public int UID;
    public int UICATID;
    public String UIVersion;
    public long UIDate;
    public String UIName;
    public String UIAuthorName;
    public String UIFileInfoURL;
    public long UIDownloadTotal;
    public int UIDownloadMonthly;
    public int UIFavoriteTotal;
    public ArrayList<CompatabilityUI> UICompatibility;
    public ArrayList<String> UIDir;
    public ArrayList<String> UIIMG_Thumbs;
    public ArrayList<String> UIIMGs;

    @Override
    public AddonSource getAddonSource() {
        return AddonSource.WOWINTERFACE;
    }

    @Override
    public String getName() {
        return UIName;
    }

    @Override
    public String getAuthor() {
        return UIAuthorName;
    }

    @Override
    public String getOrigin() {
        return UIFileInfoURL;
    }

    @Override
    public int getProjectId() {
        return UID;
    }

    @Override
    public String getDescription() {
        return "";
    }
}

class CompatabilityUI{
    String version;
    String name;
}
