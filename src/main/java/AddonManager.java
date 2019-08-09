import DataCollection.CurseForgeScraper;
import DataCollection.Scraper;
import HelperTools.Log;
import HelperTools.Zipping;
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
        System.out.println("Updating addons ...");
        for(Addon addon : managedAddons){
            UpdateResponse response = addon.checkForUpdate();
            if(!response.isUpdateAvailable()){
                System.out.println(addon.getName() + " by " + addon.getAuthor() + " is up to date!");
                continue;
            }
            System.out.println("update available for: " + addon.getName() + " by " + addon.getAuthor() + "!");
            addon.fetchUpdate(response.getScraper());
            install(addon);
            saveToFile();
        }
        System.out.println("Finished updating!");
    }

    public boolean addNewAddon(String origin){
        Log.log("Attempting to track new addon ...");

        for(Addon addon : managedAddons){
            if(!addon.getOrigin().equals(origin)){
                continue;
            }
            Log.log("Addon already being tracked!");
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
            Log.log("Addon #" + addonNum + " was not found!");
            return false;
        }

        managedAddons.remove(addonNum - 1);
        saveToFile();

        Log.log("Successfully removed addon!");
        return true;
    }

    public static AddonManager initialize(){
        Log.log("Looking for managed.json file ...");
        if(!new File("data/managed.json").isFile()){
            Log.log("No managed.json file found!");
            String installLocation = requireSetup();
            AddonManager manager = new AddonManager(installLocation);
            manager.saveToFile();
            System.out.println("Setup complete!");
            return manager;
        }
        AddonManager addonManager = null;

        Log.log("Loading managed.json ...");

        try {
            Reader reader = new FileReader("data/managed.json");
            Gson gson = new Gson();
            addonManager =  gson.fromJson(reader, AddonManager.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Log.log("Successfully loaded managed.json!");

        return addonManager;
    }

    public static String requireSetup(){
        //TODO: Verify this is indeed the classic wow installation folder
        Scanner in = new Scanner(System.in);
        System.out.println("|------------------------|");
        System.out.println("|######## SETUP #########|");
        System.out.println("Please provide path to WoW Classic installation:");
        System.out.print(">");
        String input = in.nextLine();
        return input;
    }

    public void saveToFile(){
        Log.log("Attempting to save to managed.json ...");
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
        Log.log("Successfully saved to managed.json!");
    }

    public void install(Addon addon){
        //TODO: make this uninstall first, then install. Leads to clean updating
        List<FileHeader> headers = Zipping.unzip("downloads/" + addon.getLastFileName(), installLocation);
        logInstallation(addon, headers);
    }



    private void logInstallation(Addon addon, List<FileHeader> headers){
        Log.log("Attempting to log installation ...");

        try {
            Gson gson = new Gson();
            File file = new File("data/managed/" + addon.getName() + ".json");
            file.getParentFile().mkdirs();
            Writer writer = new FileWriter(file);
            List<String> rootDirectories = getOnlyRootDirectories(headers);
            gson.toJson(rootDirectories, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.log("Successfully logged installation!");
    }

    public void uninstall(Addon addon){
        //TODO: Make this actually remove the directories mentioned in data/managed/ADDON_NAME.json
    }



    public List<Addon> getManagedAddons(){
        return managedAddons;
    }


    //HELPER METHODS

    private List<String> getOnlyRootDirectories(List<FileHeader> headers){
        List<String> rootDirectories = new ArrayList<>();
        for(FileHeader header : headers){
            if(!header.isDirectory()){
                continue;
            }
            String[] headerInfo = header.getFileName().split("/");
            if(headerInfo.length > 1){
                continue;
            }
            rootDirectories.add(header.getFileName());
        }
        return rootDirectories;
    }


}
