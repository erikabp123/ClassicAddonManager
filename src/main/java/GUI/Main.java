package GUI;

import AddonManagement.AddonManager;
import HelperTools.UserInput;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{

        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("GUI.fxml"));

        Stage stage = new Stage();
        stage.setTitle("WoW Classic Addon Manager");
        stage.setScene(new Scene(loader.load()));

        UserInput userInput = new ToastUserInput("Please provide path to WoW Classic Installation");

        AddonManager addonManager = AddonManager.initialize(userInput);
        Controller controller = loader.getController();
        controller.setAddonManager(addonManager);
        controller.updateListView();

        stage.show();




        //Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("GUI.fxml"));


        //primaryStage.setTitle("WoW Classic Addon Manager");
        //primaryStage.setScene(new Scene(root, 758, 433));

        //primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
