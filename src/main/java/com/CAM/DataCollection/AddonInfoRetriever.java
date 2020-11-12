package com.CAM.DataCollection;

import com.CAM.HelperTools.GameSpecific.AddonSource;
import com.gargoylesoftware.htmlunit.Page;

import java.util.Date;

public interface AddonInfoRetriever {

    String getDownloadLink() throws DataCollectionException;

    Date getLastUpdated() throws DataCollectionException;

    String getName() throws DataCollectionException;

    String getAuthor() throws DataCollectionException;

    String getFileName() throws DataCollectionException;

    String getUrl();

    Page getFetchedPage();

    void setFetchedPage(Page scrapedPage);

    boolean isValidLink() throws DataCollectionException;

    AddonSource getAddonSource();

}
