package DataCollection;

import HelperTools.Log;
import com.gargoylesoftware.htmlunit.html.*;

import java.util.List;

public class CurseForgeScraper extends Scraper {

    private final static String gameVersion = "1738749986%3A67408"; //TODO: change to non-final and support automatic scraping of gameVersion in case it changes
    private final static String searchSuffix = "/files/all?filter-game-version=" + gameVersion;
    private final static String websiteUrl = "https://www.curseforge.com";

    private int versions;

    public CurseForgeScraper(String url) {
        super(url + searchSuffix, false, false, true);
        versions = 1;
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
    public String getLastUpdated(){
        HtmlPage page = getScrapedPage();
        HtmlTableRow row = findFirstDownloadRow(page);
        return findDateAbbr(row).getAttribute("title");
    }

    @Override
    public String getName(){
        Log.log("Fetching author name!");
        HtmlPage page = getScrapedPage();
        HtmlHeading2 nameHeading = (HtmlHeading2) page.getByXPath("//h2").get(0);
        String name = nameHeading.asText();
        Log.log("Found author name: " + name);
        return name;
    }

    @Override
    public String getAuthor(){
        Log.log("Fetching author name!");
        HtmlPage page = getScrapedPage();
        HtmlAnchor authorAnchor = (HtmlAnchor) page.getByXPath("//a[contains(@href, '/members/')]").get(1);
        String author = authorAnchor.getChildren().iterator().next().asText();
        Log.log("Found author: " + author);
        return author;
    }

    @Override
    public String getFileName(){
        HtmlPage page = getScrapedPage();
        HtmlTableRow row = findFirstDownloadRow(page);
        System.out.println("File name: " + findFileAnchor(row).asText());
        return findFileAnchor(row).asText();
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
        HtmlDivision containerDiv = (HtmlDivision) downloadCell.getChildren().iterator().next();
        HtmlAnchor downloadAnchor = (HtmlAnchor) containerDiv.getChildren().iterator().next();
        return downloadAnchor;
    }

    private HtmlAnchor findFileAnchor(HtmlTableRow row){
        HtmlTableCell fileCell = row.getCell(1);
        HtmlAnchor fileAnchor = (HtmlAnchor) fileCell.getChildren().iterator().next();
        return fileAnchor;
    }

    public int getVersions() {
        return versions;
    }
}
