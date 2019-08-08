package DataCollection;

import com.gargoylesoftware.htmlunit.html.*;

import java.util.List;

public class CurseForgeScraper extends Scraper {

    private final static String gameVersion = "1738749986%3A67408"; //TODO: change to non-final and support automatic scraping of gameVersion in case it changes
    private final static String searchSuffix = "/files/all?filter-game-version=" + gameVersion;
    private final static String websiteUrl = "https://www.curseforge.com";

    public CurseForgeScraper(String url) {
        super(url + searchSuffix, false, false, true);
    }

    public String getDownloadLink(){
        HtmlPage page = scrape();

        HtmlTableRow row = findFirstDownloadRow(page);
        HtmlAnchor downloadAnchor = findDownloadAnchor(row);
        String downloadSuffix = downloadAnchor.getAttribute("href");

        return websiteUrl + downloadSuffix;
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

    private HtmlAnchor findDownloadAnchor(HtmlTableRow row){
        HtmlTableCell downloadCell = row.getCell(6);
        HtmlDivision containerDiv = (HtmlDivision) downloadCell.getChildren().iterator().next();
        HtmlAnchor downloadAnchor = (HtmlAnchor) containerDiv.getChildren().iterator().next();
        return downloadAnchor;
    }

}
