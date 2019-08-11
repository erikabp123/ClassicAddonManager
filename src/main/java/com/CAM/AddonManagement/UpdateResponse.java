package com.CAM.AddonManagement;

import com.CAM.DataCollection.Scraper;

public class UpdateResponse {

    private Scraper scraper;
    private boolean updateAvailable;

    public UpdateResponse(Scraper scraper){
        this.scraper = scraper;
    }

    public UpdateResponse(Scraper scraper, boolean updateAvailable){
        this.scraper = scraper;
        this.updateAvailable = updateAvailable;
    }

    public Scraper getScraper() {
        return scraper;
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public void setUpdateAvailable(boolean updateAvailable) {
        this.updateAvailable = updateAvailable;
    }
}
