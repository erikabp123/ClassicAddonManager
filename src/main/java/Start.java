import DataCollection.CurseForgeScraper;
import DataCollection.FileDownloader;
import DataCollection.Scraper;
import HelperTools.Log;
import com.gargoylesoftware.htmlunit.html.*;

import java.io.IOException;
import java.util.List;

public class Start {



    public static void main(String[] args){
        Log.logging = true;

        AddonManager manager = AddonManager.initialize();
        //manager.testPopulate();
        manager.saveToFile();
        manager.updateAddons();

        //Addon detailsAddon = new Addon("testName", "testAuthor", "testVersion");
        //detailsAddon.fetchUpdate("https://www.curseforge.com/wow/addons/details");

    }



}
