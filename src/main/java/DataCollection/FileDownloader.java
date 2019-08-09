package DataCollection;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class FileDownloader {

    private String downloadLocation;

    public FileDownloader(String downloadLocation){
        this.downloadLocation = downloadLocation;
    }


    public void downloadFile(String stringUrl, String fileName) {
        File file = new File(downloadLocation + "/" + fileName);

        try {
            URL url = new URL(stringUrl);
            FileUtils.copyURLToFile(url, file);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
