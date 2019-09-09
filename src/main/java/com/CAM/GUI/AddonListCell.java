package com.CAM.GUI;

import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;

public class AddonListCell<T> extends ListCell<T> {

    private ImageView statusView;
    private Image processingImage;
    private Image successImage;
    private Image failureImage;
    private Image pendingImage;

    public AddonListCell(){
        statusView = new ImageView();
        statusView.setPreserveRatio(true);
        statusView.setFitHeight(20);
        processingImage = new Image(this.getClass().getClassLoader().getResource("processingAddon.gif").toExternalForm());
        successImage = new Image(this.getClass().getClassLoader().getResource("success.png").toExternalForm());
        failureImage = new Image(this.getClass().getClassLoader().getResource("failure.png").toExternalForm());
        pendingImage = new Image(this.getClass().getClassLoader().getResource("pending.png").toExternalForm());
    }

    @Override
    protected void updateItem(T item, boolean empty) {
        setPrefHeight(22);
        super.updateItem(item, empty);
        if(empty){
            setText(null);
            setGraphic(null);
            return;
        }
        if(item.getClass() != String.class){
            return;
        }
        String itemStr = (String) item;
        String text = itemStr.substring(2);
        if(itemStr.startsWith("P:")){
            statusView.setImage(processingImage);
        } else if(itemStr.startsWith("S:")){
            statusView.setImage(successImage);
        } else if(itemStr.startsWith("F:")){
            statusView.setImage(failureImage);
        } else {
            text = itemStr;
            statusView.setImage(pendingImage);
        }
        setGraphic(statusView);
        setText(text);
    }


}
