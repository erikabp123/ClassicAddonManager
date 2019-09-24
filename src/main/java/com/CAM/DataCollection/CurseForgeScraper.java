package com.CAM.DataCollection;

import com.CAM.HelperTools.AddonSource;
import com.CAM.HelperTools.DateConverter;
import com.CAM.HelperTools.Log;
import com.gargoylesoftware.htmlunit.html.*;

import java.util.Date;
import java.util.List;

public class CurseForgeScraper extends Scraper implements TwitchSite {

    private final static String gameVersion = "1738749986%3A67408"; //TODO: change to non-final and support automatic scraping of gameVersion in case it changes
    private final static String officialSuffix = "/files/all?filter-game-version=" + gameVersion;
    private final static String nonOfficialSuffix = "/files/all";
    private final static String websiteUrl = "https://www.curseforge.com";

    private String baseUrl;

    public static CurseForgeScraper makeScraper(String url, boolean updatingAddon) throws ScrapeException {
        CurseForgeScraper scraper = getOfficialScraper(url, updatingAddon);
        if(scraper.isClassicSupported()){
            return scraper;
        }
        return getNonOfficialScraper(url, updatingAddon);
    }

    public static CurseForgeScraper getNonOfficialScraper(String url, boolean updatingAddon) throws ScrapeException {
        return new CurseForgeScraper(url, nonOfficialSuffix, updatingAddon);
    }

    public static CurseForgeScraper getOfficialScraper(String url, boolean updatingAddon) throws ScrapeException {
        return new CurseForgeScraper(url, officialSuffix, updatingAddon);
    }

    public CurseForgeScraper(String url, String suffix, boolean updatingAddon) throws ScrapeException {
        super(url + suffix, false, false, true, AddonSource.CURSEFORGE);
        this.baseUrl = url;
        if(!updatingAddon && !isValidLink()){
            throw new ScrapeException(getAddonSource(), "Invalid CurseForge url!");
        }
    }

    @Override
    public String getDownloadLink(){
        HtmlPage page = getScrapedPage();

        HtmlTableRow row = findFirstDownloadRow(page);
        HtmlAnchor downloadAnchor = findDownloadAnchor(row);
        String downloadSuffix = downloadAnchor.getAttribute("href");

        return websiteUrl + downloadSuffix + "/file";
    }

    @Override
    public Date getLastUpdated(){
        HtmlPage page = getScrapedPage();
        HtmlTableRow row = findFirstDownloadRow(page);
        Date date = DateConverter.convertFromCurse(findDateAbbr(row).getAttribute("title"));
        return date;
    }

    @Override
    public String getName(){
        Log.verbose("Fetching addon name!");
        HtmlPage page = getScrapedPage();
        HtmlHeading2 nameHeading = (HtmlHeading2) page.getByXPath("//h2").get(0);
        String name = nameHeading.asText();
        Log.verbose("Found addon name: " + name);
        return sanatizeInput(name);
    }

    @Override
    public String getAuthor(){
        Log.verbose("Fetching author name!");
        HtmlPage page = getScrapedPage();
        HtmlAnchor authorAnchor = (HtmlAnchor) page.getByXPath("//a[contains(@href, '/members/')]").get(1);
        String author = authorAnchor.getChildren().iterator().next().asText();
        Log.verbose("Found author: " + author);
        return sanatizeInput(author);
    }

    @Override
    public String getFileName(){
        HtmlPage page = getScrapedPage();
        HtmlTableRow row = findFirstDownloadRow(page);
        String fileName = findFileAnchor(row).asText();
        return sanatizeInput(fileName);
    }

    @Override
    public boolean isValidLink() throws ScrapeException {
        CurseForgeScraper scraper = this;
        if(getUrl().endsWith(officialSuffix)){
            scraper = getNonOfficialScraper(baseUrl, false);
        }
        HtmlPage page = scraper.getScrapedPage();
        if(page == null){
            return false;
        }
        HtmlTableRow downloadRow = scraper.findFirstDownloadRow(page);
        if(downloadRow == null){
            return false;
        }
        return true;
    }

    @Override
    public AddonSource getAddonSource() {
        return AddonSource.CURSEFORGE;
    }

    //HELPER METHODS

    private HtmlTableRow findFirstDownloadRow(HtmlPage page){
        final List<?> rows = page.getByXPath("//tr");

        for(int i=0; i<rows.size(); i++){
            HtmlTableRow row = (HtmlTableRow) rows.get(i);

            // check if row length matches
            if(row.getCells().size() != 7){
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
        HtmlTableCell downloadCell = row.getCell(6);
        /*
        HtmlDivision containerDiv = (HtmlDivision) downloadCell.getChildren().iterator().next();
        HtmlAnchor downloadAnchor = (HtmlAnchor) containerDiv.getChildren().iterator().next();
         */
        HtmlAnchor downloadAnchor = (HtmlAnchor) downloadCell.getByXPath(".//a[@class='button button--hollow mr-2 button--icon-only']").get(0);
        return downloadAnchor;
    }

    private HtmlAnchor findFileAnchor(HtmlTableRow row){
        HtmlTableCell fileCell = row.getCell(1);
        //HtmlAnchor fileAnchor = (HtmlAnchor) fileCell.getChildren().iterator().next();
        HtmlAnchor fileAnchor = (HtmlAnchor) fileCell.getChildren().iterator().next();
        return fileAnchor;
    }

    @Override
    public boolean isClassicSupported(){
        HtmlTableRow row = findFirstDownloadRow(getScrapedPage());
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
