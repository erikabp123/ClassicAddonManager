package com.CAM.GUI;

import com.CAM.AddonManagement.AddonManager;
import com.CAM.HelperTools.FileOperations;
import com.CAM.HelperTools.GameVersion;
import com.CAM.HelperTools.Log;
import com.CAM.HelperTools.ReadWriteClassFiles;

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

    public void setActiveManager(GameVersion gameVersion){
        this.activeManager = gameVersion;
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


}
