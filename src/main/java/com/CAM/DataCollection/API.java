package com.CAM.DataCollection;

import com.CAM.HelperTools.AddonSource;
import com.CAM.HelperTools.Log;
import com.CAM.HelperTools.Tools;
import com.gargoylesoftware.htmlunit.*;

import java.io.IOException;
import java.util.Date;

public abstract class API implements AddonInfoRetriever {

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

    @Override
    public abstract String getDownloadLink() throws DataCollectionException;

    @Override
    public abstract Date getLastUpdated() throws DataCollectionException;

    @Override
    public abstract String getName() throws DataCollectionException;

    @Override
    public abstract String getAuthor() throws DataCollectionException;

    @Override
    public abstract String getFileName() throws DataCollectionException;

    @Override
    public String getUrl(){
        return url;
    }

    public void setUrl(String url){
        this.url = url;
    }

    @Override
    public Page getFetchedPage() {
        return fetchedPage;
    }

    @Override
    public void setFetchedPage(Page fetchedPage){
        this.fetchedPage = fetchedPage;
    }

    public static String sanatizeInput(String input){
        return Tools.sanatizeInput(input);
    }

    @Override
    public abstract boolean isValidLink() throws DataCollectionException;

    @Override
    public AddonSource getAddonSource(){
        return source;
    }

    public Page jsonScrape(String url) throws DataCollectionException {
        WebClient client = new WebClient();
        client.getOptions().setJavaScriptEnabled(js);
        client.getOptions().setCssEnabled(css);
        client.getOptions().setUseInsecureSSL(insecureSSL);

        Page page;

        try {
            page = client.getPage(url);
        } catch (FailingHttpStatusCodeException e){
            Log.verbose("Scrape resulted in " + e.getStatusCode());
            throw new DataCollectionException(getAddonSource(), e);
        } catch (IOException e) {
            throw new DataCollectionException(getAddonSource(), e);
        }
        return page;
    }

    protected abstract boolean apiFound() throws DataCollectionException;

}
