package com.CAM.DataCollection;

import com.CAM.HelperTools.AddonSource;

public interface SearchedAddonRequest {

    @Override
    String toString();

    AddonSource getAddonSource();

    String getName();

    String getAuthor();

    String getOrigin();

    int getProjectId();

}
