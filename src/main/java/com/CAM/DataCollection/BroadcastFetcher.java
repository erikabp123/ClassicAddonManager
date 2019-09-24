package com.CAM.DataCollection;

import com.CAM.HelperTools.Log;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;

public class BroadcastFetcher {

    private final String broadcastUrl = "https://raw.githubusercontent.com/erikabp123/ClassicAddonManager/master/EmergencyBroadcast.txt";

    public HtmlPage scrape(String targetUrl) throws IOException {
        WebClient client = new WebClient(BrowserVersion.CHROME);
        client.getOptions().setJavaScriptEnabled(false);
        client.getOptions().setCssEnabled(false);
        client.getOptions().setUseInsecureSSL(true);

        HtmlPage page;

        page = client.getPage(targetUrl);

        return page;
    }

    public String fetchBroadcastMessage() throws IOException{
        HtmlPage page = scrape(broadcastUrl);
        return page.asText();
    }
}
