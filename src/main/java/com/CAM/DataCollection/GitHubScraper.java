package com.CAM.DataCollection;

import com.CAM.HelperTools.DateConverter;
import com.CAM.HelperTools.Log;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import netscape.javascript.JSObject;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

public class GitHubScraper extends Scraper {

    private String branch;
    private boolean releases;

    public GitHubScraper(String url, String branch, boolean releases){
        super(url, branch);
        this.branch = branch;
        this.releases = releases;
        setStatusCode(200); //Dirty implementation, consider fixing
    }

    @Override
    public String getDownloadLink() {
        if(releases){
            return getReleasesDownload();
        }
        return getBranchDownload();
    }

    private String getReleasesDownload(){
        String[] repoInfo = getUrl().split("/");
        String prefix = "https://api.github.com/repos/";
        String suffix = "/releases";
        String url = prefix + repoInfo[3] + "/" + repoInfo[4] + suffix;
        JsonArray jsonArray = getJsonArray(jsonScrape(url));
        String downloadLink = ((JsonObject) ((JsonArray) ((JsonObject) jsonArray.get(0)).get("assets")).get(0)).get("browser_download_url").getAsString();
        return downloadLink;
    }

    private String getBranchDownload(){
        String downloadSuffix = "/archive/" + getBranch() + ".zip";
        return getUrl() + downloadSuffix;
    }

    public Page jsonScrape(String url){
        WebClient client = new WebClient();
        client.getOptions().setJavaScriptEnabled(false);
        client.getOptions().setCssEnabled(false);
        client.getOptions().setUseInsecureSSL(true);

        Page page = null;

        try {
            page = client.getPage(url);
        } catch (FailingHttpStatusCodeException e){
            Log.verbose("Scrape resulted in " + e.getStatusCode());
            setStatusCode(e.getStatusCode());
            return null;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return page;
    }

    @Override
    public Date getLastUpdated() {
        if(releases){
            return getReleasesUpdated();
        }
        return getBranchUpdated();
    }

    private Date getReleasesUpdated(){
        String[] repoInfo = getUrl().split("/");
        String prefix = "https://api.github.com/repos/";
        String suffix = "/releases";
        String url = prefix + repoInfo[3] + "/" + repoInfo[4] + suffix;
        JsonArray jsonArray = getJsonArray(jsonScrape(url));
        String githubDate = ((JsonObject) jsonArray.get(0)).get("published_at").getAsString();
        Date date = DateConverter.convertFromGithub(githubDate);
        return date;
    }

    private Date getBranchUpdated(){
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

    @Override
    public String getBranch() {
        return branch;
    }

    @Override
    public void setBranch(String branch) {
        this.branch = branch;
    }

    public boolean isReleases() {
        return releases;
    }

    public void setReleases(boolean releases) {
        this.releases = releases;
    }
}
