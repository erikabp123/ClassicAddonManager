package CLI;

import HelperTools.Log;
import HelperTools.UserInput;
import java.util.Scanner;

public class ScannerUserInput implements UserInput {
    @Override
    public String getUserInput() {
        Scanner in = new Scanner(System.in);
        String input = in.nextLine();
        return input;
    }
}
