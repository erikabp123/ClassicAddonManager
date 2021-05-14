package com.CAM.GUI;

import com.CAM.AddonManagement.AddonManager;
import com.CAM.HelperTools.ArgumentPasser;
import com.CAM.HelperTools.GameSpecific.GameVersion;
import com.CAM.HelperTools.IO.FileOperations;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.HashMap;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{

        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("GUI_v2.fxml"));

        Stage stage = new Stage();
        stage.getIcons().add(new Image(getClass().getClassLoader().getResource("program_icon.png").toExternalForm()));
        stage.setTitle("WoW Classic Addon Manager");
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(getClass().getClassLoader().getResource("bootstrap3.css").toExternalForm());
        //scene.getStylesheets().add(getClass().getClassLoader().getResource("invertedBootstrap3.css").toExternalForm());
        stage.setScene(scene);

        GUIUserInput.initBaseContext(primaryStage);

        HashMap<GameVersion, AddonManager> managers = new HashMap<>();
        AddonManagerControl amc;
        boolean updateInstallLocation = false;

        if(AddonManagerControl.noCurrentSetup()){
            if(AddonManagerControl.noPreviousSetup()){
                AddonManagerControl.selectInstallations(managers);
                if(managers.isEmpty()) return;
                amc = new AddonManagerControl(managers);
                amc.saveToFile();
            } else {
                AddonManagerControl.convertFromOldFormat();
                FileOperations.deleteFile("/data/managed.json");
                amc = load(managers);
                updateInstallLocation = true;
            }

        } else {
            amc = load(managers);
        }

        if(managers.keySet().isEmpty()){
            return;
        }
        Controller controller = loader.getController();
        controller.setAddonManagerControl(amc);
        controller.updateActiveManager(amc.getActiveManager().getGameVersion());
        controller.setupTableView();
        controller.setupSearchedAddonsTableView();
        controller.setupListeners();

        if(updateInstallLocation){
            ArgumentPasser argumentPasser = new ArgumentPasser();
            AddonManagerControl.selectInstallations(argumentPasser);
            if(argumentPasser.getReturnArguments() != null && argumentPasser.getReturnArguments()[0].equals("Cancelled")) return;
        }


        Thread updateAddonFormatThread = new Thread(() -> controller.updateManagedListToLatestFormat());
        updateAddonFormatThread.start();
        Thread updateThread = new Thread(() -> controller.checkForUpdate());
        updateThread.start();

        stage.show();
        controller.setupAutoCompletionListener();
    }

    public static AddonManagerControl load(HashMap<GameVersion, AddonManager> managers){
        AddonManagerControl amc = AddonManagerControl.loadFromFile();
        if(amc == null) return null;

        for(GameVersion gv: GameVersion.values()){
            AddonManager manager = AddonManager.loadManagerFromFile(gv);
            if(manager == null) continue;
            managers.put(gv, manager);
        }
        return amc;
    }


    public static void begin(String[] args) {
        launch(args);
    }
}
