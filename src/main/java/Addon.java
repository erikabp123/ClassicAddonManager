import DataCollection.CurseForgeScraper;
import HelperTools.DateConverter;
import DataCollection.FileDownloader;
import DataCollection.Scraper;
import java.util.Date;

public class Addon {
    private String name;
    private String author;
    private String version;
    private Date lastUpdated;
    private String origin;

    public Addon(String name, String author, String version, Date lastUpdated, String origin){
        this.name = name;
        this.author = author;
        this.version = version;
        this.lastUpdated = lastUpdated;
        this.origin = origin;
    }

    public boolean fetchUpdate(Scraper scraper){
        String downloadLink = scraper.getDownloadLink();
        FileDownloader downloader = new FileDownloader("downloads");
        downloader.downloadFile(downloadLink, name + "_" + author + ".zip");
        lastUpdated = DateConverter.convertFromCurse(scraper.getLastUpdated());
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




    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public String getVersion() {
        return version;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

}
