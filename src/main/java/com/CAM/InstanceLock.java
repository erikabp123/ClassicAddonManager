package com.CAM;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class InstanceLock {
    private String appName;

    FileLock lock;

    FileChannel channel;

    public InstanceLock(String appName) {
        this.appName = appName;
    }

    public boolean isAppActive() throws Exception {
        File file = new File(System.getProperty("user.home"), appName + ".tmp");
        channel = new RandomAccessFile(file, "rw").getChannel();

        lock = channel.tryLock();
        if(lock == null) {
            channel.close();
            return true;
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                lock.release();
                channel.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
        return false;
    }
}