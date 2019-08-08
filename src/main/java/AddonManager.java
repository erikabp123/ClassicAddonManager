import com.google.gson.Gson;

import java.util.ArrayList;

public class AddonManager {

    private String version = "1.0";
    private ArrayList<Addon> managedAddons;

    public AddonManager(){
        this.managedAddons = new ArrayList<>();
    }

    public void test(){
        managedAddons.add(new Addon("testAddonName", "testAddonAuthor", "testAddonVersion"));
        managedAddons.add(new Addon("testAddonName2", "testAddonAuthor2", "testAddonVersion2"));
        managedAddons.add(new Addon("testAddonName3", "testAddonAuthor3", "testAddonVersion3"));

        Gson gson = new Gson();

        System.out.println(gson.toJson(this));

    }





}
