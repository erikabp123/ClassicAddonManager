package com.CAM.DataCollection;

import com.CAM.HelperTools.GameSpecific.AddonSource;
import com.CAM.HelperTools.Logging.Log;
import com.CAM.HelperTools.Tools;
import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;
import java.util.Date;

@Deprecated
public abstract class Scraper implements AddonInfoRetriever {

    private final String url;
    private final boolean js;
    private final boolean css;
    private final boolean insecureSSL;
    private HtmlPage fetchedPage;
    private AddonSource source;

    public Scraper(String url, AddonSource source) throws DataCollectionException {
        this.url = url;
        this.js = false;
        this.css = false;
        this.insecureSSL = false;
        this.source = source;
        this.fetchedPage = scrape(this.url);
    }

    public HtmlPage scrape(String targetUrl) throws DataCollectionException {
        WebClient client = new WebClient(BrowserVersion.CHROME);
        client.getOptions().setJavaScriptEnabled(js);
        client.getOptions().setCssEnabled(css);
        client.getOptions().setUseInsecureSSL(insecureSSL);
        client.setCookieManager(new CookieManager());
        client.getOptions().setRedirectEnabled(true);

        HtmlPage page;

        try {
            page = client.getPage(targetUrl);
        } catch (FailingHttpStatusCodeException e){
            Log.verbose("Scrape resulted in " + e.getStatusCode());

            switch (e.getStatusCode()){
                case 503:
                    return scrapeJS(targetUrl);
                default:
                    throw new DataCollectionException(getAddonSource(), e);
            }
        } catch (IOException e) {
            throw new DataCollectionException(getAddonSource(), e);
        }

        return page;
    }

    private HtmlPage scrapeJS(String targetUrl) throws DataCollectionException {
        Log.verbose("Attempting JS scrape ...");
        WebClient client = new WebClient(BrowserVersion.CHROME);
        client.getOptions().setJavaScriptEnabled(true);
        client.getOptions().setCssEnabled(css);
        client.getOptions().setUseInsecureSSL(insecureSSL);
        client.setCookieManager(new CookieManager());
        client.getOptions().setRedirectEnabled(true);

        HtmlPage page;

        try {
            page = client.getPage(targetUrl);
            client.waitForBackgroundJavaScript(10000);
        } catch (FailingHttpStatusCodeException e){
            Log.verbose("JS scrape resulted in " + e.getStatusCode());
            throw new DataCollectionException(getAddonSource(), e);
        } catch (IOException e){
            throw new DataCollectionException(getAddonSource(), e);
        }

        return page;
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

    @Override
    public HtmlPage getFetchedPage() {
        return fetchedPage;
    }

    @Override
    public void setFetchedPage(Page fetchedPage){
        if(!fetchedPage.isHtmlPage()){
            return;
        }
        this.fetchedPage = (HtmlPage) fetchedPage;
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
}
