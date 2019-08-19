package com.CAM;

public class Starter {

    public static void main(String[] args) throws Exception {
        InstanceLock instanceLock = new InstanceLock("ClassicAddonManager");
        if (instanceLock.isAppActive()) {
            System.out.println("Already active.");
            System.exit(1);
        }
        com.CAM.GUI.Main.begin(args);
    }

}