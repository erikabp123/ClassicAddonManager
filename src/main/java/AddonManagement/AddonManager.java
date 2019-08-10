package AddonManagement;

import DataCollection.CurseForgeScraper;
import DataCollection.Scraper;
import HelperTools.Log;
import HelperTools.FileOperations;
import HelperTools.UserInput;
import com.google.gson.Gson;
import net.lingala.zip4j.model.FileHeader;

import java.io.*;
import java.util.*;

public class AddonManager {

    private String version = "1.0";
    private List<Addon> managedAddons;
    private String installLocation;

    public AddonManager(String installLocation){
        this.managedAddons = new ArrayList<>();
        this.installLocation = installLocation;
    }

    public void updateAddons(){
        Log.log("Updating addons ...");
        for(Addon addon : managedAddons){
            UpdateResponse response = addon.checkForUpdate();
            if(!response.isUpdateAvailable()){
                Log.verbose(addon.getName() + " by " + addon.getAuthor() + " is up to date!");
                continue;
            }
            Log.log("update available for: " + addon.getName() + " by " + addon.getAuthor() + "!");
            addon.fetchUpdate(response.getScraper());
            install(addon);
            saveToFile();
        }
        Log.log("Finished updating!");
    }

    public boolean addNewAddon(String origin){
        Log.log("Attempting to track new addon ...");

        for(Addon addon : managedAddons){
            if(!addon.getOrigin().equals(origin)){
                continue;
            }
            Log.log("AddonManagement.Addon already being tracked!");
            return false;
        }

        Scraper scraper = new CurseForgeScraper(origin);
        String name = scraper.getName();
        String author = scraper.getAuthor();
        Addon newAddon = new Addon(name, author, origin);

        managedAddons.add(newAddon);
        Collections.sort(managedAddons);
        saveToFile();

        Log.log("Successfully tracking new addon!");
        return true;
    }

    public boolean removeAddon(int addonNum){
        //TODO: Make it actually delete the folders
        Log.log("Attempting to remove addon ...");

        if(addonNum > managedAddons.size()){
            Log.log("AddonManagement.Addon #" + addonNum + " was not found!");
            return false;
        }

        uninstall(managedAddons.get(addonNum));
        managedAddons.remove(addonNum);

        saveToFile();

        Log.log("Successfully removed addon!");
        return true;
    }

    public static AddonManager initialize(UserInput userInput){
        Log.verbose("Looking for managed.json file ...");
        if(!new File("data/managed.json").isFile()){
            Log.verbose("No managed.json file found!");
            String installLocation = userInput.getUserInput();
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
            addonManager =  gson.fromJson(reader, AddonManager.class);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.verbose("Successfully loaded managed.json!");

        return addonManager;
    }

    public void saveToFile(){
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

    public void install(Addon addon){
        // Uninstall old save for clean updating
        Log.verbose("Attempting install of addon " + addon.getName() + " ...");
        uninstall(addon);
        List<FileHeader> headers = FileOperations.unzip("downloads/" + addon.getLastFileName(), installLocation);
        logInstallation(addon, headers);
        Log.verbose("Successfully installed addon!");
    }


    private void logInstallation(Addon addon, List<FileHeader> headers){
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

    private void uninstall(Addon addon){
        Log.verbose("Attempting uninstall of " + addon.getName() + " ...");
        Set<String> directories = readInstallationLog(addon);
        for(String directory : directories){
            String fullPath = installLocation + "\\" + directory;
            FileOperations.deleteDirectory(fullPath);
        }
        FileOperations.deleteFile(getInstallationLogPath(addon));
        Log.verbose("Finished uninstall!");
    }

    public Set<String> readInstallationLog(Addon addon){
        Log.verbose("Looking for addon installation log ...");

        String filePath = getInstallationLogPath(addon);

        if(!new File(filePath).isFile()){
            Log.verbose("No installation log found!");
            return new HashSet<>();
        }
        Set<String> directories = null;

        Log.verbose("Loading installation log ...");

        try {
            Reader reader = new FileReader(filePath);
            Gson gson = new Gson();
            directories =  gson.fromJson(reader, HashSet.class);
            reader.close();
            Log.verbose("Directories: " + directories);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.verbose("Successfully loaded managed.json!");

        return directories;
    }



    public List<Addon> getManagedAddons(){
        return managedAddons;
    }


    //HELPER METHODS

    public static boolean verifyInstallLocation(String path){
        Log.verbose("Checking supplied path ...");
        String exePath = path + "\\Wow.exe";
        if(!(new File(exePath).exists())){
            Log.verbose("Wow.exe not found!");
            return false;
        }
        String version = FileOperations.getFileVersion(exePath);
        if(!version.startsWith("1.")){
            Log.verbose("Non-classic client!");
            return false;
        }
        Log.verbose("Path valid!");
        return true;
    }

    public String getInstallationLogPath(Addon addon){
        return  "data/managed/"+ addon.getName() + ".json";
    }

    private Set<String> getOnlyRootDirectories(List<FileHeader> headers){
        Set<String> rootDirectories = new HashSet<>();

        for(FileHeader header : headers){
            String[] headerInfo = header.getFileName().split("/");
            rootDirectories.add(headerInfo[0]);
        }
        return rootDirectories;
    }


}
