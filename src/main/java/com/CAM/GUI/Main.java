package com.CAM.GUI;

import com.CAM.AddonManagement.AddonManager;
import com.CAM.HelperTools.GameVersion;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.HashMap;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{

        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("GUI.fxml"));

        Stage stage = new Stage();
        stage.getIcons().add(new Image(getClass().getClassLoader().getResource("program_icon.png").toExternalForm()));
        stage.setTitle("WoW Classic Addon Manager");
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(getClass().getClassLoader().getResource("bootstrap3.css").toExternalForm());
        stage.setScene(scene);

        GUIUserInput.initBaseContext(primaryStage);

        HashMap<GameVersion, AddonManager> managers = new HashMap<>();
        AddonManagerControl amc;

        if(AddonManager.noPreviousSetup()){
            AddonManager.selectInstallations(managers);
            if(managers.isEmpty()) return;
            amc = new AddonManagerControl(managers);
            amc.saveToFile();
        } else {
            amc = AddonManagerControl.loadFromFile();
            if(amc == null) return;

            for(GameVersion gv: GameVersion.values()){
                AddonManager manager = AddonManager.loadManagerFromFile(gv);
                if(manager == null) continue;
                managers.put(gv, manager);
            }
        }


        //AddonManager addonManager = AddonManager.initialize(GameVersion.CLASSIC);

        if(managers.keySet().isEmpty()){
            return;
        }
        Controller controller = loader.getController();
        controller.setAddonManagerControl(amc);
        controller.updateListView();
        controller.setupListeners();
        Thread updateAddonFormatThread = new Thread(() -> controller.updateManagedListToLatestFormat());
        updateAddonFormatThread.start();
        Thread updateThread = new Thread(() -> controller.checkForUpdate());
        updateThread.start();

        stage.show();
    }


    public static void begin(String[] args) {
        launch(args);
    }
}
