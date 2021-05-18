package com.CAM.GUI;

import com.CAM.AddonManagement.AddonManager;
import com.CAM.HelperTools.ArgumentPasser;
import com.CAM.HelperTools.GameSpecific.GameVersion;
import com.CAM.HelperTools.IO.FileOperations;
import com.CAM.HelperTools.IO.ReadWriteClassFiles;
import com.CAM.HelperTools.Logging.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class AddonManagerControl {

    private HashMap<GameVersion, AddonManager> managers;
    private GameVersion activeManager;
    private ArrayList<GameVersion> managedGames;

    public AddonManagerControl(HashMap<GameVersion, AddonManager> managers){
        this.managers = managers;
        if(managers.keySet().isEmpty()) return;
        activeManager = managers.keySet().iterator().next();
    }

    public static void convertFromOldFormat() {
        Log.verbose("Loading managed.json ...");
        AddonManager addonManager = (AddonManager) ReadWriteClassFiles.readFile("data/managed.json", new AddonManager(null, null));
        Log.verbose("Successfully loaded managed.json!");
        addonManager.setVersion("3.0");
        addonManager.setGameVersion(GameVersion.CLASSIC);
        addonManager.saveToFile();
        addonManager.setInstallLocation(addonManager.getInstallLocation() + "\\");
        HashMap<GameVersion, AddonManager> hashMap = new HashMap<>();
        hashMap.put(GameVersion.CLASSIC, addonManager);
        AddonManagerControl amc = new AddonManagerControl(hashMap);
        FileOperations.moveFile("data/managed/", "data/" + GameVersion.CLASSIC + "/managed/");
        amc.saveToFile();
    }

    public HashMap<GameVersion, AddonManager> getManagers(){
        return managers;
    }

    public void addManagedGame(GameVersion gameVersion, AddonManager manager){
        this.managers.put(gameVersion, manager);
        saveToFile();
    }

    public void setActiveManager(GameVersion gameVersion){
        this.activeManager = gameVersion;
        saveToFile();
    }

    public AddonManager getActiveManager(){
        return managers.get(activeManager);
    }

    public static AddonManagerControl loadFromFile(){
        Log.verbose("Looking for managedGames.json file ...");
        String managedPath = "data/managedGames.json";

        boolean existingManagedList = new File(managedPath).isFile();
        if (!existingManagedList) {
            Log.verbose("No managedGames.json file found!");
            return null;
        }

        Log.verbose("Loading managedGames.json ...");
        AddonManagerControl amc = (AddonManagerControl) ReadWriteClassFiles.readFile(managedPath, new AddonManagerControl(new HashMap<>()));
        Log.verbose("Successfully loaded managedGames.json!");

        amc.managers = new HashMap<>();
        for(GameVersion gv: amc.managedGames){
            amc.managers.put(gv, AddonManager.loadManagerFromFile(gv));
        }
        amc.managedGames = null;

        return amc;
    }

    public void saveToFile(){
        managedGames = new ArrayList<>(managers.keySet());
        HashMap<GameVersion, AddonManager> tempManagers = managers;
        managers = null;
        ReadWriteClassFiles.saveFile("data/managedGames.json", this);
        managedGames = null;
        managers = tempManagers;
    }

    public Set<GameVersion> getManagedGames() {
        return managers.keySet();
    }

    public static boolean noCurrentSetup(){
        String path = "data/managedGames.json";
        File file = new File(path);
        return !file.exists();
    }

    public static boolean noPreviousSetup(){
        String path = "data/managed.json";
        File file = new File(path);
        return !file.exists();
    }

    public static void selectInstallations(HashMap<GameVersion, AddonManager> managers){
        Window window = new Window("selectAddonFolder.fxml", "Select Addon Installation Folder");
        window.initDialog(new Object[]{managers});
        window.showAndWait();
    }

    public static void selectInstallations(ArgumentPasser argumentPasser){
        Window window = new Window("selectAddonFolder.fxml", "Select Addon Installation Folder");
        window.initDialog(new Object[]{argumentPasser});
        window.showAndWait();
    }


}
