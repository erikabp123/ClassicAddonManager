package com.CAM;

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

        com.CAM.GUI.Main.begin(args);


    }

}