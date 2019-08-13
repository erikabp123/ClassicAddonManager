package com.CAM.GUI;

import com.CAM.AddonManagement.Addon;
import com.CAM.AddonManagement.AddonManager;
import com.CAM.HelperTools.Log;
import com.CAM.HelperTools.UserInput;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
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

    final ObservableList<String> listItems = FXCollections.observableArrayList();

    private final AtomicReference<AddonManager> addonManager = new AtomicReference<AddonManager>();

    @FXML
    private void setupAction(){
        UserInput userInput = new ToastUserInput("Please provide path to WoW Classic Installation");
        addonManager.get().setInstallLocation(AddonManager.specifyInstallLocation(userInput));
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
        addonManager.get().removeAddon(selected.get(0));
        updateListView();
    }

    @FXML
    private void updateAction(){
        addonManager.get().updateAddons();
    }

    @FXML
    private void addAction(){
        textAdd.setVisible(true);
        String origin = textFieldURL.getText();
        addonManager.get().addNewAddon(origin);
        updateListView();
        textAdd.setVisible(false);
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
        for(Addon addon : addonManager.get().getManagedAddons()){
            addonNames.add(addon.getName());
        }
        listItems.setAll(addonNames);
        updateListViewLabel();
    }

    private void updateListViewLabel(){
        int managedCount = addonManager.get().getManagedAddons().size();
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
        return addonManager.get();
    }

    public void setAddonManager(AddonManager addonManager){
        this.addonManager.set(addonManager);
    }


}
