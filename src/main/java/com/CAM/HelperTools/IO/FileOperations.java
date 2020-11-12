package com.CAM.HelperTools.IO;

import com.CAM.HelperTools.Logging.Log;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileOperations {

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

    public static boolean deleteDirectory(String path){
        File dir = new File(path);
        Log.verbose(path + " is valid directory: " + dir.exists());
        if(!dir.exists()){
            return false;
        }
        try {
            FileUtils.deleteDirectory(dir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static boolean deleteFile(String path){
        File file = new File(path);
        Log.verbose(path + " is valid file: " + file.exists());
        return file.delete();
    }

    public static String getFileVersion(String path) {
        //TODO: Clean this mess up
        String version = null;
        try {
            Runtime rt = Runtime.getRuntime();
            String commandPrefix = "wmic datafile where name=";
            String commandSuffix = " get Version /value";
            String fullCommand = commandPrefix + convertPath(path) + commandSuffix;
            String[] commands = {"cmd", "/c",  fullCommand};

            Process pr = rt.exec(commands);

            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));

            String line = null;
            while ((line = input.readLine()) != null) {
                if(!line.contains("Version")){
                    continue;
                }
                version = line.split("=")[1];
            }

            int exitVal = pr.waitFor();
            Log.verbose("Exited with error code " + exitVal);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return version;
    }

    private static String convertPath(String path){

        String[] parts = path.split("\\\\");

        String frankenstein = "\"" + parts[0];

        for(int i=1; i<parts.length; i++){
            frankenstein = frankenstein + "\\\\" + parts[i];
        }
        frankenstein = frankenstein + "\"";

        return frankenstein;
    }

    public static File[] fileFinder(String dirName, FileFilter fileFilter){
        File dir = new File(dirName);
        File[] files = dir.listFiles(fileFilter);
        return files;
    }

    public static String determineTOCName(String directory){
        Log.verbose("Determining TOC file name ...");
        FileFilter tocFilter = pathname -> {
            if(!pathname.getName().endsWith(".toc")){
                return false;
            }
            return true;
        };
        File[] tocFiles = fileFinder(directory, tocFilter);
        if(tocFiles.length == 0){
            return null;
        }
        String tocName = tocFiles[0].getName().replace(".toc", "");
        Log.verbose("TOC with name " + tocName + "!");
        return tocName;
    }

    public static void renameDirectory(String oldPath, String newName){
        File dir = new File(oldPath);
        String newPath = null;
        try {
            newPath = new File(dir.getParent() + "\\" + newName).getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        deleteDirectory(newPath);
        Path source = Paths.get(oldPath);
        try {
            Files.move(source, source.resolveSibling(newName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //dir.renameTo(newDir);
    }

    public static void moveFile(String curPath, String newPath){
        File curDir = new File(curPath);
        File newDir = new File(newPath);
        try {
            FileUtils.moveDirectory(curDir, newDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
