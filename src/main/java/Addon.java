import DataCollection.CurseForgeScraper;
import HelperTools.DateConverter;
import DataCollection.FileDownloader;
import DataCollection.Scraper;
import HelperTools.Log;

import java.util.Date;

public class Addon implements Comparable<Addon> {
    private String name;
    private String author;
    private String origin;
    private Date lastUpdated;
    private String lastFileName;

    public Addon(String name, String author, String origin, Date lastUpdated){
        this.name = name;
        this.author = author;
        this.origin = origin;
        this.lastUpdated = lastUpdated;
    }

    public Addon(String name, String author, String origin){
        this.name = name;
        this.author = author;
        this.origin = origin;
    }

    public boolean fetchUpdate(Scraper scraper){
        //TODO: Consider tracking folders installed so that deleting is easier
        Log.verbose("Attempting to fetch update ...");
        String downloadLink = scraper.getDownloadLink();
        FileDownloader downloader = new FileDownloader("downloads");
        String fileName = name + "_" + author + "_(" +scraper.getFileName() + ").zip";
        downloader.downloadFile(downloadLink, fileName);
        lastUpdated = DateConverter.convertFromCurse(scraper.getLastUpdated());
        lastFileName = fileName;
        Log.verbose("Successfully fetched new update!");
        return true;
    }

    public UpdateResponse checkForUpdate(){
        Scraper scraper = new CurseForgeScraper(origin);
        UpdateResponse response = new UpdateResponse(scraper, true);

        // Check if addon has ever been updated through this program
        //TODO: consider whether redundant, could be useful later for determining if addon was installed manually though
        if(lastUpdated == null){
            return response;
        }
        // Get the date of the last update as seen by scrape
        Date lastUpdateScrape = DateConverter.convertFromCurse(scraper.getLastUpdated());
        // Check if scrape has seen a newer update
        if(DateConverter.isNewerDate(lastUpdateScrape, lastUpdated)){
            return response;
        }
        // There are no new updates
        response.setUpdateAvailable(false);
        return response;
    }

    @Override
    public int compareTo(Addon o) {
        return name.compareTo(o.name);
    }


    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public String getOrigin(){
        return origin;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getLastFileName() {
        return lastFileName;
    }
}
