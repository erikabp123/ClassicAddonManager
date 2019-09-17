package com.CAM.AddonManagement;

import com.CAM.DataCollection.*;
import com.CAM.HelperTools.*;
import com.google.gson.Gson;
import net.lingala.zip4j.model.FileHeader;

import java.io.*;
import java.util.*;

public class AddonManager {

    private String version = "1.0";
    private List<Addon> managedAddons;
    private String installLocation;

    public AddonManager(String installLocation) {
        this.managedAddons = new ArrayList<>();
        this.installLocation = installLocation;
    }

    public void importAddonList(ArrayList<Addon> addons){
        for (Addon addon : addons) {
            if(listContainsAddon(addon)){
                continue;
            }
            managedAddons.add(addon);
        }
        saveToFile();
    }

    private boolean listContainsAddon(Addon addon){
        for(Addon storedAddon : managedAddons){
            if (addon.getOrigin().equals(storedAddon.getOrigin())) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<Exception> updateAddons(UpdateProgressListener listener) {
        ArrayList<Exception> exceptions = new ArrayList<>();
        Log.log("Updating addons ...");
        int position = 0;
        int statusCode;
        for (Addon addon : managedAddons) {
            statusCode = 1;
            listener.informStart(position);
            try{
                if(Log.skipGithubDownloads && addon.getOrigin().contains("github.com")){
                    Log.log("Skipping github addon " + addon.getName() + " by " + addon.getAuthor());
                    statusCode = 2;
                    listener.informFinish(position, statusCode);
                    position++;
                    continue;
                }
                UpdateResponse response = addon.checkForUpdate();
                if (!response.isUpdateAvailable()) {
                    Log.verbose(addon.getName() + " by " + addon.getAuthor() + " is up to date!");
                    statusCode = 1;
                    listener.informFinish(position, statusCode);
                    position++;
                    continue;
                }
                Log.log("update available for: " + addon.getName() + " by " + addon.getAuthor() + "!");
                addon.fetchUpdate(response.getScraper());
                install(addon);
                saveToFile();
            } catch (ScrapeException e){
                exceptions.add(e);
                statusCode = 0;
            } catch (Exception e) {
                exceptions.add(new ScrapeException(addon, e));
                statusCode = 0;
            }
            listener.informFinish(position, statusCode);
            position++;
        }
        Log.log("Finished updating!");
        return exceptions;
    }

    public boolean addNewAddon(AddonRequest request) throws ScrapeException {
        Log.log("Attempting to track new addon ...");

        UrlInfo urlInfo = UrlInfo.examineAddonUrl(request.origin);
        if (!urlInfo.isValid) {
            Log.log("Could not track addon!");
            return false;
        }
        String trimmedOrigin = "";
        switch (urlInfo.addonSource) {
            case CURSEFORGE:
                trimmedOrigin = UrlInfo.trimCurseForgeUrl(request.origin);
                break;
            case GITHUB:
                trimmedOrigin = UrlInfo.trimGitHubUrl(request.origin);
                break;
            case WOWINTERFACE:
                trimmedOrigin = UrlInfo.trimWowInterfaceUrl(request.origin);
                break;
            case TUKUI:
                trimmedOrigin = UrlInfo.trimTukuiUrl(request.origin);
                break;
        }

        for (Addon addon : managedAddons) {
            if (!addon.getOrigin().equals(trimmedOrigin)) {
                continue;
            }
            Log.log(addon.getName() + " already being tracked!");
            return false;
        }

        Scraper scraper = null;

        switch (urlInfo.addonSource) {
            case CURSEFORGE:
                scraper = CurseForgeScraper.makeScraper(trimmedOrigin, false);
                break;
            case GITHUB:
                scraper = new GitHubScraper(trimmedOrigin, request.branch, request.releases, false);
                break;
            case WOWINTERFACE:
                scraper = new WowInterfaceScraper(trimmedOrigin, false);
                break;
            case TUKUI:
                scraper = new TukuiScraper(trimmedOrigin, false);
                break;
        }

        String name = scraper.getName();
        String author = scraper.getAuthor();
        Addon newAddon = new Addon(name, author, trimmedOrigin, request.branch, request.releases);

        managedAddons.add(newAddon);
        Collections.sort(managedAddons);
        saveToFile();

        Log.log("Successfully tracking new addon!");
        return true;
    }

    public boolean removeAddon(int addonNum) {
        //TODO: Make it actually delete the folders
        Log.log("Attempting to remove addon ...");

        if (addonNum > managedAddons.size()) {
            Log.log("com.CAM.AddonManagement.Addon #" + addonNum + " was not found!");
            return false;
        }

        uninstall(managedAddons.get(addonNum));
        managedAddons.remove(addonNum);

        saveToFile();

        Log.log("Successfully removed addon!");
        return true;
    }

    public static AddonManager initialize(UserInput userInput) {
        Log.verbose("Looking for managed.json file ...");
        if (!new File("data/managed.json").isFile()) {
            Log.verbose("No managed.json file found!");
            String installLocation = specifyInstallLocation(userInput);
            if (installLocation == null) {
                return null;
            }
            AddonManager manager = new AddonManager(installLocation);
            manager.saveToFile();
            Log.log("Setup complete!");
            return manager;
        }
        AddonManager addonManager = null;

        Log.verbose("Loading managed.json ...");

        try {
            Reader reader = new FileReader("data/managed.json");
            Gson gson = new Gson();
            addonManager = gson.fromJson(reader, AddonManager.class);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.verbose("Successfully loaded managed.json!");

        return addonManager;
    }

    public void saveToFile() {
        Log.verbose("Attempting to save to managed.json ...");
        try {
            Gson gson = new Gson();
            File file = new File("data/managed.json");
            file.getParentFile().mkdirs();
            Writer writer = new FileWriter(file);
            gson.toJson(this, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.verbose("Successfully saved to managed.json!");
    }

    public void install(Addon addon) {
        // Uninstall old save for clean updating
        Log.verbose("Attempting install of addon " + addon.getName() + " ...");
        uninstall(addon);
        String zipPath = "downloads/" + addon.getLastFileName();
        List<FileHeader> headers = FileOperations.unzip(zipPath, installLocation);
        logInstallation(addon, headers);
        FileOperations.deleteFile(zipPath);
        Set<String> directoriesFullPaths = new HashSet<>();
        for(String directory : readInstallationLog(addon)){
            String fullPathToDirectory = installLocation + "\\" + directory;
            directoriesFullPaths.add(fullPathToDirectory);
        }
        Set<String> tocFolders = new HashSet<>();
        handleAllSubFolders(directoriesFullPaths, tocFolders);
        Set<String> newDirectories = new HashSet<>();
        for(String fullPath : tocFolders){
            File file = new File(fullPath);
            newDirectories.add(file.getName());
        }
        logRename(addon, newDirectories);
        if(addon.getOrigin().contains("github")) {
            renameFolders(addon);
        }
        Log.verbose("Successfully installed addon!");
    }

    private void handleAllSubFolders(Set<String> directories, Set<String> tocFolders){
        for(String directory : directories){
            handleFolder(directory, tocFolders);
        }
    }

    private void handleFolder(String directory, Set<String> tocFolders){
        System.out.println("Handling: " + directory);
        if(!containsSubFolders(directory)){
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
        for(int i=0; i<files.length; i++){
            if(!files[i].isDirectory()){
                continue;
            }
            String curPath = null;
            String newPath = null;
            try {
                curPath = files[i].getCanonicalPath();
                newPath = files[i].getParentFile().getParent() + "\\" + files[i].getName();
            } catch (IOException e) {
                e.printStackTrace();
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
        for(int i=0; i<pathParts.length - 1; i++){
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
            String fullPath = installLocation + "\\" + directory;
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

        try {
            Gson gson = new Gson();
            File file = new File(getInstallationLogPath(addon));
            file.getParentFile().mkdirs();
            Writer writer = new FileWriter(file);
            gson.toJson(directories, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

        try {
            Gson gson = new Gson();
            File file = new File(getInstallationLogPath(addon));
            file.getParentFile().mkdirs();
            Writer writer = new FileWriter(file);
            Set<String> rootDirectories = getOnlyRootDirectories(headers);
            gson.toJson(rootDirectories, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.verbose("Successfully logged installation!");
    }

    private void uninstall(Addon addon) {
        Log.verbose("Attempting uninstall of " + addon.getName() + " ...");
        Set<String> directories = readInstallationLog(addon);
        for (String directory : directories) {
            String fullPath = installLocation + "\\" + directory;
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
        Set<String> directories = null;

        Log.verbose("Loading installation log ...");

        try {
            Reader reader = new FileReader(filePath);
            Gson gson = new Gson();
            directories = gson.fromJson(reader, HashSet.class);
            reader.close();
            Log.verbose("Directories: " + directories);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.verbose("Successfully loaded managed.json!");

        return directories;
    }


    public List<Addon> getManagedAddons() {
        return managedAddons;
    }


    //HELPER METHODS

    public static boolean verifyInstallLocation(String path) {
        Log.verbose("Checking supplied path ...");
        String exePath = path + "\\Wow.exe";
        if (!(new File(exePath).exists())) {
            Log.verbose("Wow.exe not found!");
            return false;
        }
        String version = FileOperations.getFileVersion(exePath);
        if (!version.startsWith("1.")) {
            Log.verbose("Non-classic client!");
            return false;
        }
        Log.verbose("Path valid!");
        return true;
    }

    public String getInstallationLogPath(Addon addon) {
        return "data/managed/" + addon.getName() + ".json";
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

    public static String specifyInstallLocation(UserInput userInput) {
        boolean validPath = false;
        String input = null;
        boolean proceed = userInput.askToProceedPrompt();
        if(!proceed){
            return null;
        }
        while (!validPath) {
            UserInputResponse response = userInput.getUserInput();
            input = response.getInput();
            if (response.isAbort()) {
                return null;
            }
            validPath = verifyInstallLocation(response.getInput());
        }
        return input + "\\Interface\\AddOns";
    }


}
