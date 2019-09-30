package com.CAM.DataCollection.Tukui.TukuiAddonResponse;

import com.CAM.DataCollection.SearchedAddonRequest;
import com.CAM.HelperTools.AddonSource;

import java.util.regex.Pattern;

public class TukuiAddonResponse implements Comparable<TukuiAddonResponse>, SearchedAddonRequest {
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
    
    public String searchFilter;

    public double determineWeight(){

        double sumWeight = 0;
        if(contains(name, searchFilter)){
            double weight = (searchFilter.length() * 1.0)/name.length();
            sumWeight += weight;
        }
        if(contains(author, searchFilter)){
            double weight = (searchFilter.length() * 0.5)/author.length();
            sumWeight += weight;
        }
        if(contains(small_desc, searchFilter)){
            double weight = (searchFilter.length() * 0.2)/small_desc.length();
            sumWeight += weight;
        }
        return sumWeight;
    }

    private boolean contains(String source, String wantedStr){
        return Pattern.compile(Pattern.quote(wantedStr), Pattern.CASE_INSENSITIVE).matcher(source).find();
    }

    @Override
    public int compareTo(TukuiAddonResponse o) {
        // negative, as I want it to sort by HIGHEST value
        return -Double.compare(this.determineWeight(), o.determineWeight());
    }

    @Override
    public String toString(){
        return getName() + " by " + getAuthor();
    }

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
}
