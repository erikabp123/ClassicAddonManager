package com.CAM.GUI;

import com.CAM.HelperTools.UserInput;
import com.CAM.HelperTools.UserInputResponse;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class GUIUserInput implements UserInput {

    private Window ownerWindow;
    private String promptText;

    public GUIUserInput(String promptText){
        this.promptText = promptText;
        this.ownerWindow = null;
    }

    public GUIUserInput(String promptText, Window ownerWindow){
        this.promptText = promptText;
        this.ownerWindow = ownerWindow;
    }



    @Override
    public UserInputResponse getUserInput() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Setup Install Path");
        alert.setHeaderText("Please provide path to WoW installation!");
        alert.setContentText("To proceed Classic Addon Manager needs to know where WoW classic is installed, do you wish to proceed?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() != ButtonType.OK){
            return new UserInputResponse(null, true);
        }


        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select path to classic wow installation");
        File selectedDirectory = directoryChooser.showDialog(ownerWindow);
        if (selectedDirectory == null) {
            return new UserInputResponse(null, true);
        }
        String path = null;
        try {
            path = selectedDirectory.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText(promptText);
        Optional<String> result = dialog.showAndWait();
        if(result.isEmpty()){
            return new UserInputResponse(null, true);
        }
        String input = dialog.getEditor().getText();
        return new UserInputResponse(input, false);
         */
        return new UserInputResponse(path, false);
    }
}
