package com.CAM.Updating;

import com.CAM.DataCollection.Github.CAMGithubAPI;
import com.CAM.DataCollection.FileDownloader;
import com.CAM.DataCollection.Github.GitHubAPI;
import com.CAM.DataCollection.DataCollectionException;
import com.CAM.GUI.Controller;
import com.CAM.HelperTools.Logging.Log;
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

    public static final boolean testing = false;

    public static final String REPO_LOCATION = "https://github.com/erikabp123/ClassicAddonManager";
    public static final String TEST_REPO_LOCATION = "https://github.com/erikabp123/CAM_TESTER";
    public static final String AUTOUPDATER_REPO_LOCATION = "https://github.com/erikabp123/AutoUpdater";
    public static final String EXE_REPO_LOCATION = "https://github.com/erikabp123/CAM_EXE";
    public static boolean JAVA_INSTALLED = false;
    public static final int MIN_JAVA_VERSION = 8;
    public static final int SLEEP_TIMER = 1000;

    public static String getRepoLocation(){
        if(testing){
            return TEST_REPO_LOCATION;
        }
        return REPO_LOCATION;
    }

    public static void selfUpdate(Controller controller) throws DataCollectionException {
        Log.log("Running self updater ...");
        CAMGithubAPI scraper = new CAMGithubAPI(getRepoLocation());
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
            Log.printStackTrace(e);
        }
    }

    private static HashMap<String, String> determineDownloads(CAMGithubAPI scraper) throws DataCollectionException {
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
        if(versionInfo.expectedCAM > VersionInfo.CAM_VERSION){
            String camLink = scraper.getReleaseJarDownload();
            filesToDownload.put(camLink, "ClassicAddonManager.jar");
            String changelogLink = scraper.getChangelogDownload();
            filesToDownload.put(changelogLink, "CHANGELOG.txt");
        }
        CAMGithubAPI updaterScraper = new CAMGithubAPI(AUTOUPDATER_REPO_LOCATION);
        if(forceExtras || versionInfo.expectedAutoUpdate > VersionInfo.AUTOUPDATER_VERSION){
            String jarLink = updaterScraper.getReleaseJarDownload();
            filesToDownload.put(jarLink, "AutoUpdater.jar");
        }
        CAMGithubAPI exeScraper = new CAMGithubAPI(EXE_REPO_LOCATION);
        if(forceExtras || versionInfo.expectedExe > VersionInfo.EXE_VERSION){
            String exeLink = exeScraper.getReleaseExeDownload();
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
        String[] commands = {"cmd.exe", "/c", "start " + " java -jar \"" + pathToAutoUpdate + "\" " + SLEEP_TIMER + " " + JAVA_INSTALLED};
        String fullCommand = "cmd /c start " + "\"" + pathToElevate + "\" \"" + pathToJava + " \"" + pathToAutoUpdate + "\" " + SLEEP_TIMER + " " + JAVA_INSTALLED;

        try {
            if(JAVA_INSTALLED){
                Runtime.getRuntime().exec(commands);
            } else {
                Runtime.getRuntime().exec(fullCommand);
            }

        } catch (IOException e) {
            Log.printStackTrace(e);
        }
    }

    private static void moveFile(String curPath, String newPath) throws IOException {
        File managerJar = new File(curPath);
        if(managerJar.exists()){
            Files.move(Paths.get(curPath), Paths.get(newPath), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static double getTag(GitHubAPI scraper) throws DataCollectionException {
        String cleaned = scraper.getTag().replace("v", "");
        double tagAsNum = Double.parseDouble(cleaned);
        return tagAsNum;
    }


}
