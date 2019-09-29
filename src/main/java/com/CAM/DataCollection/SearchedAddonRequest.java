package com.CAM.DataCollection;

import com.CAM.HelperTools.AddonSource;

public interface SearchedAddonRequest {
    AddonSource getAddonSource();

    String getName();

    String getAuthor();

    String getOrigin();

    int getProjectId();
}
