package DataCollection;

import HelperTools.Log;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlHeading2;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;

import java.io.IOException;

public class Scraper {

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
        } catch (IOException e) {
            e.printStackTrace();
        }
        return page;
    }

    public String getDownloadLink(){
        // Child should implement concrete solution
        return null;
    }

    public String getLastUpdated(){
        // Child should implement concrete solution
        return null;
    }

    public String getName(){
        return null;
    }

    public String getAuthor(){
        return null;
    }

    public String getFileName(){
        return null;
    }

    public String getUrl(){
        return url;
    }

    public HtmlPage getScrapedPage() {
        return scrapedPage;
    }
}
