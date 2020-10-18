package com.CAM.DataCollection.Cache;

import com.CAM.HelperTools.AddonSource;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Cache {
    //TODO: Probably replace this with a proper cache library

    final long MAX_CACHE_TIME = 1800000; //30 min

    static Cache cache;
    private ConcurrentMap<AddonSource, Object> concurrentMap;
    private ConcurrentMap<AddonSource, Long> concurrentMapTimestamp;

    public static Cache getInstance(){
        if(cache == null) return new Cache();
        return cache;
    }

    public Cache(){
        concurrentMap = new ConcurrentHashMap<>();
        concurrentMapTimestamp = new ConcurrentHashMap<>();
    }

    public void cacheValue(AddonSource key, Object value){
        concurrentMap.put(key, value);
        concurrentMapTimestamp.put(key, System.currentTimeMillis());
    }

    public Object getCacheValue(AddonSource key){
        long timeStamp = concurrentMapTimestamp.getOrDefault(key, 0L);
        if(timeStamp == 0) return null;
        if(timeStamp < System.currentTimeMillis() - MAX_CACHE_TIME) {
            concurrentMapTimestamp.remove(key);
            concurrentMap.remove(key);
            return null;
        }
        return concurrentMap.get(key);
    }


}
