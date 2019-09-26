package com.CAM.DataCollection.TwitchOwned.CurseForge.CurseAddonReponse;

import java.util.ArrayList;

public class CurseFile {
    public int id;
    public String displayName;
    public String fileName;
    public String fileDate;
    public int fileLength;
    public int releaseType;
    public int fileStatus;
    public String downloadUrl;
    public boolean isAlternate;
    public int alternateFileId;
    public ArrayList<CurseDependency> dependencies;
    public boolean isAvailable;
    public ArrayList<CurseModule> moduels;
    public int packageFingerPrint;
    public ArrayList<String> gameVersion;
    public ArrayList<CurseGameVersion> sortableGameVersion;
    public Object installMetadata;
    public Object changelog;
    public boolean hasInstallScript;
    public boolean isCompatibleWithClient;
    public int categorySectionPackageType;
    public int restrictProjectFileAccess;
    public int projectStatus;
    public int renderCacheId;
    public Object fileLegacyMappingId;
    public int projectId;
    public Object parentProjectFileId;
    public Object parentFileLegacyMappingId;
    public Object fileTypeId;
    public Object exposeAsAlternative;
    public int PackageFingerPrintId;
    public String gameVersionDateRelease;
    public int gameVersionMappingId;
    public int gameVersionId;
    public int gameId;
    public boolean isServerPack;
    public Object serverPackFileId;
    public String gameVersionFlavor;
}
