package com.CAM.GUI;

import com.CAM.AddonManagement.AddonManager;
import com.CAM.HelperTools.*;
import com.CAM.HelperTools.GameSpecific.GameVersion;
import com.CAM.HelperTools.IO.FileOperations;
import com.CAM.HelperTools.Logging.Log;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.CAM.GUI.GUISelectInstallationFolder.scanForInstallLocation;
import static com.CAM.GUI.GUISelectInstallationFolder.specifyInstallLocation;

public class GUISelectInstallationFolder implements Initializable, WindowController {

    @FXML
    private ImageView retailLocationImageView;

    @FXML
    private ImageView retailPtrLocationImageView;

    @FXML
    private ImageView classicLocationImageView;

    @FXML
    private ImageView classicPtrLocationImageView;

    @FXML
    private TextField retailLocationTextField;

    @FXML
    private TextField retailPtrLocationTextField;

    @FXML
    private TextField classicLocationTextField;

    @FXML
    private TextField classicPtrLocationTextField;

    @FXML
    private Button selectInstallationContinueButton;

    @FXML
    private Button cancelRetailSearchButton;

    @FXML
    private Button cancelRetailPtrSearchButton;

    @FXML
    private Button cancelClassicSearchButton;

    @FXML
    private Button cancelClassicPtrSearchButton;

    @FXML
    private Button retailLocationConfirmButton;

    @FXML
    private Button retailPtrLocationConfirmButton;

    @FXML
    private Button classicLocationConfirmButton;

    @FXML
    private Button classicPtrLocationConfirmButton;

    @FXML
    private Button retailManualSelectionButton;

    @FXML
    private Button retailPtrManualSelectionButton;

    @FXML
    private Button classicManualSelectionButton;

    @FXML
    private Button classicPtrManualSelectionButton;

    @FXML
    private void retailConfirmSelectionAction(){
        searches.get(GameVersion.RETAIL).confirmFolder(this);
    }

    @FXML
    private void retailPtrConfirmSelectionAction(){
        searches.get(GameVersion.PTR_RETAIL).confirmFolder(this);
    }

    @FXML
    private void classicConfirmSelectionAction(){
        searches.get(GameVersion.CLASSIC).confirmFolder(this);
    }

    @FXML
    private void classicPtrConfirmSelectionAction(){
        searches.get(GameVersion.PTR_CLASSIC).confirmFolder(this);
    }


    Image imageFailure = new Image(this.getClass().getClassLoader().getResource("failure.png").toExternalForm());
    HashMap<GameVersion, InstallationSearch> searches = new HashMap<>();
    HashMap<GameVersion, AddonManager> managers;
    AddonManagerControl amc;
    boolean firstTimeSetup;
    private ArgumentPasser closedButtonClicked;


    @FXML
    private void retailManualSelectionAction(){
        searches.get(GameVersion.RETAIL).manualSelection();
    }

    @FXML
    private void retailPtrManualSelectionAction(){
        searches.get(GameVersion.PTR_RETAIL).manualSelection();
    }

    @FXML
    private void classicManualSelectionAction(){
        searches.get(GameVersion.CLASSIC).manualSelection();
    }

    @FXML
    private void classicPtrManualSelectionAction(){
        searches.get(GameVersion.PTR_CLASSIC).manualSelection();
    }

    @FXML
    private void cancelRetailAction() {
        searches.get(GameVersion.RETAIL).cancelSearch();
    }

    @FXML
    private void cancelRetailPtrAction() {
        searches.get(GameVersion.PTR_RETAIL).cancelSearch();
    }

    @FXML
    private void cancelClassicAction() {
        searches.get(GameVersion.CLASSIC).cancelSearch();
    }

    @FXML
    private void cancelClassicPtrAction() {
        searches.get(GameVersion.PTR_CLASSIC).cancelSearch();
    }

    @FXML
    private void saveAction(ActionEvent event) {
        if(firstTimeSetup){
            for(GameVersion gv: searches.keySet()){
                InstallationSearch search = searches.get(gv);
                if(!search.isConfirmed()) continue;
                AddonManager manager = AddonManager.initializeFromScanUI(gv, search.getLocation());
                managers.put(gv, manager);
            }
        } else {
            for(GameVersion gv: searches.keySet()){
                InstallationSearch search = searches.get(gv);
                if(!search.isConfirmed()) continue;
                if(amc.getManagers().keySet().contains(gv)){
                    String location = search.getLocation() + "Interface\\AddOns\\";
                    amc.getManagers().get(gv).setInstallLocation(location);
                    continue;
                }
                AddonManager manager = AddonManager.initializeFromScanUI(gv, search.getLocation());
                amc.addManagedGame(gv, manager);
            }
            Controller.getInstance().updateManagedVersionChoiceBox();
            Controller.getInstance().updateSelectedManagedVersionChoiceBox();
        }

        closeStage(event);
    }

    @FXML
    private void cancelAction(ActionEvent event) {
        if(closedButtonClicked != null){
            closedButtonClicked.setReturnArguments(new Object[]{"Cancelled"});
        }
        closeStage(event);
    }

    private void closeStage(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }

    public void enableContinueButton() {
        selectInstallationContinueButton.setDisable(false);
    }

    private void scanForLocations(GameVersion[] versions) {
        for(GameVersion gv: versions){
            searches.put(gv, createInstallationSearch(gv));
        }
        for(GameVersion gv: searches.keySet()){
            Thread searchThread = new Thread(() -> searches.get(gv).startScan());
            searchThread.setDaemon(true);
            searchThread.start();
        }
    }

    private void scanForLocations() {
        scanForLocations(GameVersion.values());
    }

    private InstallationSearch createInstallationSearch(GameVersion gameVersion){
        InstallationSearch search = null;
        switch (gameVersion) {
            case RETAIL:
                search = new InstallationSearch(gameVersion,
                        retailLocationTextField,
                        retailLocationImageView,
                        cancelRetailSearchButton,
                        retailLocationConfirmButton,
                        retailManualSelectionButton);
                break;
            case PTR_RETAIL:
                search = new InstallationSearch(gameVersion,
                        retailPtrLocationTextField,
                        retailPtrLocationImageView,
                        cancelRetailPtrSearchButton,
                        retailPtrLocationConfirmButton,
                        retailPtrManualSelectionButton);
                break;
            case CLASSIC:
                search = new InstallationSearch(gameVersion,
                        classicLocationTextField,
                        classicLocationImageView,
                        cancelClassicSearchButton,
                        classicLocationConfirmButton,
                        classicManualSelectionButton);
                break;
            case PTR_CLASSIC:
                search = new InstallationSearch(gameVersion,
                        classicPtrLocationTextField,
                        classicPtrLocationImageView,
                        cancelClassicPtrSearchButton,
                        classicPtrLocationConfirmButton,
                        classicPtrManualSelectionButton);
                break;
        }
        return search;
    }

    @Override
    public void initDialog(Object[] args) {
        firstTimeSetup = (args[0] != null && !args[0].getClass().equals(ArgumentPasser.class));
        updateButtonGraphics();

        if(firstTimeSetup){
            managers = (HashMap<GameVersion, AddonManager>) args[0];
            initFirstTime();
        }
        else{
            if(args[0] != null && args[0].getClass().equals(ArgumentPasser.class)) setClosedButtonClicked((ArgumentPasser) args[0]);
            initFromPrevious();
        };

    }

    public void initFromPrevious(){
        amc = Controller.getInstance().getAddonManagerControl();
        HashMap<GameVersion, AddonManager> currentlyManaged = amc.getManagers();

        HashMap<GameVersion, AddonManager> validManager = new HashMap<>();
        HashSet<GameVersion> rescan = new HashSet<>(Arrays.asList(GameVersion.values().clone()));

        for(GameVersion gv: currentlyManaged.keySet()){
            String path = currentlyManaged.get(gv).getInstallLocation();
            path = path.replace("Interface\\AddOns\\", "");
            boolean validInstallation = verifyInstallLocation(path, gv);
            if(validInstallation) validManager.put(gv, currentlyManaged.get(gv));
        }

        rescan.removeAll(validManager.keySet());
        if(!rescan.isEmpty()) scanForLocations(rescan.toArray(new GameVersion[0]));
        if(!validManager.isEmpty()) markAsValidInstall(validManager);

    }

    private void markAsValidInstall(HashMap<GameVersion, AddonManager> versions) {
        for(GameVersion gv: versions.keySet()){
            searches.put(gv, createInstallationSearch(gv));
        }
        for(GameVersion gv: versions.keySet()){
            Platform.runLater(() -> {
                String path = versions.get(gv).getInstallLocation();
                path = path.replace("Interface\\AddOns\\", "");
                searches.get(gv).prefillSearch(path);
            });
        }
    }

    public void initFirstTime(){
        scanForLocations();
    }

    private void updateButtonGraphics() {
        ArrayList<Button> cancelSearchButtons = new ArrayList<>();
        cancelSearchButtons.add(cancelRetailSearchButton);
        cancelSearchButtons.add(cancelRetailPtrSearchButton);
        cancelSearchButtons.add(cancelClassicSearchButton);
        cancelSearchButtons.add(cancelClassicPtrSearchButton);

        for(Button button: cancelSearchButtons){
            ImageView view = new ImageView(imageFailure);
            button.setGraphic(view);
            view.setFitHeight(15);
            view.setFitWidth(15);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public static String specifyInstallLocation(GameVersion gameVersion) {
        boolean validPath = false;
        String input = null;
        UserInput userInput = GUIUserInput.getBaseContext();

        String title = "Setup Install Path";
        String header = "Please provide the path to your Wow " + gameVersion.getFormattedString() + " installation!";
        String content = "To proceed, Classic Addon Manager needs to know where WoW " + gameVersion.getFormattedString() + " is installed. Do you wish to proceed?";

        boolean proceed = userInput.askToProceedPrompt(title, header, content);
        if (!proceed) {
            return null;
        }

        String directoryChooserTitle = "Navigate to the wow '" + gameVersion.getPath() + "' folder";

        while (!validPath) {
            UserInputResponse response = userInput.getUserInput(directoryChooserTitle);
            input = response.getInput();
            if(!response.getInput().endsWith("\\")) input = input + "\\";
            if (response.isAbort()) {
                return null;
            }
            validPath = verifyInstallLocation(input, gameVersion);
            if(!validPath) showInvalidFolderAlert(gameVersion);
        }
        return input + "Interface\\AddOns";
    }

    private static void showInvalidFolderAlert(GameVersion gameVersion) {
        Platform.runLater(() -> {
            Alert exceptionAlert = new Alert(Alert.AlertType.WARNING);
            exceptionAlert.setHeaderText("Invalid folder!");
            exceptionAlert.setContentText("The supplied folder is invalid! Please select the folder containing the " + gameVersion.getExeName() + " file!");
            exceptionAlert.showAndWait();
        });
    }

    public static String scanForInstallLocation(GameVersion gameVersion) throws IOException {
        String exeName = gameVersion.getExeName();

        String[] commands = {"cmd.exe", "/c", "cd \\ & dir /s /b " + exeName + " & exit"};
        Process p = Runtime.getRuntime().exec(commands);
        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;

        while (true) {
            line = input.readLine();
            if (line == null) break;

            if (!line.contains("\\" + gameVersion.getPath() + "\\")) continue;
            String substring = line.substring(0, line.length() - exeName.length());
            if(verifyInstallLocation(substring, gameVersion)) return substring;
        }

        return null;
    }

    public static boolean verifyInstallLocation(String path, GameVersion gameVersion) {
        Log.verbose("Checking supplied path ...");

        String exeName = gameVersion.getExeName();
        String prefix = gameVersion.getPrefix();


        String exePath = path + exeName;
        if (!(new File(exePath).exists())) {
            Log.verbose(exeName + " not found!");
            return false;
        }

        String version = FileOperations.getFileVersion(exePath);

        if (!version.startsWith(prefix)) {
            Log.verbose("Invalid game version!");
            return false;
        }
        Log.verbose("Path valid!");
        return true;
    }

    public ArgumentPasser getClosedButtonClicked() {
        return closedButtonClicked;
    }

    public void setClosedButtonClicked(ArgumentPasser closedButtonClicked) {
        this.closedButtonClicked = closedButtonClicked;
    }
}

class InstallationSearch {

    private TextField locationTextField;
    private ImageView searchingImageView;
    private Button cancelSearchButton;
    private Button confirmSearchButton;
    private Button manualSelectionButton;
    private AtomicReference<String> location;
    private GameVersion gameVersion;
    private AtomicBoolean skip;
    private AtomicBoolean confirmed;

    Image imageProcessing = new Image(this.getClass().getClassLoader().getResource("processingAddon.gif").toExternalForm());
    Image imageSuccess = new Image(this.getClass().getClassLoader().getResource("success.png").toExternalForm());

    public InstallationSearch(GameVersion gameVersion, TextField locationTextField, ImageView searchingImageView,
                              Button cancelSearchButton, Button confirmSearchButton, Button manualSelectionButton){
        this.gameVersion = gameVersion;
        this.locationTextField = locationTextField;
        this.searchingImageView = searchingImageView;
        this.cancelSearchButton = cancelSearchButton;
        this.confirmSearchButton = confirmSearchButton;
        this.manualSelectionButton = manualSelectionButton;
        this.skip = new AtomicBoolean(false);
        this.location = new AtomicReference<>(null);
        this.confirmed = new AtomicBoolean(false);
    }

    public String getLocation(){
        return location.get();
    }

    private void searchComplete(){
        searchingImageView.setVisible(false);
        searchingImageView.setDisable(true);
        searchingImageView.setFocusTraversable(false);
    }

    private void searchSuccess(String installLocation){
        searchComplete();
        ImageView buttonView = new ImageView(imageSuccess);
        confirmSearchButton.setGraphic(buttonView);
        buttonView.setFitHeight(15);
        buttonView.setFitWidth(15);
        confirmSearchButton.setVisible(true);
        confirmSearchButton.setDisable(false);
        confirmSearchButton.setFocusTraversable(true);
        locationTextField.setText(installLocation);
    }

    public void startScan() {
        try {
            Platform.runLater(() -> searchingImageView.setImage(imageProcessing));
            location.set(scanForInstallLocation(gameVersion));
            Platform.runLater(() -> {
                if(skip.get() || location.get() == null) {
                    cancelSearch();
                    return;
                }
                searchSuccess(location.get());
            });
        } catch (IOException e) {
            Controller.getInstance().createExceptionAlert("Scanning error!", "An error occurred while attempting to scan!",
                    "While attempting to scan for wow installations, the program encountered an error. This may be due to insufficient permission.", e);
        }
    }

    public void cancelSearch(){
        skip.set(true);
        searchComplete();
        locationTextField.setText("Select Location Manually");
        confirmSearchButton.setVisible(false);
        confirmSearchButton.setDisable(true);
        confirmSearchButton.setFocusTraversable(false);
        enableManualSelection();
    }

    public void prefillSearch(String currentUrl){
        location.set(currentUrl);
        searchSuccess(location.get());
    }

    private void enableManualSelection() {
        manualSelectionButton.setVisible(true);
        manualSelectionButton.setFocusTraversable(true);
        manualSelectionButton.setDisable(false);
    }

    public void manualSelection() {
        String installLocation = specifyInstallLocation(gameVersion);
        if(installLocation == null) return;
        location.set(installLocation);
        searchSuccess(location.get());
    }

    public void confirmFolder(GUISelectInstallationFolder context){
        manualSelectionButton.setVisible(false);
        manualSelectionButton.setFocusTraversable(false);
        manualSelectionButton.setDisable(true);

        cancelSearchButton.setVisible(false);
        cancelSearchButton.setFocusTraversable(false);
        cancelSearchButton.setDisable(true);

        confirmSearchButton.setVisible(false);
        confirmSearchButton.setFocusTraversable(false);
        confirmSearchButton.setDisable(true);

        confirmed.set(true);

        context.enableContinueButton();
    }

    public boolean isConfirmed(){
        return confirmed.get();
    }


}

