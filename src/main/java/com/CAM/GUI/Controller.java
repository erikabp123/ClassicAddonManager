package com.CAM.GUI;

import com.CAM.AddonManagement.Addon;
import com.CAM.AddonManagement.AddonManager;
import com.CAM.AddonManagement.AddonRequest;
import com.CAM.DataCollection.FileDownloader;
import com.CAM.HelperTools.DownloadListener;
import com.CAM.HelperTools.Log;
import com.CAM.HelperTools.SelfUpdater;
import com.CAM.HelperTools.UserInput;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class Controller implements Initializable {


    @FXML
    private ListView<String> listViewAddons;

    @FXML
    private Button buttonRemove;

    @FXML
    private TextField textFieldURL;

    @FXML
    private Text textManagedLabel;

    @FXML
    private TextArea textAreaOutputLog;

    @FXML
    private ImageView imageViewAdd;

    @FXML
    private TextField textFieldBranch;

    @FXML
    private CheckBox checkboxReleases;

    @FXML
    public VBox vboxMainUI;

    @FXML
    public ImageView imageViewUpdate;

    @FXML
    public ProgressBar progressBarDownload;

    @FXML
    private Button buttonUpdate;

    @FXML
    private Button buttonAdd;

    @FXML
    private MenuItem menuAboutVersion;

    final ObservableList<String> listItems = FXCollections.observableArrayList();

    private AddonManager addonManager;

    @FXML
    private void releasesAction(){
        textFieldBranch.setDisable(checkboxReleases.isSelected());
    }

    @FXML
    private void setupAction(){
        UserInput userInput = new GUIUserInput("Please provide path to WoW Classic Installation");
        addonManager.setInstallLocation(AddonManager.specifyInstallLocation(userInput));
    }

    @FXML
    private void toggleDebugAction(){
        Log.toggleLogging();
    }

    @FXML
    private void removeAction(){
        Thread removeThread = new Thread(() -> {
            Platform.runLater(() -> disableAll());
            ObservableList<Integer> selected = listViewAddons.getSelectionModel().getSelectedIndices();
            if(selected.size() < 1){
                return;
            }
            addonManager.removeAddon(selected.get(0));
            Platform.runLater(() -> {
                updateListView();
                enableAll();
            });
        });
        removeThread.start();
    }

    @FXML
    private void updateAction(){
        Thread updateThread = new Thread(() -> {
            Platform.runLater(() -> {
                disableAll();
                buttonUpdate.setVisible(false);
                progressBarDownload.setVisible(true);
            });
            addonManager.updateAddons();
            Platform.runLater(() -> {
                enableAll();
                buttonUpdate.setVisible(true);
                progressBarDownload.setVisible(false);
            });
        });
        updateThread.start();
    }

    @FXML
    private void addAction(){
        Thread addAddonThread = new Thread(() -> {
            Platform.runLater(() -> {
                disableAll();
                imageViewAdd.setVisible(true);
            });
            String origin = textFieldURL.getText();
            String branch = textFieldBranch.getText();
            boolean releases = checkboxReleases.isSelected();
            AddonRequest request = new AddonRequest();
            request.origin = origin;
            request.branch = branch;
            request.releases = releases;

            if(!isValidRequest(request)){
                return;
            }

            addonManager.addNewAddon(request);
            Platform.runLater(() -> {
                updateListView();
                enableAll();
                imageViewAdd.setVisible(false);
            });
        });
        addAddonThread.start();
    }

    private void progressBarListen(){
        DownloadListener downloadListener = new GUIDownloadListener(progressBarDownload);
        FileDownloader.listen(downloadListener);
    }

    private boolean isValidRequest(AddonRequest request){
        if(request.origin.contains("curseforge.com")){
            return true;
        }
        if(request.releases){
            return true;
        }
        if(!request.branch.equals("")){
            return true;
        }
        return false;
    }

    @FXML
    private void githubRedirectAction(){
        String github = "https://github.com/erikabp123/ClassicAddonManager";
        openUrl(github);
    }

    @FXML
    private void discordRedirectAction(){
        String discord = "https://discord.gg/StX3gbw";
        openUrl(discord);
    }

    private void openUrl(String url){
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateListView(){
        ArrayList<String> addonNames = new ArrayList<>();
        for(Addon addon : addonManager.getManagedAddons()){
            addonNames.add(addon.getName());
        }
        listItems.setAll(addonNames);
        updateListViewLabel();
    }

    private void updateListViewLabel(){
        int managedCount = addonManager.getManagedAddons().size();
        String textSuffix = "addons";
        if(managedCount == 1){
            textSuffix = "addon";
        }
        textManagedLabel.setText("Managing " + managedCount +" " + textSuffix);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        menuAboutVersion.setText("Version " + SelfUpdater.VERSION);
        imageViewAdd.setImage(new Image(this.getClass().getClassLoader().getResource("adding.gif").toExternalForm()));
        listViewAddons.setItems(listItems);
        Log.listen(new GUILogListener(textAreaOutputLog));
        progressBarListen();
    }

    public void checkForUpdate(){
        SelfUpdater.selfUpdate(this);
    }

    public AddonManager getAddonManager(){
        return addonManager;
    }

    public void setAddonManager(AddonManager addonManager){
        this.addonManager = addonManager;
    }

    private void disableAll(){
        buttonRemove.setDisable(true);
        buttonAdd.setDisable(true);
        buttonUpdate.setDisable(true);
    }

    private void enableAll(){
        buttonRemove.setDisable(false);
        buttonAdd.setDisable(false);
        buttonUpdate.setDisable(false);
    }


}
