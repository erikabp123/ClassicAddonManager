package com.CAM.Updating;

import com.CAM.DataCollection.FileDownloader;
import com.CAM.DataCollection.GitHubScraper;
import com.CAM.DataCollection.ScrapeException;
import com.CAM.GUI.Controller;
import com.CAM.HelperTools.Log;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Optional;

public class SelfUpdater {

    public static final String REPO_LOCATION = "https://github.com/erikabp123/ClassicAddonManager";
    public static final String AUTOUPDATER_REPO_LOCATION = "https://github.com/erikabp123/AutoUpdater";
    public static final String EXE_REPO_LOCATION = "";
    public static final int SLEEP_TIMER = 1000;

    public static void selfUpdate(Controller controller) throws ScrapeException {
        Log.log("Running self updater ...");
        GitHubScraper scraper = new GitHubScraper(REPO_LOCATION, null, true, true);
        HashMap<String, String> filesToDownload = determineDownloads(scraper);
        if(filesToDownload.isEmpty()){
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
                    fetchNewVersion(filesToDownload);
                    moveAutoUpdater();
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

    public static void moveAutoUpdater() {
        try {
            moveFile("downloads/AutoUpdater.jar", "system/AutoUpdater.jar");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static HashMap<String, String> determineDownloads(GitHubScraper scraper) throws ScrapeException {
        HashMap<String, String> filesToDownload = new HashMap<>();
        VersionInfo versionInfo = VersionInfo.readVersioningFile();
        boolean newTag = getTag(scraper) > VersionInfo.CAM_VERSION;
        boolean forceExtras = (versionInfo == null);
        if(versionInfo == null || newTag){
            String manifestLink = scraper.getUpdateManifestLink();
            FileDownloader downloader = new FileDownloader("system");
            downloader.downloadFile(manifestLink, "VERSIONING");
            versionInfo = VersionInfo.readVersioningFile();
        }
        GitHubScraper updaterScraper = new GitHubScraper(AUTOUPDATER_REPO_LOCATION, null, true, true);
        if(versionInfo.expectedCAM > VersionInfo.CAM_VERSION){
            String camLink = scraper.getReleaseJarDownload();
            filesToDownload.put(camLink, "ClassicAddonManager.jar");
        }
        if(forceExtras || versionInfo.expectedAutoUpdate > VersionInfo.AUTOUPDATER_VERSION){
            String jarLink = updaterScraper.getReleaseJarDownload();
            filesToDownload.put(jarLink, "AutoUpdater.jar");
        }
        if(forceExtras || versionInfo.expectedExe > VersionInfo.EXE_VERSION){
            String exeLink = updaterScraper.getReleaseExeDownload();
            filesToDownload.put(exeLink, "Classic Addon Manager.exe");
        }
        return filesToDownload;
    }

    private static void fetchNewVersion(HashMap<String, String> filesToDownload) {
        Log.verbose("Attempting to download new release ...");
        FileDownloader downloader = new FileDownloader("downloads");
        downloader.downloadMultipleFilesMonitored(filesToDownload);
        Log.verbose("Successfully downloaded new release!");
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

    private static void moveFile(String curPath, String newPath) throws IOException {
        File managerJar = new File(curPath);
        if(managerJar.exists()){
            Files.move(Paths.get(curPath), Paths.get(newPath), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static double getTag(GitHubScraper scraper) throws ScrapeException {
        String cleaned = scraper.getTag().replace("v", "");
        double tagAsNum = Double.parseDouble(cleaned);
        return tagAsNum;
    }


}
