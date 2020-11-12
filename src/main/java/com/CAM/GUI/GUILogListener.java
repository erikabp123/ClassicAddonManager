package com.CAM.GUI;

import com.CAM.HelperTools.Logging.LogListener;
import javafx.application.Platform;
import javafx.scene.control.TextArea;

public class GUILogListener implements LogListener {

    private final TextArea textArea;

    public GUILogListener(TextArea textArea){
        this.textArea = textArea;
    }

    @Override
    public void notify(String text) {
        Platform.runLater(() -> {
            textArea.appendText(text + "\n");
            textArea.setScrollTop(Double.MAX_VALUE);
        });

    }
}
