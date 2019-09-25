package com.CAM.HelperTools;

public class Tools {

    public static String sanatizeInput(String input){
        String sanatized = input;
        String[] illegals = {"?", "!", "\\", "/", ":", "*", "<", ">", "|"};
        for(int i=0; i<illegals.length; i++){
            sanatized = sanatized.replace(illegals[i], "");
        }
        return sanatized;
    }
}
