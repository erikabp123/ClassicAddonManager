package com.CAM.GUI;

import com.CAM.HelperTools.UserInput;
import com.CAM.HelperTools.UserInputResponse;
import javafx.scene.control.TextInputDialog;

import java.util.Optional;

public class GUIUserInput implements UserInput {

    private String promptText;

    public GUIUserInput(String promptText){
        this.promptText = promptText;
    }

    @Override
    public UserInputResponse getUserInput() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText(promptText);
        Optional<String> result = dialog.showAndWait();
        if(result.isEmpty()){
            return new UserInputResponse(null, true);
        }
        String input = dialog.getEditor().getText();
        return new UserInputResponse(input, false);
    }
}
