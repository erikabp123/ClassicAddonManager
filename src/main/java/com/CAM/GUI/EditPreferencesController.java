package com.CAM.GUI;

import com.CAM.AddonManagement.Addon;
import com.CAM.Settings.Preferences;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

public class EditPreferencesController implements Initializable, WindowController {

    @FXML
    private CheckBox cfReleases;

    private Preferences preferences;

    @FXML
    private CheckBox updatesOnLaunch;

    @FXML
    private CheckBox scrollOnUpdate;

    @FXML
    private Slider cahceSlider;

    @FXML
    private void saveAction(ActionEvent event){
        this.preferences.setCfReleasesOnly(cfReleases.isSelected());
        this.preferences.setCheckForUpdatesOnLaunch(updatesOnLaunch.isSelected());
        this.preferences.setScrollToBottomOnUpdate(scrollOnUpdate.isSelected());
        this.preferences.setMaxCacheDuration((int) cahceSlider.getValue());
        Preferences.savePreferencesFile();
        closeStage(event);
    }

    @FXML
    private void cancelAction(ActionEvent event){
        closeStage(event);
    }

    @Override
    public void initDialog(Object[] args){
        this.preferences = Preferences.getInstance();
        this.cfReleases.setSelected(preferences.isCfReleasesOnly());
        this.updatesOnLaunch.setSelected(preferences.isCheckForUpdatesOnLaunch());
        this.scrollOnUpdate.setSelected(preferences.isScrollToBottomOnUpdate());
        this.cahceSlider.setValue(preferences.getMaxCacheDuration());
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
