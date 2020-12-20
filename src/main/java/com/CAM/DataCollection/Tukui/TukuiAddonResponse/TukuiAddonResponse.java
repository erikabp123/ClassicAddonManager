package com.CAM.DataCollection.Tukui.TukuiAddonResponse;

import com.CAM.DataCollection.SearchedAddonRequest;
import com.CAM.DataCollection.TwitchOwned.CurseForge.CurseAddonReponse.CurseGameVersionFile;
import com.CAM.HelperTools.GameSpecific.AddonSource;
import com.CAM.HelperTools.GameSpecific.GameVersion;
import com.CAM.HelperTools.GameSpecific.PatchNumber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TukuiAddonResponse extends SearchedAddonRequest {
    public String id;
    public String name;
    public String small_desc;
    public String author;
    public String version;
    public String screenshot_url;
    public String url;
    public String category;
    public String downloads;
    public String lastupdate;
    public String patch;
    public String last_download;
    public String web_url;
    public String changelog;
    public String donate_url;

    @Override
    public AddonSource getAddonSource() {
        return AddonSource.TUKUI;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getAuthor() {
        return author;
    }

    @Override
    public String getOrigin() {
        return web_url;
    }

    @Override
    public int getProjectId() {
        return Integer.parseInt(id);
    }

    @Override
    public String getDescription() {
        return small_desc;
    }

    @Override
    public AddonSource getSource() {
        return AddonSource.TUKUI;
    }

    @Override
    public String getDonationLink() {
        return donate_url;
    }

    @Override
    public List<String> getScreenshots() {
        List<String> screenshots = new ArrayList<>();
        screenshots.add(screenshot_url);
        return screenshots;
    }

    @Override
    public List<String> getSupportedPatches() {
        List<String> patches = new ArrayList<>();
        if(patch == null || patch.equals("null")) patches.add("Unknown");
        else patches.add(patch);
        return patches;
    }
}
