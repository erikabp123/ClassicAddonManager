package com.CAM.DataCollection;

import com.CAM.HelperTools.AddonSource;
import com.gargoylesoftware.htmlunit.Page;

import java.util.Date;

public interface AddonInfoRetriever {

    String getDownloadLink() throws ScrapeException;

    Date getLastUpdated() throws ScrapeException;

    String getName() throws ScrapeException;

    String getAuthor() throws ScrapeException;

    String getFileName() throws ScrapeException;

    String getUrl();

    Page getFetchedPage();

    void setFetchedPage(Page scrapedPage);

    boolean isValidLink() throws ScrapeException;

    AddonSource getAddonSource();

}
