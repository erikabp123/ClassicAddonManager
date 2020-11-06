package com.CAM.DataCollection.TwitchOwned.CurseForge.CurseAddonReponse;

import com.CAM.DataCollection.SearchedAddonRequest;
import com.CAM.DataCollection.WowInterface.WowInterfaceAddonResponse.WowInterfaceAddonResponse;
import com.CAM.HelperTools.AddonSource;
import com.CAM.HelperTools.DateConverter;
import com.CAM.HelperTools.GameVersion;

import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;

public class CurseAddonResponse extends SearchedAddonRequest {
    public int id;
    public String name;
    public ArrayList<CurseAuthor> authors;
    public ArrayList<CurseAttachment> attachments;
    public String websiteUrl;
    public int gameId;
    public String summary;
    public int defaultFileId;
    public ArrayList<CurseFile> latestFiles;
    public ArrayList<CurseCategory> Categories;
    public int status;
    public int primaryCategoryId;
    public CurseCategorySection categorySection;
    public String slug;
    public ArrayList<CurseGameVersionFile> gameVersionLatestFiles;
    public boolean isFeatured;
    public double popularityScore;
    public int gamePopularityRank;
    public String primaryLanguage;
    public String gameSlug;
    public String gameName;
    public String portalName;
    public String dateModified;
    public String dateCreated;
    public String dateReleased;
    public boolean isAvailable;
    public boolean isExperiemental;


    public boolean isGameVersionSupported(GameVersion gameVersion){
        for(CurseFile file : latestFiles){
            if(file.gameVersionFlavor.equals(gameVersion.getCurseFlavor())){
                return true;
            }
        }
        return false;
    }

    @Override
    public AddonSource getAddonSource() {
        return AddonSource.CURSEFORGE;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getAuthor() {
        return authors.get(0).name;
    }

    @Override
    public String getOrigin() {
        return websiteUrl;
    }

    @Override
    public int getProjectId() {
        return id;
    }

    @Override
    public String getDescription() {
        return summary;
    }

}

