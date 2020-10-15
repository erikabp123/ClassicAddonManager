package com.CAM.GUI;

import com.CAM.AddonManagement.AddonManager;
import com.CAM.HelperTools.GameVersion;
import com.CAM.HelperTools.LoopController;
import com.CAM.HelperTools.UserInput;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

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
        for(GameVersion gv: searches.keySet()){
            InstallationSearch search = searches.get(gv);
            if(!search.isConfirmed()) continue;
            AddonManager manager = AddonManager.initializeFromScanUI(gv, search.getLocation());
            managers.put(gv, manager);
        }
        closeStage(event);
    }

    @FXML
    private void cancelAction(ActionEvent event) {
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

    private void scanForLocations() {
        for(GameVersion gv: GameVersion.values()){
            searches.put(gv, createInstallationSearch(gv));
        }
        for(GameVersion gv: searches.keySet()){
            Thread searchThread = new Thread(() -> searches.get(gv).startScan());
            searchThread.setDaemon(true);
            searchThread.start();
        }
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
        managers = (HashMap<GameVersion, AddonManager>) args[0];
        updateButtonGraphics();
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
            view.setFitHeight(15);
            view.setFitWidth(15);
            button.setGraphic(view);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

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
        buttonView.setFitHeight(15);
        buttonView.setFitWidth(15);
        confirmSearchButton.setGraphic(buttonView);
        confirmSearchButton.setVisible(true);
        confirmSearchButton.setDisable(false);
        confirmSearchButton.setFocusTraversable(true);
        locationTextField.setText(installLocation);
    }

    public void startScan() {
        try {
            Platform.runLater(() -> searchingImageView.setImage(imageProcessing));
            location.set(AddonManager.scanForInstallLocation(gameVersion));
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

    private void enableManualSelection() {
        manualSelectionButton.setVisible(true);
        manualSelectionButton.setFocusTraversable(true);
        manualSelectionButton.setDisable(false);
    }

    public void manualSelection() {
        String installLocation = AddonManager.specifyInstallLocation(gameVersion);
        if(installLocation == null) return;
        searchSuccess(installLocation);
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

