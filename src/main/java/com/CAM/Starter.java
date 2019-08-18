package com.CAM;

import com.CAM.HelperTools.Log;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.StandardOpenOption;

public class Starter {

    public static void main(String[] args) {
        if(!claimInstanceLock()){
            return;
        }
        com.CAM.GUI.Main.begin(args);
    }

    private static boolean claimInstanceLock(){
        String userHome = System.getProperty("user.home");
        File file = new File(userHome, "CAM.lock");
        try {
            FileChannel fc = FileChannel.open(file.toPath(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE);
            FileLock lock = fc.tryLock();
            if (lock == null) {
                return false;
            }
        } catch (IOException e) {
            throw new Error(e);
        }
        return true;
    }


}
