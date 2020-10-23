package com.CAM.DataCollection.Github;

import com.CAM.DataCollection.API;
import com.CAM.DataCollection.DataCollectionException;
import com.CAM.HelperTools.AddonSource;
import com.CAM.HelperTools.DateConverter;
import com.CAM.HelperTools.Log;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Date;

public class GitHubAPI extends API {

    private String branch;
    private boolean releases;
    private JsonArray repoArray;

    public GitHubAPI(String url, String branch, boolean releases, boolean updatingAddon) throws DataCollectionException {
        super(url, AddonSource.GITHUB);
        this.branch = branch;
        this.releases = releases;
        this.repoArray = null;
        if(!updatingAddon && !isValidLink()){
            throw new DataCollectionException(getAddonSource(), "Invalid Github URL!");
        }
    }

    @Override
    public String getDownloadLink() throws DataCollectionException {
        if(releases){
            return getReleasesDownload();
        }
        return getBranchDownload();
    }

    private String getReleasesDownload() throws DataCollectionException {
        JsonArray jsonArray = getRepoArray();
        String downloadLink;
        try {
            downloadLink = ((JsonObject) ((JsonArray) ((JsonObject) jsonArray.get(0)).get("assets")).get(0)).get("browser_download_url").getAsString();
        } catch (IndexOutOfBoundsException e){
            throw  new DataCollectionException(getAddonSource(), e);
        }

        return downloadLink;
    }

    private String getBranchDownload(){
        String downloadSuffix = "/archive/" + getBranch() + ".zip";
        return getUrl() + downloadSuffix;
    }

    @Override
    public Date getLastUpdated() throws DataCollectionException {
        if(releases){
            return getReleasesUpdated();
        }
        return getBranchUpdated();
    }

    private Date getReleasesUpdated() throws DataCollectionException {
        JsonArray jsonArray = getRepoArray();
        String githubDate = ((JsonObject) jsonArray.get(0)).get("published_at").getAsString();
        Date date = DateConverter.convertFromGithub(githubDate);
        return date;
    }

    private Date getBranchUpdated() throws DataCollectionException {
        String[] repoInfo = getUrl().split("/");
        String prefix = "https://api.github.com/repos/";
        String suffix = "/branches/" + branch;
        String url = prefix + repoInfo[3] + "/" + repoInfo[4] + suffix;
        JsonObject jsonObject = getJsonObject(fetchJson(url));
        String githubDate = jsonObject.getAsJsonObject("commit").getAsJsonObject("commit").getAsJsonObject("author").get("date").toString();
        Date date = DateConverter.convertFromGithub(githubDate);
        return date;
    }

    private JsonArray getJsonArray(Page page){
        WebResponse webResponse = page.getWebResponse();
        String json = webResponse.getContentAsString();
        return new Gson().fromJson(json, JsonArray.class);
    }

    private JsonObject getJsonObject(Page page){
        WebResponse webResponse = page.getWebResponse();
        String json = webResponse.getContentAsString();
        return new Gson().fromJson(json, JsonObject.class);
    }

    public String getTag() throws DataCollectionException {
        JsonArray jsonArray = getRepoArray();
        String tag = ((JsonObject) jsonArray.get(0)).get("tag_name").getAsString();
        return tag;
    }

    public JsonArray getRepoArray() throws DataCollectionException {
        if(repoArray != null){
            return repoArray;
        }
        String[] repoInfo = getUrl().split("/");
        String prefix = "https://api.github.com/repos/";
        String suffix = "/releases";
        String url = prefix + repoInfo[3] + "/" + repoInfo[4] + suffix;
        repoArray = getJsonArray(fetchJson(url));
        return repoArray;
    }

    @Override
    public String getName() {
        String name = getUrl().split("/")[4];
        return name;
    }

    @Override
    public String getAuthor() {
        String author = getUrl().split("/")[3];
        return author;
    }

    @Override
    public String getFileName() {
        String fileName = getName() + "-" + getBranch();
        return fileName;
    }

    public static ArrayList<String> getBranches(String repo) throws DataCollectionException {
        ArrayList<String> branches = new ArrayList<>();
        GitHubAPI scraper = new GitHubAPI(repo, null, false, false);
        String author = scraper.getAuthor();
        String name = scraper.getName();
        String branchesApi = "https://api.github.com/repos/" + author + "/" + name + "/branches";
        JsonArray array = scraper.getJsonArray(scraper.fetchJson(branchesApi));
        for(JsonElement branch : array){
            branches.add(((JsonObject) branch).get("name").getAsString());
        }
        return branches;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    @Override
    public boolean isValidLink() throws DataCollectionException {
        String[] parts = getUrl().split("/");
        if(parts.length < 5){
            return false;
        }
        if(!apiFound()){
            return false;
        }
        if(releases && getRepoArray().size() == 0){
            Log.log("The provided link does not have any releases!");
            throw new DataCollectionException(getAddonSource(), "The provided link does not have any releases!");
        }
        return true;
    }

    @Override
    protected boolean apiFound() throws DataCollectionException {
        String suffix = releases
                ? "releases"
                : "branches";
        String author = getAuthor();
        String name = getName();
        String api = "https://api.github.com/repos/" + author + "/" + name + "/" + suffix;
        Page response = fetchJson(api);
        if(response == null){
            return false;
        }
        return true;
    }

    public boolean isReleases() {
        return releases;
    }

    public void setReleases(boolean releases) {
        this.releases = releases;
    }
}
