package com.CAM.GUI;

import com.CAM.AddonManagement.Addon;
import com.CAM.AddonManagement.AddonManager;
import com.CAM.AddonManagement.AddonRequest;
import com.CAM.HelperTools.Log;
import com.CAM.HelperTools.UserInput;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;

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
    private Text textAdd;

    @FXML
    private TextField textFieldBranch;

    @FXML
    private CheckBox checkboxReleases;

    final ObservableList<String> listItems = FXCollections.observableArrayList();

    private AddonManager addonManager;

    @FXML
    private void releasesAction(){
        textFieldBranch.setDisable(checkboxReleases.isSelected());
    }

    @FXML
    private void setupAction(){
        UserInput userInput = new ToastUserInput("Please provide path to WoW Classic Installation");
        addonManager.setInstallLocation(AddonManager.specifyInstallLocation(userInput));
    }

    @FXML
    private void toggleDebugAction(){
        Log.logging = !Log.logging;
    }

    @FXML
    private void removeAction(){
        ObservableList<Integer> selected = listViewAddons.getSelectionModel().getSelectedIndices();
        if(selected.size() < 1){
            return;
        }
        addonManager.removeAddon(selected.get(0));
        updateListView();
    }

    @FXML
    private void updateAction(){
        addonManager.updateAddons();
    }

    @FXML
    private void addAction(){
        textAdd.setVisible(true);
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
        updateListView();
        textAdd.setVisible(false);
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
        listViewAddons.setItems(listItems);
        Log.listen(new GUILogListener(textAreaOutputLog));
    }

    public AddonManager getAddonManager(){
        return addonManager;
    }

    public void setAddonManager(AddonManager addonManager){
        this.addonManager = addonManager;
    }


}
