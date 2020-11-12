package com.CAM.DataCollection.WowInterface.WowInterfaceAddonResponse;

import com.CAM.DataCollection.SearchedAddonRequest;
import com.CAM.DataCollection.TwitchOwned.CurseForge.CurseAddonReponse.CurseGameVersionFile;
import com.CAM.HelperTools.GameSpecific.AddonSource;
import com.CAM.HelperTools.GameSpecific.GameVersion;
import com.CAM.HelperTools.GameSpecific.PatchNumber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WowInterfaceAddonResponse extends SearchedAddonRequest {

    public int UID;
    public int UICATID;
    public String UIVersion;
    public long UIDate;
    public String UIName;
    public String UIAuthorName;
    public String UIFileInfoURL;
    public long UIDownloadTotal;
    public int UIDownloadMonthly;
    public int UIFavoriteTotal;
    public ArrayList<CompatabilityUI> UICompatibility;
    public ArrayList<String> UIDir;
    public ArrayList<String> UIIMG_Thumbs;
    public ArrayList<String> UIIMGs;
    public String UIDonationLink;

    @Override
    public AddonSource getAddonSource() {
        return AddonSource.WOWINTERFACE;
    }

    @Override
    public String getName() {
        return UIName;
    }

    @Override
    public String getAuthor() {
        return UIAuthorName;
    }

    @Override
    public String getOrigin() {
        return UIFileInfoURL;
    }

    @Override
    public int getProjectId() {
        return UID;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public AddonSource getSource() {
        return AddonSource.WOWINTERFACE;
    }

    @Override
    public String getDonationLink() {
        return UIDonationLink;
    }

    @Override
    public List<String> getScreenshots() {
        return UIIMGs;
    }

    @Override
    public List<String> getSupportedPatches() {
        List<String> supportedPatches = new ArrayList<>();
        if(UICompatibility == null) return supportedPatches;

        HashMap<GameVersion, PatchNumber> latestByGameVersion = new HashMap<>();
        for(CompatabilityUI comp: UICompatibility){
            PatchNumber patchNumber = new PatchNumber(comp.version);
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

        if(latestByGameVersion.isEmpty()){
            PatchNumber newest = null;
            for(CompatabilityUI comp: UICompatibility){
                PatchNumber patchNumber = new PatchNumber(comp.version);
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

class CompatabilityUI{
    String version;
    String name;
}
