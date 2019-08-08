import DataCollection.CurseForgeScraper;
import DataCollection.Scraper;
import com.gargoylesoftware.htmlunit.html.*;

import java.util.List;

public class Addon {
    private String name;
    private String author;
    private String version;

    public Addon(String name, String author, String version){
        this.name = name;
        this.author = author;
        this.version = version;
    }

    public boolean fetchUpdate(String addonUrl){
        Scraper scraper = new CurseForgeScraper(addonUrl);
        String downloadLink = scraper.getDownloadLink();
        System.out.println(downloadLink);
        return true;
    }


    public boolean checkForUpdate(){
        return false;
    }


    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public String getVersion() {
        return version;
    }
}
