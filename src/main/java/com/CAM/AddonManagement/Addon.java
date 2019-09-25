package com.CAM.AddonManagement;

import com.CAM.DataCollection.*;
import com.CAM.HelperTools.AddonSource;
import com.CAM.HelperTools.DateConverter;
import com.CAM.HelperTools.Log;
import com.CAM.HelperTools.UrlInfo;

import java.util.Date;

public class Addon implements Comparable<Addon> {
    private String name;
    private String author;
    private String origin;
    private Date lastUpdated;
    private String lastFileName;
    private String branch;
    private boolean releases;
    private Date lastUpdateCheck;

    public Addon(String name, String author, String origin, String branch, boolean releases){
        this.name = name;
        this.author = author;
        this.origin = origin;
        this.branch = branch;
        this.releases = releases;
    }

    public Addon export(){
        return new Addon(name, author, origin, branch, releases);
    }

    public boolean fetchUpdate(Scraper scraper) throws ScrapeException {
        try {
            Log.verbose("Attempting to fetch update ...");
            String downloadLink = scraper.getDownloadLink();
            FileDownloader downloader = new FileDownloader("downloads");
            String fileName = name + "_" + author + "_(" + scraper.getFileName() + ").zip";
            downloader.downloadFileMonitored(downloadLink, fileName);
            lastUpdated = scraper.getLastUpdated();
            lastFileName = fileName;
            lastUpdateCheck = new Date();
        } catch (ScrapeException e){
            e.setAddon(this);
            throw e;
        } catch (Exception e) {
            ScrapeException exception = new ScrapeException(getAddonSource(), e);
            exception.setAddon(this);
            throw exception;
        }
        Log.verbose("Successfully fetched new update!");
        return true;
    }

    public UpdateResponse checkForUpdate() throws ScrapeException {
        UpdateResponse response;
        try {
            Scraper scraper = getScraper(true);
            response = new UpdateResponse(scraper, true);

            // Check if addon has ever been updated through this program
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
        } catch (ScrapeException e){
            e.setAddon(this);
            throw e;
        }

        return response;
    }

    private Scraper getScraper(boolean updatingAddon) throws ScrapeException {
        try {
           return UrlInfo.getCorrespondingScraper(getAddonSource(), origin, updatingAddon, branch, releases);
        } catch (ScrapeException e){
            e.setAddon(this);
            throw e;
        }
    }

    public AddonSource getAddonSource(){
        return UrlInfo.getAddonSource(origin);
    }

    @Override
    public int compareTo(Addon o) {
        return name.toLowerCase().compareTo(o.name.toLowerCase());
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

    public String getBranch(){
        return branch;
    }

    public void setBranch(String branch){
        this.branch = branch;
    }

    public void setOrigin(String origin){
        this.origin = origin;
    }

    public Date getLastUpdateCheck() {
        return lastUpdateCheck;
    }

    public void setLastUpdateCheck(Date lastUpdateCheck) {
        this.lastUpdateCheck = lastUpdateCheck;
    }
}
