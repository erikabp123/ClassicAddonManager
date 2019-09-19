package com.CAM.DataCollection;

import com.CAM.HelperTools.AddonSource;
import com.CAM.HelperTools.Log;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;
import java.util.Date;

public abstract class Scraper {

    private final String url;
    private final boolean js;
    private final boolean css;
    private final boolean insecureSSL;
    private HtmlPage scrapedPage;
    private String branch;
    private AddonSource source;

    public Scraper(String url, boolean js, boolean css, boolean insecureSSL, AddonSource source) throws ScrapeException {
        this.url = url;
        this.js = js;
        this.css = css;
        this.insecureSSL = insecureSSL;
        this.source = source;
        this.scrapedPage = scrape();
    }

    public Scraper(String url, String branch){
        //For use for non-scraping, aka when an API is available
        //TODO: Create a separate interface that this class implements, such that API dont use scraper class
        this.url = url;
        this.js = false;
        this.css = false;
        this.insecureSSL = false;
        this.scrapedPage = null;
        this.branch = branch;
    }

    public Scraper(String url){
        //For use for non-scraping, aka when an API is available
        //TODO: Create a separate interface that this class implements, such that API dont use scraper class
        this.url = url;
        this.js = false;
        this.css = false;
        this.insecureSSL = false;
        this.scrapedPage = null;
    }

    public HtmlPage scrape() throws ScrapeException {
        return scrape(url);
    }

    public HtmlPage scrape(String targetUrl) throws ScrapeException {
        WebClient client = new WebClient();
        client.getOptions().setJavaScriptEnabled(js);
        client.getOptions().setCssEnabled(css);
        client.getOptions().setUseInsecureSSL(insecureSSL);

        HtmlPage page;

        try {
            page = client.getPage(targetUrl);
        } catch (FailingHttpStatusCodeException e){
            Log.verbose("Scrape resulted in " + e.getStatusCode());

            switch (e.getStatusCode()){
                case 503:
                    return scrapeJS(targetUrl);
                default:
                    throw new ScrapeException(source, e);
            }
        } catch (IOException e) {
            throw new ScrapeException(source, e);
        }

        return page;
    }

    private HtmlPage scrapeJS(String targetUrl) throws ScrapeException {
        Log.verbose("Attempting JS scrape ...");
        WebClient client = new WebClient();
        client.getOptions().setJavaScriptEnabled(true);
        client.getOptions().setCssEnabled(css);
        client.getOptions().setUseInsecureSSL(insecureSSL);

        HtmlPage page;

        try {
            page = client.getPage(targetUrl);
            client.waitForBackgroundJavaScript(10_000);
        } catch (FailingHttpStatusCodeException e){
            Log.verbose("JS scrape resulted in " + e.getStatusCode());
            throw new ScrapeException(source, e);
        } catch (IOException e){
            throw new ScrapeException(source, e);
        }

        return page;
    }

    public abstract String getDownloadLink() throws ScrapeException;

    public abstract Date getLastUpdated() throws ScrapeException;

    public abstract String getName() throws ScrapeException;

    public abstract String getAuthor() throws ScrapeException;

    public abstract String getFileName() throws ScrapeException;

    public String getUrl(){
        return url;
    }

    public HtmlPage getScrapedPage() {
        return scrapedPage;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public static String sanatizeInput(String input){
        String sanatized = input;
        String[] illegals = {"?", "!", "\\", "/", ":", "*", "<", ">", "|"};
        for(int i=0; i<illegals.length; i++){
            sanatized = sanatized.replace(illegals[i], "");
        }
        return sanatized;
    }

    public abstract boolean isValidLink() throws ScrapeException;

    public abstract AddonSource getAddonSource();
}
