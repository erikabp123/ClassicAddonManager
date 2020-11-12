package com.CAM.DataCollection.TwitchOwned.CurseForge.CurseAddonReponse;

import com.CAM.DataCollection.SearchedAddonRequest;
import com.CAM.HelperTools.GameSpecific.AddonSource;
import com.CAM.HelperTools.GameSpecific.GameVersion;
import com.CAM.HelperTools.GameSpecific.PatchNumber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    @Override
    public AddonSource getSource() {
        return AddonSource.CURSEFORGE;
    }

    @Override
    public String getDonationLink() {
        return null;
    }

    @Override
    public List<String> getScreenshots() {
        return null;
    }

    @Override
    public List<String> getSupportedPatches() {
        HashMap<GameVersion, PatchNumber> latestByGameVersion = new HashMap<>();
        for(CurseGameVersionFile file : gameVersionLatestFiles){
            PatchNumber patchNumber = new PatchNumber(file.gameVersion);
            GameVersion gameVersion = patchNumber.getGameVersion();
            if(gameVersion == null){
                continue;
            }
            if(latestByGameVersion.containsKey(gameVersion)) {
                PatchNumber latestPatchNumber = latestByGameVersion.get(gameVersion);
                PatchNumber latest = PatchNumber.getHighestPatchNumber(patchNumber, latestPatchNumber);
                latestByGameVersion.put(gameVersion, latest);
            } else {
                latestByGameVersion.put(gameVersion, patchNumber);
            }
        }

        List<String> supportedPatches = new ArrayList<>();

        if(latestByGameVersion.isEmpty()){
            PatchNumber newest = null;
            for(CurseGameVersionFile file : gameVersionLatestFiles){
                PatchNumber patchNumber = new PatchNumber(file.gameVersion);
                if(newest == null) {
                    newest = patchNumber;
                    continue;
                }
                newest = PatchNumber.getHighestPatchNumber(patchNumber, newest);
            }
        } else {
            for(PatchNumber pn: latestByGameVersion.values()) supportedPatches.add(pn.toString());
        }

        return supportedPatches;
    }

}

