package com.CAM.DataCollection.WowInterface.WowInterfaceAddonResponse;

import com.CAM.DataCollection.SearchedAddonRequest;
import com.CAM.DataCollection.Tukui.TukuiAddonResponse.TukuiAddonResponse;
import com.CAM.HelperTools.AddonSource;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class WowInterfaceAddonResponse implements Comparable<WowInterfaceAddonResponse>, SearchedAddonRequest {

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

    public String searchFilter;

    public double determineWeight(){

        double sumWeight = 0;
        if(contains(getName(), searchFilter)){
            double weight = (searchFilter.length() * 1.0)/getName().length();
            sumWeight += weight;
        }
        if(contains(getAuthor(), searchFilter)){
            double weight = (searchFilter.length() * 0.5)/getAuthor().length();
            sumWeight += weight;
        }
        return sumWeight;
    }

    private boolean contains(String source, String wantedStr){
        return Pattern.compile(Pattern.quote(wantedStr), Pattern.CASE_INSENSITIVE).matcher(source).find();
    }

    @Override
    public int compareTo(WowInterfaceAddonResponse o) {
        // negative, as I want it to sort by HIGHEST value
        return -Double.compare(this.determineWeight(), o.determineWeight());
    }


    @Override
    public String toString(){
        return getName() + " by " + getAuthor();
    }

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
}

class CompatabilityUI{
    String version;
    String name;
}
