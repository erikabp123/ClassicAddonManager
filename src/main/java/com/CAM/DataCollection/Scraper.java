package com.CAM.DataCollection;

import com.CAM.HelperTools.Log;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
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
    private int statuscode;
    private String branch;

    public Scraper(String url, boolean js, boolean css, boolean insecureSSL){
        this.url = url;
        this.js = js;
        this.css = css;
        this.insecureSSL = insecureSSL;
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

    public HtmlPage scrape(){
        WebClient client = new WebClient();
        client.getOptions().setJavaScriptEnabled(js);
        client.getOptions().setCssEnabled(css);
        client.getOptions().setUseInsecureSSL(insecureSSL);

        HtmlPage page = null;

        try {
            page = client.getPage(url);
        } catch (FailingHttpStatusCodeException e){
            Log.verbose("Scrape resulted in " + e.getStatusCode());
            statuscode = e.getStatusCode();
            if(statuscode == 503){
                return scrapeJS();
            }
            return null;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        statuscode = 200;
        return page;
    }

    private HtmlPage scrapeJS(){
        Log.verbose("Attempting JS scrape ...");
        WebClient client = new WebClient();
        client.getOptions().setJavaScriptEnabled(true);
        client.getOptions().setCssEnabled(css);
        client.getOptions().setUseInsecureSSL(insecureSSL);

        HtmlPage page = null;

        try {
            page = client.getPage(url);
            client.waitForBackgroundJavaScript(10_000);
        } catch (FailingHttpStatusCodeException e){
            Log.verbose("Scrape resulted in " + e.getStatusCode());
            statuscode = e.getStatusCode();
            return null;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        statuscode = 200;
        return page;
    }

    public int getStatuscode(){
        return statuscode;
    }

    public void setStatusCode(int statuscode){
        this.statuscode = statuscode;
    }

    public abstract String getDownloadLink();

    public abstract Date getLastUpdated();

    public abstract String getName();

    public abstract String getAuthor();

    public abstract String getFileName();

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
}
