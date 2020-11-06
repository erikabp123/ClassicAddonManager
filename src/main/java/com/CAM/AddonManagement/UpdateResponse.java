package com.CAM.AddonManagement;

import com.CAM.DataCollection.API;
import com.CAM.DataCollection.AddonInfoRetriever;

public class UpdateResponse {

    private API api;
    private boolean updateAvailable;

    public UpdateResponse(API api, boolean updateAvailable){
        this.api = api;
        this.updateAvailable = updateAvailable;
    }

    public API getApi() {
        return api;
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public void setUpdateAvailable(boolean updateAvailable) {
        this.updateAvailable = updateAvailable;
    }
}
