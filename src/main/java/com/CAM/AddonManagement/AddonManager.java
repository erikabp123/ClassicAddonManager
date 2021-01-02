package com.CAM.AddonManagement;

import com.CAM.DataCollection.*;
import com.CAM.HelperTools.*;
import com.CAM.HelperTools.GameSpecific.AddonSource;
import com.CAM.HelperTools.GameSpecific.GameVersion;
import com.CAM.HelperTools.IO.FileOperations;
import com.CAM.HelperTools.IO.ReadWriteClassFiles;
import com.CAM.HelperTools.Logging.Log;
import com.CAM.Settings.Preferences;
import com.CAM.Settings.SessionOnlySettings;
import net.lingala.zip4j.model.FileHeader;

import java.io.*;
import java.util.*;

public class AddonManager {

    private static final String DOWNLOAD_LOCATION = "downloads/";
    private static final String DATA_LOCATION = "data/";


    private String version = "3.0";
    private List<Addon> managedAddons;
    private String installLocation;
    private GameVersion gameVersion;

    public AddonManager(String installLocation, GameVersion gameVersion) {
        this.managedAddons = new ArrayList<>();
        this.installLocation = installLocation;
        this.gameVersion = gameVersion;
    }

    public String getInstallLocation(){
        return installLocation;
    }

    public GameVersion getGameVersion(){
        return gameVersion;
    }

    public void setGameVersion(GameVersion gameVersion){ this.gameVersion = gameVersion; }

    private String getManagedLocation() {
        return DATA_LOCATION + gameVersion + "/managed.json";
    }

    private String getAddonFoldersLocation() {
        return DATA_LOCATION + gameVersion + "/managed/";
    }

    public void importAddonList(ArrayList<Addon> addons) {
        for (Addon addon : addons) {
            if (listContainsAddon(addon)) {
                continue;
            }
            managedAddons.add(addon);
        }
        saveToFile();
    }

    private boolean listContainsAddon(Addon addon) {
        for (Addon storedAddon : managedAddons) {
            if (addon.getOrigin().equals(storedAddon.getOrigin())) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<Addon> checkForUpdates(ArrayList<Exception> exceptions){
        Log.log("Checking for updates ...");
        ArrayList<Addon> updatesAvailable = new ArrayList<>();

        for(Addon addon : getManagedAddons()){
            if (SessionOnlySettings.isSkipGithubDownloads() && addon.getAddonSource() == AddonSource.GITHUB) {
                Log.log("Skipping github addon " + addon.getName() + " by " + addon.getAuthor());
                continue;
            }

            if (!SessionOnlySettings.isForceUpdateChecking()
                    && addon.getLastUpdateCheck() != null
                    && System.currentTimeMillis() < addon.getLastUpdateCheck().getTime() + Preferences.getInstance().getMaxCacheDuration()
            ) {
                Log.log("Checked addon " + addon.getName() + " by " + addon.getAuthor() + " recently! Skipping!");
                continue;
            }

            try {
                UpdateResponse response = addon.checkForUpdate(gameVersion);
                if (!response.isUpdateAvailable()) {
                    Log.verbose(addon.getName() + " by " + addon.getAuthor() + " is up to date!");
                    addon.setLastUpdateCheck(new Date());
                    saveToFile();
                    continue;
                }

                Log.log("update available for: " + addon.getName() + " by " + addon.getAuthor() + "!");
                updatesAvailable.add(addon);
            } catch (Exception e) {
                exceptions.add(e);
            }
        }

        return updatesAvailable;
    }

    public void updateSpecificAddon(Addon addon, TableViewStatus tableViewStatus) throws DataCollectionException {

        Log.log("Updating " + addon.getName() + " by " + addon.getAuthor() + "...");

        addon.fetchUpdate(addon.getAPI(true, gameVersion), tableViewStatus);
        install(addon);

        Date now = new Date();
        addon.setLastUpdateCheck(now);
        addon.setLastUpdated(now);
        System.out.println(now);
        saveToFile();
        Log.log("Finished updating!");
    }

    public ArrayList<Exception> updateAllAddons(HashMap<Addon, TableViewStatus> addonsWithUpdates) {
        ArrayList<Exception> exceptions = new ArrayList<>();
        Log.log("Updating addons ...");

        ArrayList<Addon> sortedAddons = new ArrayList<>(addonsWithUpdates.keySet());
        Collections.sort(sortedAddons);
        for (Addon addon : sortedAddons) {
            try {
                updateSpecificAddon(addon, addonsWithUpdates.get(addon));
            } catch (DataCollectionException e) {
                exceptions.add(e);
            }
        }
        Log.log("Finished updating!");
        return exceptions;
    }

    public ArrayList<Exception> updateAddons(UpdateProgressListener listener) {
        int UPDATE_MIN_WAIT = 1800000; //30 min in ms
        ArrayList<Exception> exceptions = new ArrayList<>();
        Log.log("Updating addons ...");
        int position = 0;
        int statusCode;
        AddonSource lastAddonSource = null;
        for (Addon addon : managedAddons) {
            statusCode = 1;
            listener.informStart(position);
            try {
                if (!SessionOnlySettings.isForceUpdateChecking()
                        && addon.getLastUpdateCheck() != null
                        && System.currentTimeMillis() < addon.getLastUpdateCheck().getTime() + UPDATE_MIN_WAIT
                ) {
                    Log.log("Checked addon " + addon.getName() + " by " + addon.getAuthor() + " recently! Skipping!");
                    statusCode = 2;
                    listener.informFinish(position, statusCode);
                    position++;
                    continue;
                }
                AddonSource addonSource = addon.getAddonSource();
                if ((addonSource == AddonSource.WOWINTERFACE || addonSource == AddonSource.WOWACE) && addonSource == lastAddonSource) {
                    try {
                        Thread.sleep(getSleepDelay());
                    } catch (InterruptedException e) {
                        Log.printStackTrace(e);
                    }
                }
                lastAddonSource = addonSource;

                if (SessionOnlySettings.isSkipGithubDownloads() && addon.getOrigin().contains("github.com")) {
                    Log.log("Skipping github addon " + addon.getName() + " by " + addon.getAuthor());
                    statusCode = 2;
                    listener.informFinish(position, statusCode);
                    position++;
                    continue;
                }
                addon.setLastUpdateCheck(new Date());
                UpdateResponse response = addon.checkForUpdate(gameVersion);
                if (!response.isUpdateAvailable() && !SessionOnlySettings.isForceReDownloads()) {
                    Log.verbose(addon.getName() + " by " + addon.getAuthor() + " is up to date!");
                    statusCode = 1;
                    listener.informFinish(position, statusCode);
                    position++;
                    saveToFile();
                    continue;
                }
                Log.log("update available for: " + addon.getName() + " by " + addon.getAuthor() + "!");
                addon.fetchUpdate(response.getApi(), null);
                install(addon);
            } catch (DataCollectionException e) {
                exceptions.add(e);
                statusCode = 0;
            } catch (Exception e) {
                exceptions.add(new DataCollectionException(addon, e));
                statusCode = 0;
            }
            listener.informFinish(position, statusCode);
            position++;
            addon.setLastUpdated(new Date());
            saveToFile();
        }
        Log.log("Finished updating!");
        return exceptions;
    }

    public void updateToLatestFormat(UpdateListener listener) throws DataCollectionException {
        int i = 1;
        for (Addon addon : getManagedAddons()) {
            listener.notifyProgress(i);
            System.out.println("processing addon " + i + "/" + getManagedAddons().size());
            boolean result = addon.updateToLatestFormat();
            if (result) {
                saveToFile();
            }
            System.out.println("Finished addon " + i + "/" + getManagedAddons().size());
            i++;
            if (result) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Log.printStackTrace(e);
                }
            }
        }
    }

    private int getSleepDelay() {
        int DELAY_RANGE = 401; // range is 300-700, 101 instead of 100 since 600 is included
        int MIN_DELAY = 300;
        return (new Random()).nextInt(DELAY_RANGE) + MIN_DELAY;
    }

    public Addon addNewAddon(AddonRequest request) throws DataCollectionException {
        Log.log("Attempting to track new addon ...");

        UrlInfo urlInfo = UrlInfo.examineAddonUrl(request.origin);
        if (!urlInfo.isValid) {
            Log.log("Could not track addon!");
            return null;
        }
        String trimmedOrigin = UrlInfo.trimString(request.origin, urlInfo.addonSource);

        for (Addon addon : managedAddons) {
            if (!addon.getOrigin().equals(trimmedOrigin)) {
                continue;
            }
            Log.log(addon.getName() + " already being tracked!");
            return null;
        }

        API api = UrlInfo.getCorrespondingAPI(gameVersion, urlInfo.addonSource, trimmedOrigin, false, request.branch, request.releases, -1);

        String name = api.getName();
        String author = api.getAuthor();
        Addon newAddon = new Addon(name, author, trimmedOrigin, request.branch, request.releases);

        managedAddons.add(newAddon);
        Collections.sort(managedAddons);
        saveToFile();

        Log.log("Successfully tracking new addon!");
        return newAddon;
    }

    public Addon addNewSearchedAddon(SearchedAddonRequest request) throws DataCollectionException {
        Log.log("Attempting to track new addon ...");

        int projectId = request.getProjectId();
        for (Addon addon : managedAddons) {
            if (addon.getProjectId() == projectId) {
                Log.log(addon.getName() + " already being tracked!");
                return null;
            }
        }

        String name = request.getName();
        String author = request.getAuthor();
        String origin = request.getOrigin();
        Addon newAddon = new Addon(name, author, origin, projectId);

        managedAddons.add(newAddon);
        Collections.sort(managedAddons);
        saveToFile();

        Log.log("Successfully tracking new addon!");
        return newAddon;
    }

    public boolean removeAddon(Addon addon) {
        Log.log("Attempting to remove addon ...");

        if (!managedAddons.contains(addon)) {
            Log.log("Addon " + addon.getName() + " was not found!");
            return false;
        }

        uninstall(addon);
        managedAddons.remove(addon);

        saveToFile();

        Log.log("Successfully removed addon!");
        return true;
    }

    public static AddonManager initializeFromScanUI(GameVersion gameVersion, String installLocation) {
        Log.verbose("Looking for managed.json file ...");
        String managedPath = "data/" + gameVersion + "/managed.json";

        boolean existingManagedList = new File(managedPath).isFile();
        if (!existingManagedList) {
            Log.verbose("No managed.json file found!");

            installLocation = installLocation + "Interface\\AddOns\\";

            AddonManager manager = new AddonManager(installLocation, gameVersion);
            manager.saveToFile();
            Log.log("Setup complete!");

            return manager;
        }

        Log.verbose("Loading managed.json ...");
        AddonManager addonManager = (AddonManager) ReadWriteClassFiles.readFile(managedPath, new AddonManager(null, null));
        Log.verbose("Successfully loaded managed.json!");

        return addonManager;
    }

    public static AddonManager loadManagerFromFile(GameVersion gameVersion) {
        Log.verbose("Looking for managed.json file ...");
        String managedPath = "data/" + gameVersion + "/managed.json";

        boolean existingManagedList = new File(managedPath).isFile();
        if (!existingManagedList) {
            Log.verbose("No managed.json file found!");
            return null;
        }

        Log.verbose("Loading managed.json ...");
        AddonManager addonManager = (AddonManager) ReadWriteClassFiles.readFile(managedPath, new AddonManager(null, null));
        Log.verbose("Successfully loaded managed.json!");

        return addonManager;
    }

    public void saveToFile() {
        Log.verbose("Attempting to save to managed.json ...");
        ReadWriteClassFiles.saveFile(getManagedLocation(), this);
        Log.verbose("Successfully saved to managed.json!");
    }

    public void install(Addon addon) {
        // Uninstall old save for clean updating
        Log.verbose("Attempting install of addon " + addon.getName() + " ...");
        uninstall(addon);

        String zipPath = DOWNLOAD_LOCATION + addon.getLastFileName();
        List<FileHeader> headers = FileOperations.unzip(zipPath, installLocation);
        logInstallation(addon, headers);

        FileOperations.deleteFile(zipPath);

        Set<String> directoriesFullPaths = new HashSet<>();
        for (String directory : readInstallationLog(addon)) {
            String fullPathToDirectory = installLocation + directory;
            directoriesFullPaths.add(fullPathToDirectory);
        }
        Set<String> tocFolders = new HashSet<>();
        handleAllSubFolders(directoriesFullPaths, tocFolders);
        Set<String> newDirectories = new HashSet<>();
        for (String fullPath : tocFolders) {
            File file = new File(fullPath);
            newDirectories.add(file.getName());
        }
        logRename(addon, newDirectories);
        if (addon.getOrigin().contains("github")) {
            renameFolders(addon);
        }
        Log.verbose("Successfully installed addon!");
    }

    private void handleAllSubFolders(Set<String> directories, Set<String> tocFolders) {
        for (String directory : directories) {
            handleFolder(directory, tocFolders);
        }
    }

    private void handleFolder(String directory, Set<String> tocFolders) {
        System.out.println("Handling: " + directory);
        if (!containsSubFolders(directory)) {
            tocFolders.add(directory);
            return;
        }
        String newPath = renameParentFolder(directory);
        Set<String> moved = moveChildFoldersUp(newPath);
        deleteParentFolder(newPath);
        handleAllSubFolders(moved, tocFolders);
    }

    private void deleteParentFolder(String newPath) {
        FileOperations.deleteDirectory(newPath);
    }

    private Set<String> moveChildFoldersUp(String parentPath) {
        Set<String> moved = new HashSet<>();
        File dir = new File(parentPath);
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (!files[i].isDirectory()) {
                continue;
            }
            String curPath = null;
            String newPath = null;
            try {
                curPath = files[i].getCanonicalPath();
                newPath = files[i].getParentFile().getParent() + "\\" + files[i].getName();
            } catch (IOException e) {
                Log.printStackTrace(e);
            }
            System.out.println("Moving: " + curPath + " to " + newPath);
            FileOperations.moveFile(curPath, newPath);
            moved.add(newPath);
        }
        return moved;
    }

    private String renameParentFolder(String path) {
        String newName = "CAM_BEING_RENAMED";
        FileOperations.renameDirectory(path, newName);
        String[] pathParts = path.split("\\\\");
        String newPath = "";
        for (int i = 0; i < pathParts.length - 1; i++) {
            newPath = newPath + "\\" + pathParts[i];
        }
        newPath = newPath + "\\" + newName;
        return newPath;
    }

    private boolean containsSubFolders(String fullPath) {
        String tocName = FileOperations.determineTOCName(fullPath);
        boolean isTocFolder = (tocName == null);
        return isTocFolder;
    }

    private void renameFolders(Addon addon) {
        Set<String> directories = readInstallationLog(addon);
        Set<String> renamedDirectories = new HashSet<>();
        for (String directory : directories) {
            String fullPath = installLocation + directory;
            if (!determineIfFolderShouldBeRenamed(fullPath)) {
                renamedDirectories.add(directory);
                continue;
            }
            String newName = renameFolderToTOCName(fullPath);
            renamedDirectories.add(newName);
        }
        logRename(addon, renamedDirectories);
    }

    private void logRename(Addon addon, Set<String> directories) {
        Log.verbose("Attempting to log rename ...");
        ReadWriteClassFiles.saveFile(getInstallationLogPath(addon), directories);
        Log.verbose("Successfully logged rename!");
    }

    private boolean determineIfFolderShouldBeRenamed(String path) {
        Log.verbose("Determining if folder should be renamed ...");
        String[] pathParts = path.split("\\\\");
        String folderName = pathParts[pathParts.length - 1];
        String requiredName = FileOperations.determineTOCName(path);
        if (folderName.equals(requiredName)) {
            Log.verbose("Folder name matches TOC file!");
            return false;
        }
        Log.verbose("Folder name is incorrect!");
        return true;
    }

    private String renameFolderToTOCName(String path) {
        String tocName = FileOperations.determineTOCName(path);
        FileOperations.renameDirectory(path, tocName);
        return tocName;
    }

    private void logInstallation(Addon addon, List<FileHeader> headers) {
        Log.verbose("Attempting to log installation ...");
        ReadWriteClassFiles.saveFile(getInstallationLogPath(addon), getOnlyRootDirectories(headers));
        Log.verbose("Successfully logged installation!");
    }

    private void uninstall(Addon addon) {
        Log.verbose("Attempting uninstall of " + addon.getName() + " ...");
        Set<String> directories = readInstallationLog(addon);
        for (String directory : directories) {
            String fullPath = installLocation + directory;
            FileOperations.deleteDirectory(fullPath);
        }
        FileOperations.deleteFile(getInstallationLogPath(addon));
        Log.verbose("Finished uninstall!");
    }

    public Set<String> readInstallationLog(Addon addon) {
        Log.verbose("Looking for addon installation log ...");

        String filePath = getInstallationLogPath(addon);

        if (!new File(filePath).isFile()) {
            Log.verbose("No installation log found!");
            return new HashSet<>();
        }

        Log.verbose("Loading installation log ...");

        Set<String> directories = (Set<String>) ReadWriteClassFiles.readFile(filePath, new HashSet());
        Log.verbose("Directories: " + directories);

        Log.verbose("Successfully loaded installation log!");

        return directories;
    }

    public List<Addon> getManagedAddons() {
        return managedAddons;
    }


    //HELPER METHODS

    public String getInstallationLogPath(Addon addon) {
        return getAddonFoldersLocation() + addon.getName() + ".json";
    }

    private Set<String> getOnlyRootDirectories(List<FileHeader> headers) {
        Set<String> rootDirectories = new HashSet<>();

        for (FileHeader header : headers) {
            String[] headerInfo = header.getFileName().split("/");
            rootDirectories.add(headerInfo[0]);
        }
        return rootDirectories;
    }

    public void setInstallLocation(String installLocation) {
        this.installLocation = installLocation;
        saveToFile();
    }


    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
