package com.CAM.HelperTools.GameSpecific;

public class PatchNumber implements Comparable<PatchNumber> {

    private int[] numbers;

    public PatchNumber(String string){
        String[] numbersAsString = string.split("\\.");
        this.numbers = new int[numbersAsString.length];
        for(int i = 0; i < numbersAsString.length; i++) numbers[i] = Integer.parseInt(numbersAsString[i]);
    }

    @Override
    public int compareTo(PatchNumber o) {
        for(int i = 0; i < numbers.length; i++){
            int result = o.numbers[i]-this.numbers[i];
            if( result != 0) return result;
        }
        return 0;
    }

    public static PatchNumber getHighestPatchNumber(PatchNumber pn1, PatchNumber pn2){
        return pn1.compareTo(pn2) > 0 ? pn1 : pn2;
    }

    public GameVersion getGameVersion(){
        for(GameVersion gameVersion: GameVersion.downloadableVersions()){
            if(toString().startsWith(gameVersion.getPrefix())) return gameVersion;
        }
        return null;
    }

    @Override
    public String toString(){
        StringBuilder string = new StringBuilder();
        for(int i = 0; i < numbers.length; i++) {
            string.append(numbers[i]);
            if(i != numbers.length - 1) string.append(".");
        }
        return string.toString();
    }


}
