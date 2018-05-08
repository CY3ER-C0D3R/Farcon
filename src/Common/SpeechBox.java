/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Common;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

enum SpeechDirection {
    LEFT, RIGHT
}

public class SpeechBox extends HBox {

    private Color DEFAULT_SENDER_COLOR = Color.GOLD;
    private Color DEFAULT_RECEIVER_COLOR = Color.LIMEGREEN;
    //private Color DEFAULT_INFO_COLOR = Color.ALICEBLUE;
    private Background DEFAULT_SENDER_BACKGROUND, DEFAULT_RECEIVER_BACKGROUND, DEFAULT_INFO_BACKGROUND;

    private String message;
    private String messageInfo;
    private SpeechDirection direction;

    private Label displayedText;
    //private Label displayedInfo;
    private SVGPath directionIndicator;

    public SpeechBox(String message, String messageInfo, SpeechDirection direction) {
        this.message = message;
        this.direction = direction;
        this.messageInfo = messageInfo;
        initialiseDefaults();
        setupElements();
    }

    private void initialiseDefaults() {
        DEFAULT_SENDER_BACKGROUND = new Background(
                new BackgroundFill(DEFAULT_SENDER_COLOR, new CornerRadii(5, 0, 5, 5, false), Insets.EMPTY));
        DEFAULT_RECEIVER_BACKGROUND = new Background(
                new BackgroundFill(DEFAULT_RECEIVER_COLOR, new CornerRadii(0, 5, 5, 5, false), Insets.EMPTY));
        //DEFAULT_INFO_BACKGROUND = new Background(
             //   new BackgroundFill(DEFAULT_INFO_COLOR, new CornerRadii(0, 5, 5, 5, false), Insets.EMPTY));
    }

    private void setupElements() {
        displayedText = new Label(message + "\n\n" + messageInfo);
        //displayedInfo = new Label(messageInfo);
        displayedText.setPadding(new Insets(5));
        displayedText.setWrapText(true);
        //displayedInfo.setPadding(new Insets(5));
        //displayedInfo.setWrapText(true);
        directionIndicator = new SVGPath();

        if (direction == SpeechDirection.LEFT) {
            configureForReceiver();
        } else {
            configureForSender();
        }
    }

    private void configureForSender() {
        displayedText.setBackground(DEFAULT_SENDER_BACKGROUND);
        displayedText.setAlignment(Pos.CENTER_RIGHT);
        //displayedInfo.setBackground(DEFAULT_INFO_BACKGROUND);
        //displayedInfo.setAlignment(Pos.CENTER_RIGHT);
        directionIndicator.setContent("M10 0 L0 10 L0 0 Z");
        directionIndicator.setFill(DEFAULT_SENDER_COLOR);

        HBox container = new HBox(displayedText, directionIndicator);
        //container.setAlignment(Pos.CENTER_RIGHT);
        //Use at most 75% of the width provided to the SpeechBox for displaying the message
        container.maxWidthProperty().bind(widthProperty().multiply(0.75));
        getChildren().setAll(container);
        setAlignment(Pos.CENTER_RIGHT);
    }

    private void configureForReceiver() {
        displayedText.setBackground(DEFAULT_RECEIVER_BACKGROUND);
        displayedText.setAlignment(Pos.CENTER_LEFT);
        //displayedInfo.setBackground(DEFAULT_INFO_BACKGROUND);
        //displayedInfo.setAlignment(Pos.CENTER_RIGHT);
        directionIndicator.setContent("M0 0 L10 0 L10 10 Z");
        directionIndicator.setFill(DEFAULT_RECEIVER_COLOR);

        HBox container = new HBox(directionIndicator, displayedText);
        //Use at most 75% of the width provided to the SpeechBox for displaying the message
        container.maxWidthProperty().bind(widthProperty().multiply(0.75));
        getChildren().setAll(container);
        setAlignment(Pos.CENTER_LEFT);
    }
}
