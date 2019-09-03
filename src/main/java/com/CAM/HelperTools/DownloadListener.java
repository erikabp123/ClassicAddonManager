package com.CAM.HelperTools;

public interface DownloadListener {

    void notify(double progress);

    void notifyMultipleDownload(double progress, double totalProgress);

}
