package com.CAM.CLI;

import com.CAM.AddonManagement.Addon;
import com.CAM.AddonManagement.AddonManager;
import com.CAM.AddonManagement.AddonRequest;
import com.CAM.HelperTools.Log;
import com.CAM.HelperTools.UserInput;

import java.util.List;
import java.util.Scanner;

public class Start {

    public static boolean running = true;

    public static void main(String[] args){
        //TODO: Remove this line when releasing, only here for debugging
        //Log.logging = true;

        UserInput userInput = new ScannerUserInput();
        AddonManager manager = AddonManager.initialize(userInput);
        while(running){
            //TODO: Add a timer such that if no input is detected for X min it closes by itself
            readUserInput(manager);
        }


    }

    public static void readUserInput(AddonManager manager){
        //TODO: Maybe use enum for commands?
        Scanner in = new Scanner(System.in);
        System.out.println("|------------------------|");
        System.out.println("For a list of commands type help");
        System.out.print(">");
        String input = in.nextLine();
        String[] commands = input.split(" ");
        switch (commands[0]){
            case "add":
                if(commands.length < 2){
                    System.out.println("Incorrect amount of parameters!");
                }
                AddonRequest request = new AddonRequest();
                request.origin = commands[1];
                request.branch = commands[2];
                manager.addNewAddon(request);
                break;
            case "remove":
                manager.removeAddon(Integer.parseInt(commands[1]));
                break;
            case "update":
                manager.updateAddons();
                break;
            case "list":
                List<Addon> managedAddons = manager.getManagedAddons();
                System.out.println("Managing (" + managedAddons.size() + ") addons:");
                int i = 1;
                for(Addon addon : managedAddons){
                    System.out.println("<" + i +"> " + addon.getName());
                    i++;
                }
                break;
            case "debug":
                Log.logging = !Log.logging;
                System.out.println("Set logging to " + Log.logging);
                break;
            case "help":
                System.out.println("######## COMMANDS ########");
                System.out.println("add ADDON_URL\n" + "remove NUMBER\n" + "update\n" + "list\n" + "debug\n" + "quit");
                System.out.println("##########################");
                break;
            case "quit":
                running = false;
                break;
            default:
                System.out.println("Invalid input! For a list of commands type help");
                break;
        }
    }



}
