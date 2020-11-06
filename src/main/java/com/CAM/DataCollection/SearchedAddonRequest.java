package com.CAM.DataCollection;

import com.CAM.DataCollection.TwitchOwned.CurseForge.CurseAddonReponse.CurseAddonResponse;
import com.CAM.HelperTools.AddonSource;

import java.util.regex.Pattern;

public abstract class SearchedAddonRequest implements Comparable<SearchedAddonRequest> {

    private String searchFilter;

    @Override
    public String toString(){
        return getName() + " by " + getAuthor();
    }

    public abstract AddonSource getAddonSource();

    public abstract String getName();

    public abstract String getAuthor();

    public abstract String getOrigin();

    public abstract int getProjectId();

    public abstract String getDescription();

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
        if(contains(getDescription(), searchFilter)){
            double weight = (searchFilter.length() * 0.2)/getDescription().length();
            sumWeight += weight;
        }
        return sumWeight;
    }

    private boolean contains(String source, String wantedStr){
        return Pattern.compile(Pattern.quote(wantedStr), Pattern.CASE_INSENSITIVE).matcher(source).find();
    }

    public void setSearchFilter(String searchFilter) {
        this.searchFilter = searchFilter;
    }

    @Override
    public int compareTo(SearchedAddonRequest o) {
        // negative, as I want it to sort by HIGHEST value
        return -Double.compare(this.determineWeight(), o.determineWeight());
    }
}
