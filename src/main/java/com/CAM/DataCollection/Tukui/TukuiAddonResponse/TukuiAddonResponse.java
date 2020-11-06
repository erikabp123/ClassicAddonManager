package com.CAM.DataCollection.Tukui.TukuiAddonResponse;

import com.CAM.DataCollection.SearchedAddonRequest;
import com.CAM.HelperTools.AddonSource;

import java.util.regex.Pattern;

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
}
