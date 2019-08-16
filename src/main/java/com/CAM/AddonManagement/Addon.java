package com.CAM.AddonManagement;

import com.CAM.DataCollection.*;
import com.CAM.HelperTools.DateConverter;
import com.CAM.HelperTools.Log;

import java.util.Date;

public class Addon implements Comparable<Addon> {
    private String name;
    private String author;
    private String origin;
    private Date lastUpdated;
    private String lastFileName;
    private String branch;
    private boolean releases;

    public Addon(String name, String author, String origin, String branch, boolean releases){
        this.name = name;
        this.author = author;
        this.origin = origin;
        this.branch = branch;
        this.releases = releases;
    }

    public boolean fetchUpdate(Scraper scraper){
        //TODO: Consider tracking folders installed so that deleting is easier
        Log.verbose("Attempting to fetch update ...");
        String downloadLink = scraper.getDownloadLink();
        FileDownloader downloader = new FileDownloader("downloads");
        String fileName = name + "_" + author + "_(" +scraper.getFileName() + ").zip";
        downloader.downloadFileMonitored(downloadLink, fileName);
        lastUpdated = scraper.getLastUpdated();
        lastFileName = fileName;
        Log.verbose("Successfully fetched new update!");
        return true;
    }

    public UpdateResponse checkForUpdate(){
        Scraper scraper = getScraper();
        UpdateResponse response = new UpdateResponse(scraper, true);

        // Check if addon has ever been updated through this program
        //TODO: consider whether redundant, could be useful later for determining if addon was installed manually though
        if(lastUpdated == null){
            return response;
        }
        // Get the date of the last update as seen by scrape
        Date lastUpdateScrape = scraper.getLastUpdated();
        // Check if scrape has seen a newer update
        if(DateConverter.isNewerDate(lastUpdateScrape, lastUpdated)){
            return response;
        }
        // There are no new updates
        response.setUpdateAvailable(false);
        return response;
    }

    private Scraper getScraper(){
        if(origin.contains("curseforge.com")){
            return new CurseForgeScraper(origin);
        }
        if(origin.contains("github.com")){
            return new GitHubScraper(origin, branch, releases);
        } if(origin.contains("wowinterface.com")){
            return new WowInterfaceScraper(origin);
        }
        return null;
    }

    @Override
    public int compareTo(Addon o) {
        return name.compareTo(o.name);
    }


    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public String getOrigin(){
        return origin;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getLastFileName() {
        return lastFileName;
    }

    public boolean isReleases() {
        return releases;
    }

    public void setReleases(boolean releases) {
        this.releases = releases;
    }
}
