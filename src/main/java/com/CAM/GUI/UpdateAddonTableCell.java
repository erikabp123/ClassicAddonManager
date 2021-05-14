package com.CAM.GUI;

import com.CAM.AddonManagement.Addon;
import com.CAM.DataCollection.DataCollectionException;
import com.CAM.HelperTools.TableViewStatus;
import com.CAM.Settings.Preferences;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableCell;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class UpdateAddonTableCell extends TableCell<Addon, Addon> {
    final ProgressBar progressBar = new ProgressBar(0);

    Button updateButton;
    AtomicReference<HashMap<Addon, TableViewStatus>> updateTableViewMap;
    Controller controller;


    public UpdateAddonTableCell(AtomicReference<HashMap<Addon, TableViewStatus>> updateTableViewMap) {
        this.updateButton = new Button("Update");
        this.updateButton.setDisable(true);
        this.updateButton.setVisible(false);

        this.updateTableViewMap = updateTableViewMap;
        this.controller = Controller.getInstance();

    }

    private void setUpdateButtonOnAction(Addon item, boolean empty) {
        updateButton.setOnAction(event -> {
            try {
                updateTableViewMap.get().get(item).setQueuedForUpdate(true);
                updateItem(item, empty);
                Controller.getInstance().getAddonManager().updateSpecificAddon(item, updateTableViewMap.get().get(item));
            } catch (DataCollectionException e) {
                controller.handleUpdateScrapeException(e);
            }
        });
    }

    @Override
    public void updateItem(Addon item, boolean empty) {
        setUpdateButtonOnAction(item, empty);

        // No contents in cell
        if (item == null) {
            updateButton.setDisable(true);
            updateButton.setVisible(false);
            setGraphic(null);
            return;
        }

        // Addon in cell is not in the list of addons with an update
        if (!updateTableViewMap.get().containsKey(item)) {
            updateButton.setDisable(true);
            updateButton.setVisible(false);
            setGraphic(null);

            Date lastUpdateCheck = item.getLastUpdateCheck();
            String text = "Not installed!";
            if (lastUpdateCheck != null) {
                boolean recentlyChecked = (new Date()).getTime() - lastUpdateCheck.getTime() < Preferences.getInstance().getMaxCacheDuration() * 60000;
                if (recentlyChecked)
                    text = "up-to-date";
                else text = "Not checked";
            }
            setText(text);
            return;
        }

        // Addon in cell has update available
        TableViewStatus tableViewStatus = updateTableViewMap.get().get(item);
        tableViewStatus.setOnChangeListener(e -> Platform.runLater(() -> {
            progressBar.setProgress(tableViewStatus.getProgress());
            updateItem(item, empty);
        }));

        // Addon in cell is not in list of addons that should be updating right now
        if(!tableViewStatus.isQueuedForUpdate()){
            updateButton.setDisable(false);
            updateButton.setVisible(true);
            setGraphic(updateButton);
            setText(null);
            return;
        }

        // Addon in cell is in list of addons that should be updating now

        // Addon in process of updating
        if(!tableViewStatus.isDoneUpdating()){
            updateButton.setDisable(true);
            updateButton.setVisible(false);
            setText(null);
            progressBar.setProgress(tableViewStatus.getProgress());
            setGraphic(progressBar);
            return;
        }

        // Addon finished updating
        updateButton.setDisable(true);
        updateButton.setVisible(false);
        setGraphic(null);

        String text = "up-to-date";
        setText(text);
    }


}
