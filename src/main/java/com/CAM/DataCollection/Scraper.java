package com.CAM.DataCollection;

import com.CAM.HelperTools.AddonSource;
import com.CAM.HelperTools.Log;
import com.CAM.HelperTools.Tools;
import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;
import java.util.Date;

public abstract class Scraper implements AddonInfoRetriever {

    private final String url;
    private final boolean js;
    private final boolean css;
    private final boolean insecureSSL;
    private HtmlPage fetchedPage;
    private AddonSource source;

    public Scraper(String url, AddonSource source) throws ScrapeException {
        this.url = url;
        this.js = false;
        this.css = false;
        this.insecureSSL = true;
        this.source = source;
        this.fetchedPage = scrape(this.url);
    }

    public HtmlPage scrape(String targetUrl) throws ScrapeException {
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
                    throw new ScrapeException(getAddonSource(), e);
            }
        } catch (IOException e) {
            throw new ScrapeException(getAddonSource(), e);
        }

        return page;
    }

    private HtmlPage scrapeJS(String targetUrl) throws ScrapeException {
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
            throw new ScrapeException(getAddonSource(), e);
        } catch (IOException e){
            throw new ScrapeException(getAddonSource(), e);
        }

        return page;
    }

    @Override
    public abstract String getDownloadLink() throws ScrapeException;

    @Override
    public abstract Date getLastUpdated() throws ScrapeException;

    @Override
    public abstract String getName() throws ScrapeException;

    @Override
    public abstract String getAuthor() throws ScrapeException;

    @Override
    public abstract String getFileName() throws ScrapeException;

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
    public abstract boolean isValidLink() throws ScrapeException;

    @Override
    public AddonSource getAddonSource(){
        return source;
    }
}
