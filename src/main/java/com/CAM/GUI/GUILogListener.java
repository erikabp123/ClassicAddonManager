package com.CAM.GUI;

import com.CAM.HelperTools.LogListener;
import javafx.scene.control.TextArea;

public class GUILogListener implements LogListener {

    private final TextArea textArea;

    public GUILogListener(TextArea textArea){
        this.textArea = textArea;
    }

    @Override
    public void notify(String text) {
        textArea.setText(textArea.getText() + text + "\n");
    }
}
