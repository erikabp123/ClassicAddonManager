package com.CAM.HelperTools;

import com.CAM.DataCollection.FileDownloader;
import com.CAM.DataCollection.GitHubScraper;
import com.CAM.GUI.Controller;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.IOException;
import java.util.Optional;

public class SelfUpdater {

    public static final String REPO_LOCATION = "https://github.com/erikabp123/ClassicAddonManager";
    public static final double VERSION = 0.42;
    public static final int SLEEP_TIMER = 1000;

    public static void selfUpdate(Controller controller){
        Log.log("Running self updater ...");
        String downloadLink = checkForNewRelease();
        if(downloadLink == null){
            Log.log("Self Updater finished without finding any new updates!");
            return;
        }
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Update Available");
            alert.setHeaderText("A new update is available!");
            alert.setContentText("Do you wish to download this update now?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK){
                Platform.runLater(() -> {
                    controller.hideForUpdate();
                });
                Thread downloadThread = new Thread(() -> {
                    String path = fetchNewVersion(downloadLink);
                    Platform.runLater(() -> {
                        Alert alert1 = new Alert(Alert.AlertType.WARNING);
                        alert1.setTitle("Update Shutdown");
                        alert1.setHeaderText("Update requires restart");
                        alert1.setContentText("To apply the new update, Classic Addon Manager will now shutdown!");
                        alert1.showAndWait();
                        Thread installerThread = new Thread(() -> launcherUpdateInstaller());
                        installerThread.start();
                        Platform.exit();
                    });
                });
                downloadThread.start();
            }
        });

    }

    private static String fetchNewVersion(String url) {
        Log.verbose("Attempting to download new release ...");
        FileDownloader downloader = new FileDownloader("downloads");
        String fileName = "ClassicAddonManager.jar";
        downloader.downloadFileMonitored(url, fileName);
        Log.verbose("Successfully downloaded new release!");
        return "downloads/" + fileName;
    }

    private static void launcherUpdateInstaller() {
        String curDir = System.getProperty("user.dir");
        String pathToElevate = curDir + "\\" + "system\\Elevate.exe";
        String pathToJava = curDir + "\\system\\jdk-12.0.2\\bin\\javaw.exe\" -jar";
        String pathToAutoUpdate = curDir + "\\system\\AutoUpdater.jar";
        String fullCommand = "cmd /c start " + "\"" + pathToElevate + "\" \"" + pathToJava + " \"" + pathToAutoUpdate + "\" " + SLEEP_TIMER;
        try {
            Runtime.getRuntime().exec(fullCommand);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String checkForNewRelease(){
        Log.log("Checking for new release ...");
        //check github for new release, same method as checking for releases of addon
        // if new version available, return download link as string, else return null
        GitHubScraper scraper = new GitHubScraper(REPO_LOCATION, null, true);
        String cleaned = scraper.getTag().replace("v", "");
        double tagAsNum = Double.parseDouble(cleaned);
        if(tagAsNum <= VERSION){
            Log.log("No new release available!");
            return null;
        }
        Log.log("Found new release!");
        return scraper.getReleaseJarDownload();
    }


}
