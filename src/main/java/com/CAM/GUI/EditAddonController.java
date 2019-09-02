package com.CAM.GUI;

import com.CAM.AddonManagement.Addon;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

public class EditAddonController implements Initializable {

    @FXML
    private TextField addonName;

    @FXML
    private TextField lastUpdated;

    @FXML
    private TextField addonAuthor;

    @FXML
    private TextField branch;

    @FXML
    private TextField addonUrl;

    @FXML
    private CheckBox releases;

    private Date lastUpdatedDate;

    private Addon addon;

    public static final int BUTTON_SAVE = 1;

    public static final int BUTTON_CANCEL = 0;

    public int buttonPress = BUTTON_CANCEL;

    @FXML
    private void toggleReleases(){
        branch.setDisable(releases.isSelected());
    }

    @FXML
    private void saveAction(ActionEvent event){
        try {
            URL url = new URL(addonUrl.getText());
        } catch (MalformedURLException e) {
            Alert malformedAlert = new Alert(Alert.AlertType.ERROR);
            malformedAlert.setTitle("Invalid URL format!");
            malformedAlert.setHeaderText("The URL is not a valid format URL!");
            malformedAlert.setContentText("The URL provided for the addon is not in the correct format, it must start with http or https and be of the format 'https://www.WEBSITE.com/...'!");
            malformedAlert.showAndWait();
            return;
        }
        if(!releases.isSelected()){
            addon.setBranch(branch.getText());
        }
        addon.setReleases(releases.isSelected());
        addon.setOrigin(addonUrl.getText());
        buttonPress = BUTTON_SAVE;
        closeStage(event);
    }

    @FXML
    private void cancelAction(ActionEvent event){
        buttonPress = BUTTON_CANCEL;
        closeStage(event);
    }

    public void createDialog(Addon addon){
        this.addon = addon;
        this.addonName.setText(addon.getName());
        this.addonAuthor.setText(addon.getAuthor());
        if(addon.getLastUpdated() == null){
            this.lastUpdated.setText("Never downloaded");
        } else {
            this.lastUpdated.setText(addon.getLastUpdated().toString());
        }
        this.addonUrl.setText(addon.getOrigin());
        if(addon.getBranch() != null){
            this.branch.setText(addon.getBranch());
        }
        this.branch.setDisable(addon.isReleases());
        this.releases.setSelected(addon.isReleases());
    }

    private void closeStage(ActionEvent event){
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
