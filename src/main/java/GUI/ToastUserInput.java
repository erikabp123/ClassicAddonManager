package GUI;

import HelperTools.UserInput;
import javafx.scene.control.TextInputDialog;

public class ToastUserInput implements UserInput {

    private String promptText;

    public ToastUserInput(String promptText){
        this.promptText = promptText;
    }

    @Override
    public String getUserInput() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText(promptText);
        dialog.showAndWait();
        String input = dialog.getEditor().getText();
        return input;
    }
}
