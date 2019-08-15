package com.CAM.HelperTools;

public class UserInputResponse {

    private String input;
    private boolean abort;

    public UserInputResponse(String input, boolean abort){
        this.input = input;
        this.abort = abort;
    }

    public String getInput() {
        return input;
    }

    public boolean isAbort() {
        return abort;
    }
}
