package com.CAM.DataCollection.TwitchOwned.CurseForge.CurseAddonReponse;

import java.util.ArrayList;

public class CurseAddonResponse {
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
}

