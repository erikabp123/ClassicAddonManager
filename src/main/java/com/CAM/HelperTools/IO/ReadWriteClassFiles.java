package com.CAM.HelperTools.IO;

import com.CAM.Updating.VersionInfo;
import com.google.gson.Gson;

import java.io.*;

public class ReadWriteClassFiles {

    public static void saveFile(String path, Object sourceObject){
        try {
            Gson gson = new Gson();
            File file = new File(path);
            file.getParentFile().mkdirs();
            Writer writer = new FileWriter(file);
            gson.toJson(sourceObject, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Object readFile(String path, Object destinationObjectType){
        File file = new File(path);
        if(!file.exists()){
            return null;
        }
        Object fileObject = null;
        try {
            Reader reader = new FileReader(path);
            Gson gson = new Gson();
            fileObject = gson.fromJson(reader, destinationObjectType.getClass());
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(destinationObjectType.getClass());
        return fileObject;
    }

}
