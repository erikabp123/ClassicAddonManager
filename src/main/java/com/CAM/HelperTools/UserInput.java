package com.CAM.HelperTools;

public interface UserInput {

    UserInputResponse getUserInput(String title);

    boolean askToProceedPrompt(String title, String header, String content);

}
