package com.CAM.DataCollection.WowInterface;

import com.CAM.DataCollection.DataCollectionException;
import com.CAM.DataCollection.Scraper;
import com.CAM.HelperTools.GameSpecific.AddonSource;
import com.CAM.HelperTools.DateConverter;
import com.CAM.HelperTools.Logging.Log;
import com.gargoylesoftware.htmlunit.html.*;

import java.util.Date;
import java.util.List;

@Deprecated
public class WowInterfaceScraper extends Scraper {

    public WowInterfaceScraper(String url, boolean updatingAddon) throws DataCollectionException {
        super(url, AddonSource.WOWINTERFACE);

        if(!updatingAddon && !isValidLink()){
            throw new DataCollectionException(getAddonSource(), "Invalid WowInterface url!");
        }
    }

    @Override
    public String getDownloadLink() throws DataCollectionException {
        HtmlPage page = getFetchedPage();
        HtmlDivision downloadDiv = (HtmlDivision) page.getByXPath("//div[@id='download']").get(0);
        HtmlAnchor downloadAnchor= (HtmlAnchor) downloadDiv.getByXPath(".//a").get(0);
        String relative = downloadAnchor.getHrefAttribute();
        String fullPath = "https://www.wowinterface.com" + relative;
        Scraper scraper = new WowInterfaceScraper(fullPath, true);
        HtmlPage downloadPage = scraper.getFetchedPage();
        HtmlDivision manualDiv = (HtmlDivision) downloadPage.getByXPath("//div[@class='manuallink']").get(0);
        HtmlAnchor manualAnchor= (HtmlAnchor) manualDiv.getByXPath(".//a").get(0);
        String downloadLink = manualAnchor.getHrefAttribute();
        return downloadLink;
    }

    @Override
    public Date getLastUpdated() {
        HtmlPage page = getFetchedPage();
        HtmlDivision updatedDiv = (HtmlDivision) page.getByXPath("//div[@id='safe']").get(0);
        String updated = updatedDiv.asText();
        String trimmed = updated.replace("Updated: ", "");
        Date date = DateConverter.convertFromWowInterface(trimmed);
        return date;
    }

    @Override
    public String getName() {
        Log.verbose("Fetching author name!");
        HtmlPage page = getFetchedPage();
        HtmlHeading1 nameHeading = (HtmlHeading1) page.getByXPath("//h1").get(0);
        String name = nameHeading.asText();
        String trimmed = name.replace("&nbsp; ", "");
        Log.verbose("Found author name: " + trimmed);
        return sanatizeInput(trimmed);
    }

    @Override
    public String getAuthor() {
        Log.verbose("Fetching author name!");
        HtmlPage page = getFetchedPage();
        HtmlDivision authorDiv = (HtmlDivision) page.getByXPath("//div[@id='author']").get(0);
        HtmlAnchor authorAnchor = (HtmlAnchor) authorDiv.getByXPath(".//a").get(0);
        String author = authorAnchor.getTextContent().replace("<b>", "").replace("<\\b>", "");
        Log.verbose("Found author: " + author);
        return sanatizeInput(author);
    }

    @Override
    public String getFileName() {
        HtmlPage page = getFetchedPage();
        HtmlDivision fileNameDiv = (HtmlDivision) page.getByXPath("//div[@id='version']").get(0);
        String fileName = fileNameDiv.getTextContent().replace("Version: ", "");
        return sanatizeInput(fileName);
    }

    @Override
    public boolean isValidLink() {
        HtmlPage page = getFetchedPage();
        List<?> downloadDivList =  page.getByXPath("//div[@id='download']");
        if(downloadDivList.isEmpty()){
            return false;
        }
        return true;
    }

}
