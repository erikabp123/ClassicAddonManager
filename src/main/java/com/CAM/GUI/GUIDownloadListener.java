package com.CAM.GUI;

import com.CAM.HelperTools.DownloadListener;
import javafx.application.Platform;
import javafx.scene.control.ProgressBar;

public class GUIDownloadListener implements DownloadListener {

    private ProgressBar progressBar;

    public GUIDownloadListener(ProgressBar progressBar){
        this.progressBar = progressBar;
    }

    @Override
    public void notify(double progress) {
        Platform.runLater(() -> progressBar.setProgress(progress));
    }
}
