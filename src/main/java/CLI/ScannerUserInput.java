package CLI;

import HelperTools.Log;
import HelperTools.UserInput;
import java.util.Scanner;
import static AddonManagement.AddonManager.verifyInstallLocation;

public class ScannerUserInput implements UserInput {
    @Override
    public String getUserInput() {
        Scanner in = new Scanner(System.in);
        Log.log("|------------------------|");
        Log.log("|######## SETUP #########|");
        Log.log("Please provide path to WoW Classic installation:");
        System.out.print(">");
        String input = in.nextLine();
        boolean validPath = verifyInstallLocation(input);
        if(!validPath){
            Log.log("It appears this path is incorrect! Please try again. If you believe this to be a bug, please report it.");
            return getUserInput();
        }
        return input + "\\Interface\\AddOns";
    }
}
