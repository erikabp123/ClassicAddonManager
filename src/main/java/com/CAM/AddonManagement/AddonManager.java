package com.CAM.AddonManagement;

import com.CAM.DataCollection.*;
import com.CAM.GUI.GUIUserInput;
import com.CAM.GUI.Window;
import com.CAM.HelperTools.*;
import com.CAM.Settings.SessionOnlySettings;
import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import net.lingala.zip4j.model.FileHeader;

import java.io.*;
import java.util.*;

public class AddonManager {

    private static final String DOWNLOAD_LOCATION = "downloads/";
    private static final String DATA_LOCATION = "data/";
    private static final String MANAGED_LOCATION = "data/";


    private String version = "3.0";
    private List<Addon> managedAddons;
    private String installLocation;
    private GameVersion gameVersion;

    public AddonManager(String installLocation, GameVersion gameVersion) {
        this.managedAddons = new ArrayList<>();
        this.installLocation = installLocation;
        this.gameVersion = gameVersion;
    }

    public GameVersion getGameVersion(){
        return gameVersion;
    }

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
                        e.printStackTrace();
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
                UpdateResponse response = addon.checkForUpdate();
                if (!response.isUpdateAvailable() && !SessionOnlySettings.isForceReDownloads()) {
                    Log.verbose(addon.getName() + " by " + addon.getAuthor() + " is up to date!");
                    statusCode = 1;
                    listener.informFinish(position, statusCode);
                    position++;
                    addon.setLastUpdateCheck(new Date());
                    saveToFile();
                    continue;
                }
                Log.log("update available for: " + addon.getName() + " by " + addon.getAuthor() + "!");
                addon.fetchUpdate(response.getRetriever());
                install(addon);
            } catch (ScrapeException e) {
                exceptions.add(e);
                statusCode = 0;
            } catch (Exception e) {
                exceptions.add(new ScrapeException(addon, e));
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

    public void updateToLatestFormat(UpdateListener listener) throws ScrapeException {
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
                    e.printStackTrace();
                }
            }
        }
    }

    private int getSleepDelay() {
        int DELAY_RANGE = 401; // range is 300-700, 101 instead of 100 since 600 is included
        int MIN_DELAY = 300;
        return (new Random()).nextInt(DELAY_RANGE) + MIN_DELAY;
    }

    public boolean addNewAddon(AddonRequest request) throws ScrapeException {
        Log.log("Attempting to track new addon ...");

        UrlInfo urlInfo = UrlInfo.examineAddonUrl(request.origin);
        if (!urlInfo.isValid) {
            Log.log("Could not track addon!");
            return false;
        }
        String trimmedOrigin = UrlInfo.trimString(request.origin, urlInfo.addonSource);

        for (Addon addon : managedAddons) {
            if (!addon.getOrigin().equals(trimmedOrigin)) {
                continue;
            }
            Log.log(addon.getName() + " already being tracked!");
            return false;
        }

        AddonInfoRetriever retriever = UrlInfo.getCorrespondingInfoRetriever(urlInfo.addonSource, trimmedOrigin, false, request.branch, request.releases, -1);

        String name = retriever.getName();
        String author = retriever.getAuthor();
        Addon newAddon = new Addon(name, author, trimmedOrigin, request.branch, request.releases);

        managedAddons.add(newAddon);
        Collections.sort(managedAddons);
        saveToFile();

        Log.log("Successfully tracking new addon!");
        return true;
    }

    public boolean addNewSearchedAddon(SearchedAddonRequest request) throws ScrapeException {
        Log.log("Attempting to track new addon ...");

        int projectId = request.getProjectId();
        for (Addon addon : managedAddons) {
            if (addon.getProjectId() == projectId) {
                Log.log(addon.getName() + " already being tracked!");
                return false;
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
        return true;
    }

    public boolean removeAddon(int addonNum) {
        Log.log("Attempting to remove addon ...");

        if (addonNum > managedAddons.size()) {
            Log.log("Addon #" + addonNum + " was not found!");
            return false;
        }

        uninstall(managedAddons.get(addonNum));
        managedAddons.remove(addonNum);

        saveToFile();

        Log.log("Successfully removed addon!");
        return true;
    }

    public static boolean noPreviousSetup(){
        for(GameVersion gv: GameVersion.values()){
            String path = "data/" + gv + "/managed.json";
            File file = new File(path);
            if(file.exists()) return false;
        }
        return true;
    }

    public static void selectInstallations(HashMap<GameVersion, AddonManager> managers){
        Window window = new Window("selectAddonFolder.fxml", "Select Addon Installation Folder");
        window.initDialog(new Object[]{managers});
        window.showAndWait();
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
            String fullPathToDirectory = installLocation + "\\" + directory;
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

    public static boolean verifyInstallLocation(String path, GameVersion gameVersion) {
        Log.verbose("Checking supplied path ...");

        String exeName = gameVersion.getExeName();
        String prefix = gameVersion.getPrefix();


        String exePath = path + exeName;
        if (!(new File(exePath).exists())) {
            Log.verbose(exeName + " not found!");
            return false;
        }
        System.out.println("path: " + exePath);
        String version = FileOperations.getFileVersion(exePath);
        System.out.println("Version: " + version);


        if (!version.startsWith(prefix)) {
            Log.verbose("Invalid game version!");
            return false;
        }
        Log.verbose("Path valid!");
        return true;
    }

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

    public static String specifyInstallLocation(GameVersion gameVersion) {
        boolean validPath = false;
        String input = null;
        UserInput userInput = GUIUserInput.getBaseContext();
        
        String title, header, content;
        switch (gameVersion){
            case RETAIL:
                title = "Setup Install Path";
                header = "Please provide the path to your WoW installation!";
                content = "To proceed, Classic Addon Manager needs to know where WoW is installed. Do you wish to proceed?";
                break;
            case PTR_RETAIL:
                title = "Setup Install Path";
                header = "Please provide the path to your WoW PTR installation!";
                content = "To proceed, Classic Addon Manager needs to know where WoW PTR is installed. Do you wish to proceed?";
                break;
            case CLASSIC:
                title = "Setup Install Path";
                header = "Please provide the path to your WoW Classic installation!";
                content = "To proceed, Classic Addon Manager needs to know where WoW classic is installed. Do you wish to proceed?";
                break;
            case PTR_CLASSIC:
                title = "Setup Install Path";
                header = "Please provide the path to your WoW Classic PTR installation!";
                content = "To proceed, Classic Addon Manager needs to know where WoW classic PTR is installed. Do you wish to proceed?";;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + gameVersion);
        }
        
        boolean proceed = userInput.askToProceedPrompt(title, header, content);
        if (!proceed) {
            return null;
        }

        String directoryChooserTitle;
        switch (gameVersion){
            case RETAIL:
                directoryChooserTitle = "Navigate to the wow '_retail_' folder";
                break;
            case PTR_RETAIL:
                directoryChooserTitle = "Navigate to the wow '_ptr_' folder";
                break;
            case CLASSIC:
                directoryChooserTitle = "Navigate to the wow '_classic_' folder";
                break;
            case PTR_CLASSIC:
                directoryChooserTitle = "Navigate to the wow '_classic_ptr_' folder";
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + gameVersion);
        }

        while (!validPath) {
            UserInputResponse response = userInput.getUserInput(directoryChooserTitle);
            input = response.getInput();
            if (response.isAbort()) {
                return null;
            }
            System.out.println(response.getInput());
            validPath = verifyInstallLocation(response.getInput(), gameVersion);
            if(!validPath) showInvalidFolderAlert(gameVersion);
        }
        return input + "\\Interface\\AddOns";
    }

    private static void showInvalidFolderAlert(GameVersion gameVersion) {
        Platform.runLater(() -> {
            Alert exceptionAlert = new Alert(Alert.AlertType.WARNING);
            exceptionAlert.setHeaderText("Invalid folder!");
            exceptionAlert.setContentText("The supplied folder is invalid! Please select the folder containing the " + gameVersion.getExeName() + " file!");
            exceptionAlert.showAndWait();
        });
    }

    public static String scanForInstallLocation(GameVersion gameVersion) throws IOException {
        String exeName = gameVersion.getExeName();

        String[] commands = {"cmd.exe", "/c", "cd \\ & dir /s /b " + exeName + " & exit"};
        Process p = Runtime.getRuntime().exec(commands);
        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;

        while (true) {
            line = input.readLine();
            if (line == null) break;

            if (!line.contains("\\" + gameVersion.getPath() + "\\")) continue;
            String substring = line.substring(0, line.length() - exeName.length());
            if(verifyInstallLocation(substring, gameVersion)) return substring;
        }

        return null;
    }


}
