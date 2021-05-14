package com.CAM.Settings;

import com.CAM.HelperTools.IO.ReadWriteClassFiles;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AddonNameCache implements Observable {

    private static final String ADDON_NAME_CACHE_LOCATION = "system/CACHED_ADDON_NAMES";
    private static AddonNameCache CACHE;

    private final Set<String> addonNames;
    private final List<InvalidationListener> listenerList;

    private AddonNameCache() {
        this.addonNames = loadCacheFromFile();
        this.listenerList = new ArrayList<>();
    }

    public static AddonNameCache getInstance() {
        if (CACHE == null) CACHE = new AddonNameCache();
        return CACHE;
    }

    public void addAddonNameToCache(String addonName) {
        boolean newValue = CACHE.addonNames.add(addonName);
        if (!newValue) return;
        saveCache();
        notifyAllListeners();
    }

    public void addAddonNamesToCache(Set<String> addonNames) {
        boolean newValue = CACHE.addonNames.addAll(addonNames);
        if (!newValue) return;
        saveCache();
        notifyAllListeners();
    }

    public void clearCache() {
        CACHE.addonNames.clear();
        saveCache();
        notifyAllListeners();
    }

    public Set<String> getCachedAddonNames() {
        return addonNames;
    }

    private Set<String> loadCacheFromFile() {
        Set<String> cachedValues = (HashSet<String>) ReadWriteClassFiles.readFile(ADDON_NAME_CACHE_LOCATION, new HashSet<>());
        return cachedValues == null ? new HashSet<>() : cachedValues;
    }

    private void saveCache() {
        ReadWriteClassFiles.saveFile(ADDON_NAME_CACHE_LOCATION, this.addonNames);
    }

    private void notifyAllListeners() {
        for (InvalidationListener listener : listenerList) listener.invalidated(this);
    }

    @Override
    public void addListener(InvalidationListener listener) {
        listenerList.add(listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        listenerList.remove(listener);
    }
}
