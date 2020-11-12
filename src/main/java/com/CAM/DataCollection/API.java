package com.CAM.DataCollection;

import com.CAM.HelperTools.GameSpecific.AddonSource;
import com.gargoylesoftware.htmlunit.*;

import java.util.Date;

public abstract class API extends PageFetcher {

    private String url;
    private final boolean js;
    private final boolean css;
    private final boolean insecureSSL;
    private Page fetchedPage;
    private AddonSource source;

    public API(String url, AddonSource source) {
        this.url = url;
        this.js = false;
        this.css = false;
        this.insecureSSL = false;
        this.source = source;
        this.fetchedPage = null;
    }

    public abstract String getDownloadLink() throws DataCollectionException;

    public abstract Date getLastUpdated() throws DataCollectionException;

    public abstract String getName() throws DataCollectionException;

    public abstract String getAuthor() throws DataCollectionException;

    public abstract String getFileName() throws DataCollectionException;

    public String getUrl(){
        return url;
    }

    public void setUrl(String url){
        this.url = url;
    }

    public Page getFetchedPage() {
        return fetchedPage;
    }

    public void setFetchedPage(Page fetchedPage){
        this.fetchedPage = fetchedPage;
    }

    public abstract boolean isValidLink() throws DataCollectionException;

    @Override
    public AddonSource getAddonSource(){
        return source;
    }

    protected abstract boolean apiFound() throws DataCollectionException;

}
