package com.CAM.DataCollection;

import com.CAM.HelperTools.Log;
import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;

public class BroadcastFetcher {

    private final String broadcastUrl = "https://raw.githubusercontent.com/erikabp123/ClassicAddonManager/master/EmergencyBroadcast.txt";

    public TextPage scrape(String targetUrl) throws IOException {
        WebClient client = new WebClient(BrowserVersion.CHROME);
        client.getOptions().setJavaScriptEnabled(false);
        client.getOptions().setCssEnabled(false);
        client.getOptions().setUseInsecureSSL(true);

        TextPage page;

        page = client.getPage(targetUrl);

        return page;
    }

    public String fetchBroadcastMessage() throws IOException{
        TextPage page = scrape(broadcastUrl);
        return page.getContent();
    }
}
