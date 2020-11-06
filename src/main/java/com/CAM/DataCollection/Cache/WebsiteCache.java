package com.CAM.DataCollection.Cache;

import com.CAM.Settings.Preferences;
import com.CAM.Settings.PreferencesChangeListener;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.TimeUnit;

public class WebsiteCache implements PreferencesChangeListener {

    private static Cache<String, String> cache = Caffeine.newBuilder()
            .expireAfterWrite(Preferences.getInstance().getMaxCacheDuration(), TimeUnit.MINUTES)
            .maximumSize(100)
            .build();

    public static void cacheValue(String url, String jsonResponse){
        cache.put(url, jsonResponse);
    }

    public static String getCacheValue(String url){
        return cache.getIfPresent(url);
    }

    @Override
    public void notifyChange(String nameOfPreference) {
        if(!nameOfPreference.equals("maxCacheDuration")) return;

        cache = Caffeine.newBuilder()
                .expireAfterWrite(Preferences.getInstance().getMaxCacheDuration(), TimeUnit.MINUTES)
                .maximumSize(100)
                .build();
    }
}
