package com.CAM.DataCollection;

import com.CAM.HelperTools.Log;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;

public abstract class Scraper {

    private final String url;
    private final boolean js;
    private final boolean css;
    private final boolean insecureSSL;
    private HtmlPage scrapedPage;

    public Scraper(String url, boolean js, boolean css, boolean insecureSSL){
        this.url = url;
        this.js = js;
        this.css = css;
        this.insecureSSL = insecureSSL;
        this.scrapedPage = scrape();
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
            Log.log("Scrape resulted in " + e.getStatusCode());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return page;
    }

    public abstract String getDownloadLink();

    public abstract String getLastUpdated();

    public abstract String getName();

    public abstract String getAuthor();

    public abstract String getFileName();

    public String getUrl(){
        return url;
    }

    public HtmlPage getScrapedPage() {
        return scrapedPage;
    }
}
