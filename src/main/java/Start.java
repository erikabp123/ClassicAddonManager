import HelperTools.FileOperations;
import HelperTools.Log;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Start {

    public static boolean running = true;

    public static void main(String[] args){
        //TODO: Remove this line when releasing, only here for debugging
        //Log.logging = true;

        AddonManager manager = AddonManager.initialize();
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
                if(commands.length != 2){
                    System.out.println("Incorrect amount of parameters!");
                }
                manager.addNewAddon(commands[1]);
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
