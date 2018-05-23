/*
 * Farcon Software
 *
 * This program is a Group Collaboration and
 * Remote Control Software, free of charge,
 * for personal or commercial use.
 *
 * Open source, code written in javafx.
 * Written by: Yuval Stein @CY3ER-C0D3R
 *
 * https://github.com/CY3ER-C0D3R/Farcon
 *
 * 2018 (c) Farcon
 */

package Common;

import OnlineChatPage.OnlineChatInterface;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

/**
 *
 * @author admin
 */
public class ConversationView extends VBox {
    
    private OnlineChatInterface oci;

    private String conversationPartner;
    private ObservableList<Node> speechBubbles = FXCollections.observableArrayList();

    private Label contactHeader;
    private ScrollPane messageScroller;
    private VBox messageContainer;
    private HBox inputContainer;

    public ConversationView(String conversationPartner, OnlineChatInterface oci) {
        super(5);
        this.oci = oci;
        this.conversationPartner = conversationPartner;
        setupElements();
    }

    private void setupElements() {
        setupContactHeader();
        setupMessageDisplay();
        setupInputDisplay();
        getChildren().setAll(contactHeader, messageScroller, inputContainer);
        setPadding(new Insets(5));
        this.setPrefSize(456.0, 634.0);
    }

    private void setupContactHeader() {
        contactHeader = new Label(conversationPartner);
        contactHeader.setAlignment(Pos.CENTER);
        contactHeader.setFont(Font.font("Comic Sans MS", 14));
    }

    private void setupMessageDisplay() {
        messageContainer = new VBox(5);
        Bindings.bindContentBidirectional(speechBubbles, messageContainer.getChildren());
        
        messageScroller = new ScrollPane(messageContainer);
        messageScroller.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        messageScroller.setHbarPolicy(ScrollBarPolicy.NEVER);
        messageScroller.setPrefHeight(1500);
        messageScroller.prefWidthProperty().bind(messageContainer.prefWidthProperty().subtract(5));
        messageScroller.setFitToWidth(true);
        //Make the scroller scroll to the bottom when a new message is added
        speechBubbles.addListener((ListChangeListener<Node>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    messageScroller.setVvalue(messageScroller.getVmax());
                }
            }
        });
    }

    private void setupInputDisplay() {
        inputContainer = new HBox(5);

        inputContainer.prefWidthProperty().bind(this.widthProperty());
        inputContainer.prefHeightProperty().bind(this.heightProperty());
        
        TextField userInput = new TextField();
        userInput.setPromptText("Enter message");
        userInput.setOnKeyPressed(event -> {
            // if enter key was pressed, send the message
            if (event.getCode().equals(KeyCode.ENTER) && !userInput.getText().equals("")) {
                sendMessage(userInput.getText());
                userInput.setText("");
            }
        });
        
        Button sendMessageButton = new Button("Send");
        sendMessageButton.disableProperty().bind(userInput.lengthProperty().isEqualTo(0));
        sendMessageButton.setOnAction(event -> {
            sendMessage(userInput.getText());
            userInput.setText("");
        });

        inputContainer.getChildren().setAll(userInput, sendMessageButton);
    }
    
    public String getCurrentDateTime(){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/YY HH:mm");
	LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }

    public void sendMessage(String message) {
        String messageInfo = "Me  " + getCurrentDateTime();
        speechBubbles.add(new SpeechBox(message, messageInfo, SpeechDirection.RIGHT));
        oci.sendMessageToServer(message, getCurrentDateTime());
    }
    
    public void receiveMessage(String sender_username, String message, String messageInfo) {
        speechBubbles.add(new SpeechBox(message, messageInfo, SpeechDirection.LEFT));
    }
}
