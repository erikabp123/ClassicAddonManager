package com.CAM.GUI;

import com.CAM.AddonManagement.AddonManager;
import com.CAM.HelperTools.UserInput;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{

        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("GUI.fxml"));

        Stage stage = new Stage();
        stage.setTitle("WoW Classic Addon Manager");
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(getClass().getClassLoader().getResource("bootstrap3.css").toExternalForm());
        stage.setScene(scene);


        UserInput userInput = new ToastUserInput("Please provide path to WoW Classic Installation");

        AddonManager addonManager = AddonManager.initialize(userInput);
        Controller controller = loader.getController();
        controller.setAddonManager(addonManager);
        controller.updateListView();

        stage.show();




        //Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("com.CAM.GUI.fxml"));


        //primaryStage.setTitle("WoW Classic Addon Manager");
        //primaryStage.setScene(new Scene(root, 758, 433));

        //primaryStage.show();
    }


    public static void begin(String[] args) {
        launch(args);
    }
}
