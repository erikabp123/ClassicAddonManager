package com.CAM.GUI;

import com.CAM.AddonManagement.UpdateProgressListener;
import javafx.application.Platform;

public class GUIUpdateProgressListener implements UpdateProgressListener {

    Controller controller;

    public GUIUpdateProgressListener(Controller controller) {
        this.controller = controller;
    }

    @Override
    public void informStart(int position) {
        Platform.runLater(() -> {
            // Set result of previous
            String curText = controller.listItems.get(position);
            String[] parts = curText.split(":");
            curText = parts[parts.length - 1];
            String updatedText = "P:" + curText;
            controller.listItems.set(position, updatedText);
        });
    }

    @Override
    public void informFinish(int position, int statusCode) {
        Platform.runLater(() -> {
            // Set result of previous
            String curText = controller.listItems.get(position);
            String[] parts = curText.split(":");
            curText = parts[parts.length - 1];
            String updatedText = "";
            switch (statusCode) {
                case 0:
                    updatedText = "F:";
                    break;
                case 1:
                    updatedText = "S:";
                    break;
                case 2:
                    break;
            }
            updatedText = updatedText + curText;
            controller.listItems.set(position, updatedText);
        });
    }

}
