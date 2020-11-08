package com.CAM.HelperTools;

import com.CAM.AddonManagement.Addon;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableCell;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class TableViewStatus implements DownloadListener {
    private ProgressBar progressBar;
    private Button updateButton;
    private AtomicBoolean queuedForUpdate;
    private AtomicBoolean doneUpdating;
    private TableCell<Addon, Addon> cell;
    private AtomicReference<Double> progress;
    private AtomicReference<ChangeListener> changeListener;

    public TableViewStatus(){
        this.progressBar = new ProgressBar(0);
        this.updateButton = new Button("Update");
        this.queuedForUpdate = new AtomicBoolean(false);
        this.doneUpdating = new AtomicBoolean(false);
        this.progress = new AtomicReference<Double>(0.0);
        this.changeListener = new AtomicReference<ChangeListener>(null);
    }


    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public Button getUpdateButton() {
        return updateButton;
    }

    public boolean isQueuedForUpdate() {
        return queuedForUpdate.get();
    }

    public void setQueuedForUpdate(boolean queuedForUpdate) {
        this.queuedForUpdate.set(queuedForUpdate);
    }

    @Override
    public void notify(double progress) {
        this.progress.set(progress);
        if(progress == 1.0) setDoneUpdating(true);
        changeListener.get().stateChanged(new ChangeEvent(this));
    }

    public void setProgress(double progress){
        this.progress.set(progress);
    }

    public double getProgress(){
        return this.progress.get();
    }

    @Override
    public void notifyMultipleDownload(double progress, double totalProgress) {

    }

    public boolean isDoneUpdating() {
        return doneUpdating.get();
    }

    private void setDoneUpdating(boolean doneUpdating){
        this.doneUpdating.set(doneUpdating);
    }

    public void setOnChangeListener(ChangeListener changeListener){
        this.changeListener.set(changeListener);
    }

}
