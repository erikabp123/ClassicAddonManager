package com.CAM.DataCollection.TwitchOwned.WowAce;

import com.CAM.DataCollection.DataCollectionException;
import com.CAM.DataCollection.Scraper;
import com.CAM.DataCollection.TwitchOwned.TwitchSite;
import com.CAM.HelperTools.GameSpecific.AddonSource;
import com.CAM.HelperTools.DateConverter;
import com.CAM.HelperTools.Logging.Log;
import com.gargoylesoftware.htmlunit.html.*;

import java.util.Date;
import java.util.List;

@Deprecated
public class WowAceScraper extends Scraper implements TwitchSite {

    private final static String gameVersion = "1738749986%3A67408"; //TODO: change to non-final and support automatic scraping of gameVersion in case it changes
    private final static String officialSuffix = "/files?filter-game-version=" + gameVersion;
    private final static String nonOfficialSuffix = "/files/all";
    private final static String websiteUrl = "https://www.wowace.com/";

    private String baseUrl;
    private HtmlPage scrapedOverviewPage;

    public static WowAceScraper makeScraper(String url, boolean updatingAddon) throws DataCollectionException {
        WowAceScraper scraper = getOfficialScraper(url, updatingAddon);
        if(scraper.isGameVersionSupported()){
            return scraper;
        }
        return getNonOfficialScraper(url, updatingAddon);
    }

    public static WowAceScraper getNonOfficialScraper(String url, boolean updatingAddon) throws DataCollectionException {
        return new WowAceScraper(url, nonOfficialSuffix, updatingAddon);
    }

    public static WowAceScraper getOfficialScraper(String url, boolean updatingAddon) throws DataCollectionException {
        return new WowAceScraper(url, officialSuffix, updatingAddon);
    }

    public WowAceScraper(String url, String suffix, boolean updatingAddon) throws DataCollectionException {
        super(url + suffix, AddonSource.WOWACE);
        this.baseUrl = url;
        if(!updatingAddon && !isValidLink()){
            throw new DataCollectionException(getAddonSource(), "Invalid WowAce url!");
        }
    }

    @Override
    public String getDownloadLink(){
        HtmlPage page = getFetchedPage();

        HtmlTableRow row = findFirstDownloadRow(page);
        HtmlAnchor downloadAnchor = findDownloadAnchor(row);
        String downloadSuffix = downloadAnchor.getAttribute("href");

        return websiteUrl + downloadSuffix;
    }

    @Override
    public Date getLastUpdated(){
        HtmlPage page = getFetchedPage();
        HtmlTableRow row = findFirstDownloadRow(page);
        Date date = DateConverter.convertFromWowAce(findDateAbbr(row).getAttribute("title"));
        return date;
    }

    @Override
    public String getName(){
        Log.verbose("Fetching addon name!");
        HtmlPage page = getFetchedPage();
        HtmlSpan nameSpan = (HtmlSpan) page.getByXPath("//span[@class='overflow-tip']").get(0);
        String name = nameSpan.asText();
        Log.verbose("Found addon name: " + name);
        return sanatizeInput(name);
    }

    @Override
    public String getAuthor() throws DataCollectionException {
        Log.verbose("Fetching author name!");
        HtmlPage page = getScrapedOverviewPage();
        HtmlListItem authorListItem = (HtmlListItem) page.getByXPath("//li[@class='user-tag-large owner']").get(0);
        HtmlDivision authorDiv = (HtmlDivision) authorListItem.getByXPath(".//div[@class='info-wrapper']").get(0);
        HtmlAnchor authorAnchor = (HtmlAnchor) authorDiv.getByXPath(".//a[contains(@href, '/members/')]").get(0);
        String author = authorAnchor.asText();
        Log.verbose("Found author: " + author);
        return sanatizeInput(author);
    }

    private HtmlPage getScrapedOverviewPage() throws DataCollectionException {
        if(scrapedOverviewPage != null){
            return scrapedOverviewPage;
        }
        scrapedOverviewPage = scrape(baseUrl);
        return scrapedOverviewPage;
    }

    @Override
    public String getFileName(){
        HtmlPage page = getFetchedPage();
        HtmlTableRow row = findFirstDownloadRow(page);
        String fileName = findFileAnchor(row).asText();
        return sanatizeInput(fileName);
    }

    @Override
    public boolean isValidLink() throws DataCollectionException {
        WowAceScraper scraper = this;
        if(getUrl().endsWith(officialSuffix)){
            scraper = getNonOfficialScraper(baseUrl, false);
        }
        HtmlPage page = scraper.getFetchedPage();
        if(page == null){
            return false;
        }
        HtmlTableRow downloadRow = scraper.findFirstDownloadRow(page);
        if(downloadRow == null){
            return false;
        }
        return true;
    }

    //HELPER METHODS

    private HtmlTableRow findFirstDownloadRow(HtmlPage page){
        final List<?> rows = page.getByXPath("//tr");

        for(int i=0; i<rows.size(); i++){
            HtmlTableRow row = (HtmlTableRow) rows.get(i);

            // check if row length matches
            if(row.getCells().size() != 6){
                continue;
            }
            // check if row is header
            if(row.getCell(0).asText().equals("Type")){
                continue;
            }
            // returns first row found that matches
            return row;
        }

        // If no download rows can be found return null
        return null;
    }

    private HtmlAbbreviated findDateAbbr(HtmlTableRow row){
        HtmlTableCell dateCell = row.getCell(3);
        HtmlAbbreviated abbreviated = (HtmlAbbreviated) dateCell.getChildren().iterator().next();
        return abbreviated;
    }

    private HtmlAnchor findDownloadAnchor(HtmlTableRow row){
        HtmlTableCell downloadCell = row.getCell(1);
        HtmlAnchor downloadAnchor = (HtmlAnchor) downloadCell.getByXPath(".//a[@class='button tip fa-icon-download icon-only']").get(0);
        return downloadAnchor;
    }

    private HtmlAnchor findFileAnchor(HtmlTableRow row){
        HtmlTableCell fileCell = row.getCell(1);
        HtmlDivision fileDiv = (HtmlDivision) fileCell.getByXPath(".//div[@class='project-file-name-container']").get(0);
        HtmlAnchor fileAnchor = (HtmlAnchor) fileDiv.getChildren().iterator().next();
        return fileAnchor;
    }

    @Override
    public boolean isGameVersionSupported(){
        HtmlTableRow row = findFirstDownloadRow(getFetchedPage());
        if(row == null){
            return false;
        }
        return true;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}

