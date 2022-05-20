package com.CAM.DataCollection;

import com.CAM.DataCollection.Cache.WebsiteCache;
import com.CAM.HelperTools.GameSpecific.AddonSource;
import com.CAM.HelperTools.Logging.Log;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;

import java.io.IOException;

public abstract class PageFetcher {

    public static final String CURSEFORGE_BASE_SITE = "addons-ecs.forgesvc.net";

    protected String fetchJson(String url) throws DataCollectionException {
        String cachedValue = WebsiteCache.getCacheValue(url);
        if (cachedValue != null) {
            System.out.println("Using cached value of: " + url);
            return cachedValue;
        }

        WebClient client = new WebClient();
        client.getOptions().setJavaScriptEnabled(false);
        client.getOptions().setCssEnabled(false);
        client.getOptions().setUseInsecureSSL(true);
        client.addRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.114 Safari/537.36");

        Page page;

        try {
            page = client.getPage(url);
        } catch (FailingHttpStatusCodeException e){
            Log.verbose("Url at: " + url + " failed!");
            Log.verbose("fetch resulted in " + e.getStatusCode());
            if (url.contains(CURSEFORGE_BASE_SITE)) return "";
            throw new DataCollectionException(getAddonSource(), e, "Url at: " + url + " failed with code " + e.getStatusCode()
                    + ". Status message was: " + e.getStatusMessage());
        } catch (IOException e) {
            throw new DataCollectionException(getAddonSource(), e);
        }
        if(page == null) return null;
        String json = page.getWebResponse().getContentAsString();
        WebsiteCache.cacheValue(url, json);
        return json;
    }

    protected abstract AddonSource getAddonSource();
}
