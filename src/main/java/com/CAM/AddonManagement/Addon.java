package com.CAM.AddonManagement;

import com.CAM.DataCollection.API;
import com.CAM.DataCollection.DataCollectionException;
import com.CAM.DataCollection.FileDownloader;
import com.CAM.DataCollection.Tukui.TukuiAPI;
import com.CAM.DataCollection.TwitchOwned.CurseForge.CurseAddonReponse.CurseAddonResponse;
import com.CAM.DataCollection.TwitchOwned.CurseForge.CurseForgeAPISearcher;
import com.CAM.DataCollection.WowInterface.WowInterfaceAPI;
import com.CAM.HelperTools.GameSpecific.AddonSource;
import com.CAM.HelperTools.GameSpecific.GameVersion;
import com.CAM.HelperTools.Logging.Log;
import com.CAM.HelperTools.TableViewStatus;
import com.CAM.HelperTools.UrlInfo;
import javafx.scene.image.Image;

import java.util.Date;

public class Addon implements Comparable<Addon> {
    public static final Date NO_FILE_PLACEHOLDER_DATE = new Date(0);
    private final String name;
    private final String author;
    private String origin;
    private Date lastUpdated;
    private String lastFileName;
    private String branch;
    private String textToMatch;
    private boolean releases;
    private Date lastUpdateCheck;
    private int projectId;

    public Addon(String name, String author, String origin, String branch, boolean releases, String textToMatch) {
        this.name = name;
        this.author = author;
        this.origin = origin;
        this.branch = branch;
        this.releases = releases;
        this.textToMatch = textToMatch;
    }

    public Addon(String name, String author, String origin, int projectId) {
        this.name = name;
        this.author = author;
        this.origin = origin;
        this.projectId = projectId;
    }

    public Addon(String name, String author, String origin, String branch, boolean releases, int projectId) {
        this.name = name;
        this.author = author;
        this.origin = origin;
        this.branch = branch;
        this.releases = releases;
        this.projectId = projectId;
    }

    public Addon export() {
        return new Addon(name, author, origin, branch, releases, projectId);
    }

    public void fetchUpdate(API api, TableViewStatus tableViewStatus) throws DataCollectionException {
        try {
            Log.verbose("Attempting to fetch update ...");
            FileDownloader downloader = new FileDownloader("downloads");
            downloader.listenLocal(tableViewStatus);
            String fileName = api.getFileName();

            if (api.getAddonSource() == AddonSource.GITHUB) {
                downloader.downloadFileMonitored(api.getDownloadLink(), fileName, 3);
            } else {
                downloader.googleDownloadFile(api.getDownloadLink(), fileName, tableViewStatus);
            }

            lastUpdated = api.getLastUpdated();
            lastFileName = fileName;
            lastUpdateCheck = new Date();
        } catch (DataCollectionException e){
            e.setAddon(this);
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            DataCollectionException exception = new DataCollectionException(getAddonSource(), e);
            exception.setAddon(this);
            throw exception;
        }
        Log.verbose("Successfully fetched new update!");
    }

    public UpdateResponse checkForUpdate(GameVersion gameVersion) throws DataCollectionException {
        UpdateResponse response;
        try {
            API api = getAPI(true, gameVersion);
            response = new UpdateResponse(api, true);

            // Get the date of the last update as seen by API
            Date lastUpdateCheck = api.getLastUpdated();

            if (lastUpdateCheck.equals(NO_FILE_PLACEHOLDER_DATE)) {
                response.setUpdateAvailable(false);
                return response;
            }

            // Check if has seen a newer update
            if (lastUpdated == null || lastUpdateCheck.after(lastUpdated)) {
                return response;
            }

            // There are no new updates
            response.setUpdateAvailable(false);
        } catch (DataCollectionException e){
            e.setAddon(this);
            throw e;
        }

        return response;
    }

    public API getAPI(boolean updatingAddon, GameVersion gameVersion) throws DataCollectionException {
        try {
            return UrlInfo.getCorrespondingAPI(gameVersion, getAddonSource(), origin, updatingAddon, branch, releases, projectId, textToMatch);
        } catch (DataCollectionException e){
            e.setAddon(this);
            throw e;
        }
    }

    public AddonSource getAddonSource(){
        return UrlInfo.getAddonSource(origin);
    }

    public boolean updateToLatestFormat() throws DataCollectionException {
        if(getAddonSource() != AddonSource.CURSEFORGE
                && getAddonSource() != AddonSource.TUKUI && getAddonSource() != AddonSource.WOWINTERFACE){
            return false;
        }
        if(projectId > 0){
            return false;
        }

        int projectId;

        switch (getAddonSource()){
            case CURSEFORGE:
                CurseForgeAPISearcher searcher = new CurseForgeAPISearcher();
                CurseAddonResponse response = searcher.findCorrespondingAddon(this);
                if(response == null){
                    System.out.println("request user input");
                }
                System.out.println(this);
                projectId = response.id;
                break;
            case TUKUI:
                projectId = TukuiAPI.extractAddonNumber(this.origin);
                break;
            case WOWINTERFACE:
                projectId = WowInterfaceAPI.extractAddonNumber(this.origin);
                break;
            default:
                throw new IllegalArgumentException("Invalid addon source!");
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

    public String getFlavor(){
        if(isReleases()) return "Release";
        if(getBranch() != null) return getBranch();
        return "Project ID: " + getProjectId();
    }

    public Image getWebsiteIcon(){
        return getAddonSource().getWebsiteIcon();
    }

    @Override
    public String toString(){
        return getName() + " by " + getAuthor();
    }
}
