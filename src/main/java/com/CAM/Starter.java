package com.CAM;

import com.CAM.HelperTools.Log;

public class Starter {

    public static void main(String[] args) {
        Log.toggleLogging();
        com.CAM.GUI.Main.begin(args);
    }


}
