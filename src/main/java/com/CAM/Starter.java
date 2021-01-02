package com.CAM;

import com.CAM.Updating.SelfUpdater;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Starter {

    public static boolean showWhatsNew = false;

    public static void main(String[] args) throws Exception {

        if(args.length > 0 && args[0].equals("updated")){
            showWhatsNew = true;
        }

        InstanceLock instanceLock = new InstanceLock("ClassicAddonManager");
        if (instanceLock.isAppActive()) {
            System.out.println("Already active.");
            System.exit(1);
        }

        SelfUpdater.JAVA_INSTALLED = isJavaInstalled();

        com.CAM.GUI.Main.begin(args);


    }

    private static boolean isJavaInstalled() throws IOException {
        String[] commands = {"cmd.exe", "/c", " java -version & exit"};
        Process p = Runtime.getRuntime().exec(commands);
        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        String outputLine = input.readLine();
        String errorLine = error.readLine();

        if(errorLine == null) return false;

        try {
            String versionText = errorLine.split(" ")[2].replace("\"", "");
            int versionNum = Integer.parseInt(versionText.split("\\.")[1]);
            return versionNum >= SelfUpdater.MIN_JAVA_VERSION;
        } catch (Exception e) {
            return false;
        }

    }

}