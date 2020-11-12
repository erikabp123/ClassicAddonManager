package com.CAM.DataCollection;

import com.CAM.HelperTools.GameSpecific.AddonSource;

import java.util.List;
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

    public abstract AddonSource getSource();

    public abstract String getDonationLink();

    public abstract List<String> getScreenshots();

    public abstract List<String> getSupportedPatches();

    public double getWeight(){
        double sumWeight = 0;
        String name = getName().toLowerCase();
        String author = getAuthor().toLowerCase();
        String desc = getDescription().toLowerCase();
        if(contains(name, searchFilter)){
            double multiplier = 1.5;
            if(name.startsWith(searchFilter)) multiplier = 10;
            double weight = (searchFilter.length() * multiplier)/name.length();
            sumWeight += weight;
        }
        if(contains(author, searchFilter)){
            double multiplier = 0.5;
            if(author.startsWith(searchFilter)) multiplier = 0.75;
            double weight = (searchFilter.length() * multiplier)/author.length();
            sumWeight += weight;
        }
        if(contains(desc, searchFilter)){
            double weight = (searchFilter.length() * 0.1)/desc.length();
            sumWeight += weight;
        }
        return sumWeight;
    }

    private boolean contains(String source, String wantedStr){
        return Pattern.compile(Pattern.quote(wantedStr), Pattern.CASE_INSENSITIVE).matcher(source).find();
    }

    public void setSearchFilter(String searchFilter) {
        this.searchFilter = searchFilter.toLowerCase();
    }

    @Override
    public int compareTo(SearchedAddonRequest o) {
        // negative, as I want it to sort by HIGHEST value
        return -Double.compare(this.getWeight(), o.getWeight());
    }
}
