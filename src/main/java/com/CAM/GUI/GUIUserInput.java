package com.CAM.GUI;

import com.CAM.HelperTools.Logging.Log;
import com.CAM.HelperTools.UserInput;
import com.CAM.HelperTools.UserInputResponse;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class GUIUserInput implements UserInput {


    private static GUIUserInput baseContext;

    private Window ownerWindow;

    public GUIUserInput(Window ownerWindow){
        this.ownerWindow = ownerWindow;
    }

    public static void initBaseContext(Window ownerWindow){
        baseContext = new GUIUserInput(ownerWindow);
    }

    public static GUIUserInput getBaseContext(){
        return baseContext;
    }

    @Override
    public UserInputResponse getUserInput(String title) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(title);
        File selectedDirectory = chooser.showDialog(ownerWindow);

        if (selectedDirectory == null) {
            return new UserInputResponse(null, true);
        }
        String path = null;
        try {
            path = selectedDirectory.getCanonicalPath();
        } catch (IOException e) {
            Log.printStackTrace(e);
        }
        return new UserInputResponse(path, false);
    }

    @Override
    public boolean askToProceedPrompt(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() != ButtonType.OK){
            return false;
        }
        return true;
    }
}
