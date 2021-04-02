package com.CAM.HelperTools;

import com.CAM.AddonManagement.Addon;
import com.CAM.HelperTools.IO.DownloadListener;
import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.google.api.client.googleapis.media.MediaHttpDownloaderProgressListener;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableCell;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class TableViewStatus implements DownloadListener, MediaHttpDownloaderProgressListener {
    private Button updateButton;
    private AtomicBoolean queuedForUpdate;
    private AtomicBoolean doneUpdating;
    private TableCell<Addon, Addon> cell;
    private AtomicReference<Double> progress;
    private AtomicReference<ChangeListener> changeListener;

    public TableViewStatus(){
        this.updateButton = new Button("Update");
        this.queuedForUpdate = new AtomicBoolean(false);
        this.doneUpdating = new AtomicBoolean(false);
        this.progress = new AtomicReference<>(0.0);
        this.changeListener = new AtomicReference<>(null);
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
        double curProgress = this.progress.get();
        if (curProgress < 1.0 && progress > curProgress) this.progress.set(progress);
        else return;
        if(this.progress.get() >= 1.0) setDoneUpdating(true);
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

    @Override
    public void progressChanged(MediaHttpDownloader downloader) {
        switch (downloader.getDownloadState()){
            case MEDIA_IN_PROGRESS:
                System.out.println("Progress: " + downloader.getProgress());
                setProgress(downloader.getProgress());
                if(changeListener.get() != null) changeListener.get().stateChanged(new ChangeEvent(this));
                break;
            case MEDIA_COMPLETE:
                setProgress(1.0);
                setDoneUpdating(true);
                if(changeListener.get() != null) changeListener.get().stateChanged(new ChangeEvent(this));
                break;
        }
    }
}
