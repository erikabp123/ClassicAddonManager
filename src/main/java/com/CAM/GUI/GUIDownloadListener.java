package com.CAM.GUI;

import com.CAM.HelperTools.DownloadListener;
import javafx.application.Platform;
import javafx.scene.control.ProgressBar;

public class GUIDownloadListener implements DownloadListener {

    private ProgressBar progressBar;
    private ProgressBar progressBarTotal;

    public GUIDownloadListener(ProgressBar progressBar){
        this.progressBar = progressBar;
    }

    public GUIDownloadListener(ProgressBar progressBar, ProgressBar progressBarTotal){
        this.progressBar = progressBar;
        this.progressBarTotal = progressBarTotal;
    }

    @Override
    public void notify(double progress) {
        Platform.runLater(() -> progressBar.setProgress(progress));
    }

    @Override
    public void notifyMultipleDownload(double progress, double totalProgress) {
        if(progressBarTotal == null){
            return;
        }
        Platform.runLater(() -> {
            progressBar.setProgress(progress);
            progressBarTotal.setProgress(totalProgress);
        });
    }
}
