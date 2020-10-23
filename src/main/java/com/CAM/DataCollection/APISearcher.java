package com.CAM.DataCollection;

import com.CAM.HelperTools.AddonSource;
import com.CAM.HelperTools.Log;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;

import java.io.IOException;

public abstract class APISearcher {

    protected Page fetchJson(String url) throws DataCollectionException {
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
        return page;
    }

    public abstract AddonSource getAddonSource();

}
