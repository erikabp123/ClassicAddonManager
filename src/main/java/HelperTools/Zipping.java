package HelperTools;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

import java.util.ArrayList;
import java.util.List;

public class Zipping {

    public static List<FileHeader> unzip(String source, String destination){
        try {
            ZipFile zipFile = new ZipFile(source);
            zipFile.extractAll(destination);
            return (List<FileHeader>) zipFile.getFileHeaders();
        } catch (ZipException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }


}
