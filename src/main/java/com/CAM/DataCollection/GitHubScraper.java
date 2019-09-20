package com.CAM.DataCollection;

import com.CAM.HelperTools.AddonSource;
import com.CAM.HelperTools.DateConverter;
import com.CAM.HelperTools.Log;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class GitHubScraper extends Scraper {

    private String branch;
    private boolean releases;
    private JsonArray repoArray;

    public GitHubScraper(String url, String branch, boolean releases, boolean updatingAddon) throws ScrapeException {
        super(url, branch);
        this.branch = branch;
        this.releases = releases;
        this.repoArray = null;
        if(!updatingAddon && !isValidLink()){
            throw new ScrapeException(getAddonSource(), "Invalid Github URL!");
        }
    }

    @Override
    public String getDownloadLink() throws ScrapeException {
        if(releases){
            return getReleasesDownload();
        }
        return getBranchDownload();
    }

    private String getReleasesDownload() throws ScrapeException {
        JsonArray jsonArray = getRepoArray();
        String downloadLink;
        try {
            downloadLink = ((JsonObject) ((JsonArray) ((JsonObject) jsonArray.get(0)).get("assets")).get(0)).get("browser_download_url").getAsString();
        } catch (IndexOutOfBoundsException e){
            throw  new ScrapeException(getAddonSource(), e);
        }

        return downloadLink;
    }

    private String getBranchDownload(){
        String downloadSuffix = "/archive/" + getBranch() + ".zip";
        return getUrl() + downloadSuffix;
    }

    public Page jsonScrape(String url) throws ScrapeException {
        WebClient client = new WebClient();
        client.getOptions().setJavaScriptEnabled(false);
        client.getOptions().setCssEnabled(false);
        client.getOptions().setUseInsecureSSL(true);

        Page page = null;

        try {
            page = client.getPage(url);
        } catch (FailingHttpStatusCodeException e){
            Log.verbose("Scrape resulted in " + e.getStatusCode());
            throw new ScrapeException(getAddonSource(), e);
        } catch (IOException e) {
            throw new ScrapeException(getAddonSource(), e);
        }
        return page;
    }

    @Override
    public Date getLastUpdated() throws ScrapeException {
        if(releases){
            return getReleasesUpdated();
        }
        return getBranchUpdated();
    }

    private Date getReleasesUpdated() throws ScrapeException {
        JsonArray jsonArray = getRepoArray();
        String githubDate = ((JsonObject) jsonArray.get(0)).get("published_at").getAsString();
        Date date = DateConverter.convertFromGithub(githubDate);
        return date;
    }

    private Date getBranchUpdated() throws ScrapeException{
        String[] repoInfo = getUrl().split("/");
        String prefix = "https://api.github.com/repos/";
        String suffix = "/branches/" + branch;
        String url = prefix + repoInfo[3] + "/" + repoInfo[4] + suffix;
        JsonObject jsonObject = getJsonObject(jsonScrape(url));
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

    public String getTag() throws ScrapeException {
        JsonArray jsonArray = getRepoArray();
        String tag = ((JsonObject) jsonArray.get(0)).get("tag_name").getAsString();
        return tag;
    }

    public String getUpdateManifestLink() throws ScrapeException {
        JsonArray jsonArray = getRepoArray();
        JsonArray assets = ((JsonArray) ((JsonObject) jsonArray.get(0)).get("assets"));
        int assetIndex = 0;
        for(int i=0; i<assets.size(); i++){
            String fileName = ((JsonObject) assets.get(i)).get("name").getAsString();
            if(!fileName.equals("VERSIONING")){
                continue;
            }
            assetIndex = i;
            break;
        }
        String downloadLink = ((JsonObject) ((JsonArray) ((JsonObject) jsonArray.get(0)).get("assets")).get(assetIndex)).get("browser_download_url").getAsString();
        return downloadLink;
    }

    public JsonArray getRepoArray() throws ScrapeException {
        if(repoArray != null){
            return repoArray;
        }
        String[] repoInfo = getUrl().split("/");
        String prefix = "https://api.github.com/repos/";
        String suffix = "/releases";
        String url = prefix + repoInfo[3] + "/" + repoInfo[4] + suffix;
        repoArray = getJsonArray(jsonScrape(url));
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

    public static ArrayList<String> getBranches(String repo) throws ScrapeException {
        ArrayList<String> branches = new ArrayList<>();
        GitHubScraper scraper = new GitHubScraper(repo, null, false, false);
        String author = scraper.getAuthor();
        String name = scraper.getName();
        String branchesApi = "https://api.github.com/repos/" + author + "/" + name + "/branches";
        JsonArray array = scraper.getJsonArray(scraper.jsonScrape(branchesApi));
        for(JsonElement branch : array){
            branches.add(((JsonObject) branch).get("name").getAsString());
        }
        return branches;
    }

    @Override
    public String getBranch() {
        return branch;
    }

    @Override
    public void setBranch(String branch) {
        this.branch = branch;
    }

    @Override
    public boolean isValidLink() throws ScrapeException {
        String[] parts = getUrl().split("/");
        if(parts.length < 5){
            return false;
        }
        if(releases){
            if(!apiFound("releases")){
                return false;
            }
            if(getRepoArray().size() == 0){
                Log.log("The provided link does not have any releases!");
                throw new ScrapeException(getAddonSource(), "The provided link does not have any releases!");
            }
            return true;
        }
        if(!apiFound("branches")){
            return false;
        }
        return true;
    }

    @Override
    public AddonSource getAddonSource() {
        return AddonSource.GITHUB;
    }

    private boolean apiFound(String suffix) throws ScrapeException {
        String author = getAuthor();
        String name = getName();
        String api = "https://api.github.com/repos/" + author + "/" + name + "/" + suffix;
        Page response = jsonScrape(api);
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
