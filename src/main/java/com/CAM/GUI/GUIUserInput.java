package com.CAM.GUI;

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
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Please select the WoW Classic 'WowClassic.exe' file");
        FileChooser.ExtensionFilter exeFilter = new FileChooser.ExtensionFilter("Exectuable files", "*.exe");
        fileChooser.getExtensionFilters().add(exeFilter);
        File selectedFile = fileChooser.showOpenDialog(ownerWindow);
        if (selectedFile == null) {
            return new UserInputResponse(null, true);
        }
        String path = null;
        try {
            path = selectedFile.getCanonicalPath();
            path = path.substring(0, path.lastIndexOf("\\"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new UserInputResponse(path, false);
    }

    @Override
    public boolean askToProceedPrompt() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Setup Install Path");
        alert.setHeaderText("Please provide the path to your WoW Classic 'WowClassic.exe' installation!");
        alert.setContentText("To proceed, Classic Addon Manager needs to know where WoW classic is installed. Do you wish to proceed?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() != ButtonType.OK){
            return false;
        }
        return true;
    }
}
