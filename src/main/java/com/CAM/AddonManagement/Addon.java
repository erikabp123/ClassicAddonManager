package com.CAM.AddonManagement;

import com.CAM.DataCollection.*;
import com.CAM.DataCollection.Tukui.TukuiAPI;
import com.CAM.DataCollection.TwitchOwned.CurseForge.CurseAddonReponse.CurseAddonResponse;
import com.CAM.DataCollection.TwitchOwned.CurseForge.CurseForgeAPI;
import com.CAM.DataCollection.TwitchOwned.CurseForge.CurseForgeAPISearcher;
import com.CAM.HelperTools.*;

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
    private int projectId;

    public Addon(String name, String author, String origin, String branch, boolean releases){
        this.name = name;
        this.author = author;
        this.origin = origin;
        this.branch = branch;
        this.releases = releases;
    }

    public Addon(String name, String author, String origin, int projectId){
        this.name = name;
        this.author = author;
        this.origin = origin;
        this.projectId = projectId;
    }

    public Addon export(){
        return new Addon(name, author, origin, branch, releases);
    }

    public boolean fetchUpdate(AddonInfoRetriever retriever) throws ScrapeException {
        try {
            Log.verbose("Attempting to fetch update ...");
            String downloadLink = retriever.getDownloadLink();
            FileDownloader downloader = new FileDownloader("downloads");
            String fileName = name + "_" + author + "_(" + retriever.getFileName() + ").zip";
            downloader.downloadFileMonitored(downloadLink, fileName);
            lastUpdated = retriever.getLastUpdated();
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

    public UpdateResponse checkForUpdate(GameVersion gameVersion) throws ScrapeException {
        UpdateResponse response;
        try {
            AddonInfoRetriever retriever = getInfoRetriever(true, gameVersion);
            response = new UpdateResponse(retriever, true);

            // Check if addon has ever been updated through this program
            if(lastUpdated == null){
                return response;
            }
            // Get the date of the last update as seen by scrape
            Date lastUpdateScrape = retriever.getLastUpdated();
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

    private AddonInfoRetriever getInfoRetriever(boolean updatingAddon, GameVersion gameVersion) throws ScrapeException {
        try {
           return UrlInfo.getCorrespondingInfoRetriever(gameVersion, getAddonSource(), origin, updatingAddon, branch, releases, projectId);
        } catch (ScrapeException e){
            e.setAddon(this);
            throw e;
        }
    }

    public AddonSource getAddonSource(){
        return UrlInfo.getAddonSource(origin);
    }

    public boolean updateToLatestFormat() throws ScrapeException {
        if(getAddonSource() != AddonSource.CURSEFORGE
                && getAddonSource() != AddonSource.TUKUI){
            return false;
        }
        if(projectId > 0){
            return false;
        }

        int projectId = 0;
        if(getAddonSource() == AddonSource.CURSEFORGE){
            CurseForgeAPISearcher searcher = new CurseForgeAPISearcher();
            CurseAddonResponse response = searcher.findCorrespondingAddon(this);
            if(response == null){
                System.out.println("request user input");
            }
            projectId = response.id;
        } else if(getAddonSource() == AddonSource.TUKUI){
            projectId = TukuiAPI.extractAddonNumber(this.origin);
        }

        setProjectId(projectId);
        return true;
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

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }
}
