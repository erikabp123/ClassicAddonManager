import HelperTools.Log;
import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.ArrayList;

public class AddonManager {

    private String version = "1.0";
    private ArrayList<Addon> managedAddons;

    public AddonManager(){
        this.managedAddons = new ArrayList<>();
    }

    public void updateAddons(){
        for(Addon addon : managedAddons){
            UpdateResponse response = addon.checkForUpdate();
            if(!response.isUpdateAvailable()){
                continue;
            }
            System.out.println("Fetching update for: " + addon.getName() + "_" + addon.getAuthor());
            addon.fetchUpdate(response.getScraper());
            saveToFile();
        }
    }

    public void testPopulate(){
        managedAddons.add(new Addon("Galvin's UnitBars Classic", "galvinsr", "NA", null, "https://www.curseforge.com/wow/addons/galvins-unitbars-classic"));
        managedAddons.add(new Addon("Questie", "aerorocks99", "NA", null,  "https://www.curseforge.com/wow/addons/questie"));
    }

    public static AddonManager initialize(){
        if(!new File("data/managed.json").isFile()){
            Log.log("No managed.json file found!");
            return new AddonManager();
        }
        AddonManager addonManager = null;

        Log.log("Loading managed.json!");

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

    public void saveToFile(){
        Log.log("Saving to managed.json!");
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
        Log.log("Saved to managed.json!");
    }





}
