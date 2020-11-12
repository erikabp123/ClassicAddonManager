package com.CAM.DataCollection;

import com.CAM.DataCollection.Cache.WebsiteCache;
import com.CAM.HelperTools.GameSpecific.AddonSource;
import com.CAM.HelperTools.Logging.Log;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;

import java.io.IOException;

public abstract class PageFetcher {

    protected String fetchJson(String url) throws DataCollectionException {
        String cachedValue = WebsiteCache.getCacheValue(url);
        if(cachedValue != null){
            System.out.println("Using cached value of: " + url);
            return cachedValue;
        }

        WebClient client = new WebClient();
        client.getOptions().setJavaScriptEnabled(false);
        client.getOptions().setCssEnabled(false);
        client.getOptions().setUseInsecureSSL(true);

        Page page;

        try {
            page = client.getPage(url);
        } catch (FailingHttpStatusCodeException e){
            Log.verbose("fetch resulted in " + e.getStatusCode());
            throw new DataCollectionException(getAddonSource(), e);
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
