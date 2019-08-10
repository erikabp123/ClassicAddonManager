package GUI;

import HelperTools.Log;
import HelperTools.UserInput;
import javafx.scene.control.TextInputDialog;

import static AddonManagement.AddonManager.verifyInstallLocation;

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
        boolean validPath = verifyInstallLocation(input);
        if(!validPath){
            Log.log("It appears this path is incorrect! Please try again. If you believe this to be a bug, please report it.");
            return getUserInput();
        }
        return input + "\\Interface\\AddOns";
    }
}
