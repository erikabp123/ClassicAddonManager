package com.CAM.GUI;

import com.CAM.AddonManagement.Addon;
import com.CAM.AddonManagement.AddonManager;
import com.CAM.HelperTools.UserInput;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
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

    final ObservableList<String> listItems = FXCollections.observableArrayList();

    private AddonManager addonManager;

    @FXML
    private void setupAction(){
        UserInput userInput = new ToastUserInput("Please provide path to WoW Classic Installation");
        addonManager.setInstallLocation(AddonManager.specifyInstallLocation(userInput));
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
        String origin = textFieldURL.getText();
        addonManager.addNewAddon(origin);
        updateListView();
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
    }

    public AddonManager getAddonManager(){
        return addonManager;
    }

    public void setAddonManager(AddonManager addonManager){
        this.addonManager = addonManager;
    }


}
