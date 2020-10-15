package com.CAM.AddonManagement;

public class GameVersionManager {

    private AddonManager classic;
    private AddonManager retail;
    private AddonManager current;

    public GameVersionManager(AddonManager classic, AddonManager retail){
        this.classic = classic;
        this.classic = retail;
    }

    public void setManagingClassic(){
        current = classic;
    }

    public void setManagingRetail(){
        current = retail;
    }

    public AddonManager getCurrentManager(){
        return current;
    }


}
