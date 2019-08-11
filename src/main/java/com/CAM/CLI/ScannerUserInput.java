package com.CAM.CLI;

import com.CAM.HelperTools.UserInput;
import java.util.Scanner;

public class ScannerUserInput implements UserInput {
    @Override
    public String getUserInput() {
        Scanner in = new Scanner(System.in);
        String input = in.nextLine();
        return input;
    }
}
