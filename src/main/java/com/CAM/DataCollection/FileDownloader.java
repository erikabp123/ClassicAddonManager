package com.CAM.DataCollection;

import com.CAM.HelperTools.IO.DownloadListener;
import com.CAM.HelperTools.Logging.Log;
import com.CAM.HelperTools.TableViewStatus;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.google.api.client.googleapis.media.MediaHttpDownloaderProgressListener;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.BackOff;
import com.google.api.client.util.ExponentialBackOff;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

public class FileDownloader {

    private String downloadLocation;
    private static ArrayList<DownloadListener> listeners = new ArrayList<>();
    private static final int EOF = -1;
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
    private ArrayList<DownloadListener> localListeners;

    public FileDownloader(String downloadLocation){
        this.downloadLocation = downloadLocation;
        this.localListeners = new ArrayList<>();
    }

    public void listenLocal(DownloadListener listener){
        localListeners.add(listener);
    }

    public void notifyAllLocalListeners(double progress){
        for(DownloadListener listener: localListeners){
            listener.notify(progress);
        }
    }

    public static void listen(DownloadListener listener){
        listeners.add(listener);
    }

    public void notifyAllListeners(double progress){
        for(DownloadListener listener : listeners){
            listener.notify(progress);
        }
    }

    public void notifyAllListenersMultiDownload(double progress, double totalProgress){
        for(DownloadListener listener : listeners){
            listener.notifyMultipleDownload(progress, totalProgress);
        }
    }

    public void downloadFile(String stringUrl, String fileName) {
        File file = new File(downloadLocation + "/" + fileName);

        try {
            URL url = new URL(stringUrl);
            FileUtils.copyURLToFile(url, file);
        } catch (IOException e) {
            Log.printStackTrace(e);
        }

    }

    public void downloadFileMonitored(String stringUrl, String fileName, int retries) throws IOException, ZipException {
        File file = new File(downloadLocation + "/" + fileName);
        URL url;
        URLConnection urlConnection;
        InputStream source;
        url = new URL(stringUrl);
        urlConnection = url.openConnection();
        urlConnection.connect();
        source = url.openStream();

        int fileSize = urlConnection.getContentLength();
        try {


            final FileOutputStream output = FileUtils.openOutputStream(file);
            try {

                final byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                long count = 0;
                int n;
                while (EOF != (n = source.read(buffer))) {
                    output.write(buffer, 0, n);
                    count += n;
                    double progress = (count*1.0/fileSize);
                    notifyAllListeners(progress);
                    notifyAllLocalListeners(progress);
                }

                output.close(); // don't swallow close Exception if copy completes normally

            } finally {
                IOUtils.closeQuietly(output);
            }

        } finally {
            IOUtils.closeQuietly(source);
            ZipFile zipFile = new ZipFile(file);
            if(!zipFile.isValidZipFile()) {
                if(retries > 0) downloadFileMonitored(stringUrl, fileName, retries - 1);
                else throw new IOException("Invalid zip file");
            }
            notifyAllListeners(1);
            notifyAllLocalListeners(1);
        }
    }

    public void downloadMultipleFilesMonitored(HashMap<String, String> files){
        HashMap<String, FileDownload> fileSizes = establishFileSize(files);

        long sumFileSize = 0;
        for(String key : fileSizes.keySet()){
            sumFileSize += fileSizes.get(key).fileSize;
        }
        System.out.println("sumFileSize: " + sumFileSize);
        long totalCount = 0;

        for(String stringUrl : files.keySet()){
            String fileName = files.get(stringUrl);
            URL url = fileSizes.get(stringUrl).url;
            int fileSize = fileSizes.get(stringUrl).fileSize;

            File file = new File(downloadLocation + "/" + fileName);
            InputStream source = null;
            try {
                source = url.openStream();
            } catch (IOException e) {
                Log.printStackTrace(e);
            }

            try {

                final FileOutputStream output = FileUtils.openOutputStream(file);
                try {

                    final byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                    long count = 0;
                    int n;
                    while (EOF != (n = source.read(buffer))) {
                        output.write(buffer, 0, n);
                        count += n;
                        totalCount += n;
                        double progress = (count*1.0/fileSize);
                        double totalProgress = (totalCount*1.0/sumFileSize);
                        notifyAllListenersMultiDownload(progress, totalProgress);
                    }

                    output.close(); // don't swallow close Exception if copy completes normally
                } finally {
                    IOUtils.closeQuietly(output);
                }

            } catch (IOException e) {
                Log.printStackTrace(e);
            } finally {
                IOUtils.closeQuietly(source);
            }

            System.out.println("Finished download");
        }

    }

    private HashMap<String, FileDownload> establishFileSize(HashMap<String, String> files){
        HashMap<String, FileDownload> fileSizes = new HashMap<>();
        for(String stringUrl : files.keySet()){

            URL url = null;
            URLConnection urlConnection = null;
            try {
                url = new URL(stringUrl);
                urlConnection = url.openConnection();
                urlConnection.connect();
            } catch (IOException e) {
                Log.printStackTrace(e);
            }

            fileSizes.put(stringUrl, new FileDownload(urlConnection.getContentLength(), url));
        }
        return fileSizes;
    }


    public void googleDownloadFile(String url, String fileName, TableViewStatus tableViewStatus) throws IOException {
        new File(downloadLocation + "/" + fileName);
        OutputStream out = new FileOutputStream(downloadLocation + "/" + fileName);

        HttpTransport transport = new NetHttpTransport();
        HttpRequestInitializer httpRequestInitializer = request -> {
            BackOff backOff = new ExponentialBackOff();
            request.setUnsuccessfulResponseHandler(new HttpBackOffUnsuccessfulResponseHandler(backOff));
            request.setIOExceptionHandler(new HttpBackOffIOExceptionHandler(backOff));
        };

        MediaHttpDownloader downloader = new MediaHttpDownloader(transport, httpRequestInitializer);
        downloader.setProgressListener(tableViewStatus);
        GenericUrl requestUrl = new GenericUrl(url);
        downloader.setDirectDownloadEnabled(true);
        downloader.download(requestUrl, out);
    }

    private class FileDownload {
        int fileSize;
        URL url;

        public FileDownload(int fileSize, URL url){
            this.fileSize = fileSize;
            this.url = url;
        }
    }



}


