package com.CAM.DataCollection.Github;

import com.CAM.DataCollection.API;
import com.CAM.DataCollection.DataCollectionException;
import com.CAM.HelperTools.GameSpecific.AddonSource;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.CAM.HelperTools.DateConverter.convertFromGithub;
import static com.CAM.HelperTools.Logging.Log.log;
import static java.text.MessageFormat.format;

public class GitHubAPI extends API {

    private String textToMatch;
    private String branch;
    private boolean releases;
    private JsonArray repoArray;

    public GitHubAPI(String url, String branch, boolean releases, boolean updatingAddon, String textToMatch) throws DataCollectionException {
        super(url, AddonSource.GITHUB);
        this.branch = branch;
        this.releases = releases;
        this.repoArray = null;
        if (textToMatch != null) {
            this.textToMatch = textToMatch.toLowerCase();
        }
        if (!updatingAddon && !isValidLink()) {
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
            JsonObject newestReleaseContainingText = getLatestReleaseContainingText(jsonArray);

            boolean noAssets = ((JsonArray) (newestReleaseContainingText).get("assets")).size() == 0;
            if (noAssets) {
                downloadLink = (newestReleaseContainingText).get("zipball_url").getAsString();
            } else {
                downloadLink = ((JsonObject) ((JsonArray) (newestReleaseContainingText).get("assets")).get(0)).get("browser_download_url").getAsString();
            }
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
        if (releases) {
            return getReleasesUpdated();
        }
        return getBranchUpdated();
    }

    private Date getReleasesUpdated() throws DataCollectionException {
        JsonArray jsonArray = getRepoArray();
        JsonObject newestReleaseContainingText = getLatestReleaseContainingText(jsonArray);
        String githubDate = newestReleaseContainingText.get("published_at").getAsString();
        return convertFromGithub(githubDate);
    }

    private JsonObject getLatestReleaseContainingText(JsonArray jsonArray) {
        if (textToMatch == null) return (JsonObject) jsonArray.get(0);

        List<JsonObject> matchesTags = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonObject = (JsonObject) jsonArray.get(i);
            boolean matchesName = jsonObject.get("name").getAsString().toLowerCase().contains(textToMatch);
            if (matchesName) {
                return jsonObject;
            }

            boolean matchesTag = jsonObject.get("name").getAsString().toLowerCase().contains(textToMatch);
            if (matchesTag) {
                matchesTags.add(jsonObject);
            }
        }
        if (!matchesTags.isEmpty()) {
            return matchesTags.get(0);
        }
        log(format("Unable to find any releases containing text {0} in release name or release tag, falling back to latest release!", textToMatch));
        return (JsonObject) jsonArray.get(0);
    }

    private Date getBranchUpdated() throws DataCollectionException {
        String[] repoInfo = getUrl().split("/");
        String prefix = "https://api.github.com/repos/";
        String suffix = "/branches/" + branch;
        String url = prefix + repoInfo[3] + "/" + repoInfo[4] + suffix;
        JsonObject jsonObject = getJsonObject(fetchJson(url));
        String githubDate = jsonObject.getAsJsonObject("commit").getAsJsonObject("commit").getAsJsonObject("author").get("date").toString();
        return convertFromGithub(githubDate);
    }

    private JsonArray getJsonArray(String json){
        return new Gson().fromJson(json, JsonArray.class);
    }

    private JsonObject getJsonObject(String json){
        return new Gson().fromJson(json, JsonObject.class);
    }

    public String getTag() throws DataCollectionException {
        JsonArray jsonArray = getRepoArray();
        return ((JsonObject) jsonArray.get(0)).get("tag_name").getAsString();
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
        return getUrl().split("/")[4];
    }

    @Override
    public String getAuthor() {
        return getUrl().split("/")[3];
    }

    @Override
    public String getFileName() {
        String suffix = isReleases() ? "release" : getBranch();
        return getName() + "-" + suffix;
    }

    public static ArrayList<String> getBranches(String repo) throws DataCollectionException {
        ArrayList<String> branches = new ArrayList<>();
        GitHubAPI scraper = new GitHubAPI(repo, null, false, false, null);
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
            log("The provided link does not have any releases!");
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
        String response = fetchJson(api);
        return response != null;
    }

    public boolean isReleases() {
        return releases;
    }

    public void setReleases(boolean releases) {
        this.releases = releases;
    }
}
