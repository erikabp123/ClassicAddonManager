package com.CAM.AddonManagement;

import com.CAM.DataCollection.AddonInfoRetriever;
import com.CAM.DataCollection.Scraper;

public class UpdateResponse {

    private AddonInfoRetriever retriever;
    private boolean updateAvailable;

    public UpdateResponse(Scraper scraper){
        this.retriever = scraper;
    }

    public UpdateResponse(AddonInfoRetriever retriever, boolean updateAvailable){
        this.retriever = retriever;
        this.updateAvailable = updateAvailable;
    }

    public AddonInfoRetriever getRetriever() {
        return retriever;
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public void setUpdateAvailable(boolean updateAvailable) {
        this.updateAvailable = updateAvailable;
    }
}
