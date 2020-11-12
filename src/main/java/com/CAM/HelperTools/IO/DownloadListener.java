package com.CAM.HelperTools.IO;

public interface DownloadListener {

    void notify(double progress);

    void notifyMultipleDownload(double progress, double totalProgress);

}
