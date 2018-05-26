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

package GroupCollaborationPage;

import Common.RemoteConnectionData;
import Common.RemoteConnectionDataInterface;
import GroupMeetingPage.GroupMeetingPageController;
import Common.ControlledScreen;
import Common.ScreensController;
import Main.main;
import Common.Context;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXDrawer;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextArea;
import com.sun.glass.ui.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import org.json.JSONObject;

/**
 * FXML Controller class
 *
 * @author admin
 */
public class GroupCollaborationPageController implements Initializable, ControlledScreen, RemoteConnectionDataInterface {
    
    ScreensController myController;
    
    GroupMeetingPageController g;
    
    RemoteGroupClient rgc;
    
    private LocalGroupServer server;
    private boolean LocalGroupServerIsOpen;
    private boolean consumeKey;
    
    private int currentPosition;  // saves the carets position
    
    public HashMap<Socket, ObjectOutputStream> clientSockets;
    private HashMap<KeyCombination, String> keys;
    private String remote_group_ip;
    private int remote_group_port;
    private String remote_group_ID;
    private String remote_group_Password;
    private String clientName;
    private String clientID;
    
    @FXML
    private JFXTextArea file_editor;
    
    @FXML
    private JFXDrawer drawer;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Context.getInstance().setGc(this);
        g = Context.getInstance().getG();
        clientID = Context.getInstance().getF().getID();
        
        RemoteConnectionData r = Context.getInstance().getRemote_group_data();
        if(r != null)
            this.SetRemoteData(Context.getInstance().getRemote_group_data());
        
        file_editor.setFocusTraversable(true);
        
        this.file_editor.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                keyPressed(event);
                if(consumeKey)
                    event.consume();
            }
        });
        this.file_editor.addEventFilter(KeyEvent.KEY_TYPED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(consumeKey)
                    event.consume();
            }
        });
        this.file_editor.addEventFilter(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(consumeKey)
                    event.consume();
                consumeKey = false;
            }
        });
                
        this.keys = new HashMap<>();
        createKeyCombinations();

        //initializeMeeting();
        
        try {
            VBox box = FXMLLoader.load(getClass().getResource("/View/GroupCollaborationPageDrawer.fxml"));
            this.drawer.setSidePane(box);
            this.drawer.setDefaultDrawerSize(120);
        } catch (IOException ex) {
            Logger.getLogger(GroupCollaborationPageController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // add listener to file editor for ctrl+s to save the file
        final KeyCombination keyCombinationCtrlS = new KeyCodeCombination(
                KeyCode.S, KeyCombination.CONTROL_DOWN);
        final KeyCombination keyCombinationCtrlC = new KeyCodeCombination(
                KeyCode.C, KeyCombination.CONTROL_DOWN);

        this.file_editor.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (keyCombinationCtrlS.match(event)) {
                    SaveFile();
                }
                else if (event.getCode().equals(KeyCode.ESCAPE)){
                    exitGroup();
                }
                else if (keyCombinationCtrlC.match(event)){
                    copyToClipboard();
                }
            }
        });
    }
    
    public void initializeMeeting(){
        // If an unsupported key was typed consume the event,
        // meaning do not allow the key to be typed in the editor.
        consumeKey = false; 
        this.clientSockets = new HashMap<>();
         
        this.LocalGroupServerIsOpen = false;  //becomes true only after registered in Master Server
        if(Context.getInstance().isGroupHost())
        {
            // open file dialog to choose the profile photo
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().addAll(
                    new ExtensionFilter("Text Files", "*.txt")
            );
            File selectedFile = fc.showOpenDialog(null);
            if (selectedFile != null) {
                Context.getInstance().setSelectedFileName(selectedFile.getAbsolutePath());
                server = new LocalGroupServer(remote_group_ID, remote_group_Password);
            }
            else  // no file seleted, cancel the meeting
            {
                Context.getInstance().updateMeetingEnded();
                myController.setScreen(main.GroupMeetingID);
            }
        }
        
        System.out.println("Starting the collaboration server...");
        System.out.println(String.format("My GID and Password are: %s, %s ", this.remote_group_ID, this.remote_group_Password));
       
        if(!Context.getInstance().isGroupHost())  // client is a part of the remote group but not the host
        {
            rgc = new RemoteGroupClient(this.clientName, this.clientID, this, this.remote_group_ID, this.remote_group_Password, this.remote_group_ip, this.remote_group_port);
        }
    }
    
    public void SendMessage(JSONObject jsonObject){
        g.SendMessage(jsonObject, true);
    }
    
    public boolean getLocalGroupServerIsOpen(){
        return this.LocalGroupServerIsOpen;
    }
    
    public String getGroupID(){
        return this.remote_group_ID;
    }
    
    public String getGroupPassword(){
        return this.remote_group_Password;
    }
    
    public void setLocalGroupServerIsOpen(boolean open){
        this.LocalGroupServerIsOpen = open;
    }
    
    public void addToSocketList(Socket sock, ObjectOutputStream ostr) {
        // for a client in the group this list consists only of the host socket,
        // while for the host this list consists of all other clients
        this.clientSockets.put(sock, ostr);
    }
    
    public void removeFromSocketList(Socket sock){
        this.clientSockets.remove(sock);
    }
    
    public void startMeeting(){
        if(this.LocalGroupServerIsOpen)
            this.server.StartLocalGroupServer();
    }
    
    public void stopMeeting(){
        rgc.stop();
        Context.getInstance().UpdateStatusBar(Context.getInstance().getLastStatus(), false);
        
    }
    
    public void DisplayNotification(String title, String text) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Image img = new Image("Resources/falcon_icon.png");
                ImageView imgview = new ImageView(img);
                imgview.setFitHeight(100);
                imgview.setFitWidth(100);
                imgview.preserveRatioProperty();
                Notifications notificationbuilder = Notifications.create()
                        .title(title)
                        .text(text)
                        .graphic(imgview)
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.BOTTOM_RIGHT)
                        .onAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                            }
                        });
                notificationbuilder.darkStyle();
                notificationbuilder.show();
                
                //Notifications.create().title("Test").text("working? or not working?").position(Pos.BOTTOM_RIGHT).show();
            }
        });
    }
    
    @Override
    public void setScreenParent(ScreensController screenParent) {
        myController = screenParent;
    }
    
    @Override
    public void SetRemoteData(RemoteConnectionData rc_data) {
        this.remote_group_ip = rc_data.getRemote_ip();
        this.remote_group_port = rc_data.getRemote_port();
        this.remote_group_ID = rc_data.getRemote_ID();
        this.remote_group_Password = rc_data.getRemote_Password();
        this.clientName = rc_data.getClient_name();
    }    
    
    // collaboration related function sector
    
//    public void setOutputStream(ObjectOutputStream ostr) {
//        this.ostr = ostr;
//    }
    
    public void SetText(String text){
        this.file_editor.setText(text);
    }
    
    public String GetText(){
        return this.file_editor.getText();
    }
    
    public void SetCaretPosition(int row, int col){
        String text = this.file_editor.getText();
        int counter = 0;
        int rowCounter = 0;
        int colCounter = 0;
        while(rowCounter != row){
            Character ch = text.charAt(counter);
            if(ch.equals('\n'))
                rowCounter++;
            counter++;
        }
        
        while (col != 0) {
            counter++;
            col--;
        }
        this.file_editor.positionCaret(counter);
    }
    
    public void AddChar(String content, String position){
        int pos = Integer.parseInt(position);
        // save the caret's current position
        this.currentPosition = this.file_editor.getCaretPosition();
        // add the content
        System.out.println("Here adding char at position: " + pos);
        this.file_editor.positionCaret(pos);
        this.file_editor.insertText(this.file_editor.getCaretPosition(), content);
        // return caret to the position it was before
        if(pos < currentPosition)
            currentPosition = content.length() + currentPosition;
        this.file_editor.positionCaret(currentPosition);
        //todo = add comments
    }
    
    public void DelChar(String content, String position){
        System.out.println("Del Char... ");
        System.out.println("Position is: " + position);
        int pos = Integer.parseInt(position);
        // save the caret's current position
        this.currentPosition = this.file_editor.getCaretPosition();
        try {
            // delete the content
            this.file_editor.positionCaret(pos);
            //if backspace
            if(content.equals("B"))
                this.file_editor.replaceText(pos-1, pos, "");
            //if delete
            else if (content.equals("D"))
                this.file_editor.replaceText(pos, pos+1, "");
            // return caret to the position it was before
            if (pos < currentPosition) {
                currentPosition = currentPosition - 1;  // todo - currentPosition - content.length()
            }
            this.file_editor.positionCaret(currentPosition);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        finally{
            //this.invalidate();
        }
//        if(this.jTextArea1.getCaretPosition() != 0 && content.equals('B'))
//            this.jTextArea1.setCaretPosition(this.jTextArea1.getCaretPosition()-1);
    }
    
    public void UpdateData(String content, Socket sender) {
        for (Socket s: this.clientSockets.keySet()) {
            try {
                if(s != null && s != sender) {
                    OutputStream ostr = this.clientSockets.get(s);
                    ostr.write(content.length());
                    ostr.write(content.getBytes());
                    ostr.flush();
                }
            } catch (IOException ex) {
                Logger.getLogger(RemoteGroupClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    //todo - change parsing way to json instead of token...(=,;)
    public void UpdateData(JSONObject jsonObject, Socket sender) {
        for (Socket s: this.clientSockets.keySet()) {
            try {
                if(s != null && s != sender) {
                    OutputStreamWriter ostr = new OutputStreamWriter(this.clientSockets.get(s), "UTF-8");
                    //ostr.write(jsonObject.toString() + "\n".length());
                    ostr.write(jsonObject.toString() + "\n");
                    ostr.flush();
                }
            } catch (IOException ex) {
                Logger.getLogger(RemoteGroupClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    @FXML
    public void keyTyped(KeyEvent e) {
        System.out.println("Here in key Pressed");
        System.out.println(e.getCode().getName());
        System.out.println(e.getText());
        
        try {
            int caretPos = this.file_editor.getCaretPosition();

            String operation = "";
            String str = "";

            switch (e.getCode().getName()) {
                case "ENTER":
                    str = "\n";
                    operation = "ins";
                    break;
                case "BACK_SPACE":
                    str = "B";
                    operation = "del";
                    break;
                case "DELETE":
                    str = "D";
                    operation = "del";
                    break;
                default:
                    operation = "ins";
                    if(e.getCode().isLetterKey())
                    {
                        if(e.isShiftDown()) // capital letter
                            str = e.getCode().impl_getChar().toUpperCase();
                        else // lowercase letter
                            str = e.getCode().impl_getChar().toLowerCase();
                    }
                    else if (e.getCode().isDigitKey() && !e.isShiftDown())
                    {
                        str = e.getText();
                    }
                    else
                    {
                        str = getKey(e);
                        System.out.println("Str after search in HashMap:");
                        System.out.println(str);
                    }
                    /*
                    else if(e.getCode().isDigitKey() || e.getCode().isWhitespaceKey())
                    {
                        str = e.getCode().impl_getChar();
                        
                        operation = "ins";
                    }
                    */
                    break;
            }
            
            if (str.equals("")) {
                System.out.println("Not supported character");
                e.consume();
                this.DisplayNotification("Not Supported Character", "Please view the supported characters list.");
            }
            else {
                String position = (new Integer(caretPos)).toString();
                String data = operation + ";" + str + ";" + position;
                System.out.println("data: " + data);
                this.UpdateData(data, null);
            }
        } catch (Exception ex) {
            Logger.getLogger(GroupCollaborationPageController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @SuppressWarnings("deprecation")
    public String getKeyChar(KeyCode k){
        String key = "";
        System.out.println("Here in getKeyChar");
        System.out.println(k.impl_getCode());
        System.out.println(KeyCode.EXCLAMATION_MARK.impl_getCode());
        //System.out.println((char)33);
        switch (k) {
            case EXCLAMATION_MARK:
                key = "!";
                break;
            case AT:
                key = "@";
                break;
            case NUMBER_SIGN:
                key = "#";
                break;
            case DOLLAR:
                key = "$";
                break;
            case CIRCUMFLEX:
                key = "^";
                break;
            case AMPERSAND:
                key = "&";
                break;
            case ASTERISK:
                key = "*";
                break;
            case LEFT_PARENTHESIS:
                key = "(";
                break;
            case RIGHT_PARENTHESIS:
                key = ")";
                break;
            case MINUS:
                key = "-";
                break;
            case UNDERSCORE:
                key = "_";
                break;
            case EQUALS:
                key = "=";
                break;
            case PLUS:
                key = "+";
                break;
            default:
                break;
        }
        return key;
    }
    
    @SuppressWarnings("deprecation")
    public String getKey(KeyEvent e){
        for(KeyCombination k : this.keys.keySet()){
            if(k.match(e))
                return this.keys.get(k);
        }
        return "Not Found";
    }
    
    public void createKeyCombinations(){
        // if key value is equal to "" that means the key 
        // will be accepted by the editor but will not be sent
        
        // add a few key combinations to make the user experience better
        KeyCombination k;
        // SHIFT + 1 = !
        k = new KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.SHIFT_DOWN);
        this.keys.put(k, "!");
        // SHIFT + 2 = @
        k = new KeyCodeCombination(KeyCode.DIGIT2, KeyCombination.SHIFT_DOWN);
        this.keys.put(k, "@");
        // SHIFT + 3 = #
        k = new KeyCodeCombination(KeyCode.DIGIT3, KeyCombination.SHIFT_DOWN);
        this.keys.put(k, "#");
        // SHIFT + 4 = $
        k = new KeyCodeCombination(KeyCode.DIGIT4, KeyCombination.SHIFT_DOWN);
        this.keys.put(k, "$");
        // SHIFT + 5 = %
        k = new KeyCodeCombination(KeyCode.DIGIT5, KeyCombination.SHIFT_DOWN);
        this.keys.put(k, "%");
        // SHIFT + 6 = ^
        k = new KeyCodeCombination(KeyCode.DIGIT6, KeyCombination.SHIFT_DOWN);
        this.keys.put(k, "^");
        // SHIFT + 7 = &
        k = new KeyCodeCombination(KeyCode.DIGIT7, KeyCombination.SHIFT_DOWN);
        this.keys.put(k, "&");
        // SHIFT + 8 = *
        k = new KeyCodeCombination(KeyCode.DIGIT8, KeyCombination.SHIFT_DOWN);
        this.keys.put(k, "*");
        // SHIFT + 9 = (
        k = new KeyCodeCombination(KeyCode.DIGIT9, KeyCombination.SHIFT_DOWN);
        this.keys.put(k, "(");
        // SHIFT + 0 = )
        k = new KeyCodeCombination(KeyCode.DIGIT0, KeyCombination.SHIFT_DOWN);
        this.keys.put(k, ")");
        // SHIFT + '-' = '_'
        k = new KeyCodeCombination(KeyCode.MINUS, KeyCombination.SHIFT_DOWN);
        this.keys.put(k, "_");
        // SHIFT + '=' = '+'
        k = new KeyCodeCombination(KeyCode.EQUALS, KeyCombination.SHIFT_DOWN);
        this.keys.put(k, "+");
        // SHIFT + '\' = '|'
        k = new KeyCodeCombination(KeyCode.BACK_SLASH, KeyCombination.SHIFT_DOWN);
        this.keys.put(k, "|");
        // SHIFT + ']' = '}'
        k = new KeyCodeCombination(KeyCode.CLOSE_BRACKET, KeyCombination.SHIFT_DOWN);
        this.keys.put(k, "}");
        // SHIFT + '[' = '{'
        k = new KeyCodeCombination(KeyCode.OPEN_BRACKET, KeyCombination.SHIFT_DOWN);
        this.keys.put(k, "{");
        // SHIFT + ',' = '<'
        k = new KeyCodeCombination(KeyCode.COMMA, KeyCombination.SHIFT_DOWN);
        this.keys.put(k, "<");
        // SHIFT + '.' = '>'
        k = new KeyCodeCombination(KeyCode.PERIOD, KeyCombination.SHIFT_DOWN);
        this.keys.put(k, ">");
        // SHIFT + '/' = '?'
        k = new KeyCodeCombination(KeyCode.SLASH, KeyCombination.SHIFT_DOWN);
        this.keys.put(k, "?");
        // SHIFT + ';' = ':'
        k = new KeyCodeCombination(KeyCode.SEMICOLON, KeyCombination.SHIFT_DOWN);
        this.keys.put(k, ":");
        // SHIFT + ' = "
        k = new KeyCodeCombination(KeyCode.QUOTE, KeyCombination.SHIFT_DOWN);
        this.keys.put(k, String.format("%c",'"'));
        // SHIFT + '`' = '~'
        k = new KeyCodeCombination(KeyCode.BACK_QUOTE, KeyCombination.SHIFT_DOWN);
        this.keys.put(k, "~");
        
        // Other non-shift related keys
        k = new KeyCodeCombination(KeyCode.MINUS);
        this.keys.put(k, "-");
        k = new KeyCodeCombination(KeyCode.EQUALS);
        this.keys.put(k, "=");
        k = new KeyCodeCombination(KeyCode.BACK_SLASH);
        this.keys.put(k, "\\");
        k = new KeyCodeCombination(KeyCode.CLOSE_BRACKET);
        this.keys.put(k, "]");
        k = new KeyCodeCombination(KeyCode.OPEN_BRACKET);
        this.keys.put(k, "[");
        k = new KeyCodeCombination(KeyCode.COMMA);
        this.keys.put(k, ",");
        k = new KeyCodeCombination(KeyCode.PERIOD);
        this.keys.put(k, ".");
        k = new KeyCodeCombination(KeyCode.SLASH);
        this.keys.put(k, "/");
        k = new KeyCodeCombination(KeyCode.COLON);
        this.keys.put(k, ":");
        k = new KeyCodeCombination(KeyCode.SEMICOLON);
        this.keys.put(k, ";");
        k = new KeyCodeCombination(KeyCode.QUOTE);
        this.keys.put(k, "'");
        k = new KeyCodeCombination(KeyCode.BACK_QUOTE);
        this.keys.put(k, "`");
        k = new KeyCodeCombination(KeyCode.HOME);
        this.keys.put(k, "");
        k = new KeyCodeCombination(KeyCode.END);
        this.keys.put(k, "");
        k = new KeyCodeCombination(KeyCode.SPACE);
        this.keys.put(k, " ");
        k = new KeyCodeCombination(KeyCode.TAB);
        this.keys.put(k, "  ");
        
        // Numpad options
        k = new KeyCodeCombination(KeyCode.ASTERISK);
        this.keys.put(k, "*");
        k = new KeyCodeCombination(KeyCode.MULTIPLY);
        this.keys.put(k, "*");
        k = new KeyCodeCombination(KeyCode.DIVIDE);
        this.keys.put(k, "/");
        k = new KeyCodeCombination(KeyCode.ADD);
        this.keys.put(k, "+");
        k = new KeyCodeCombination(KeyCode.SUBTRACT);
        this.keys.put(k, "-");
        k = new KeyCodeCombination(KeyCode.DECIMAL);
        this.keys.put(k, ".");
    }
    
    @FXML
    public void keyPressed(KeyEvent e) {
        
        System.out.println("Here in key Pressed");
        System.out.println(e.getCode());
        System.out.println(e.getText());
        
        try {
            int caretPos = this.file_editor.getCaretPosition();

            String operation = "";
            String str = "";

            switch (e.getCode()) {
                case ENTER:
                    str = "\n";
                    operation = "ins";
                    break;
                case BACK_SPACE:
                    str = "B";
                    operation = "del";
                    break;
                case DELETE:
                    str = "D";
                    operation = "del";
                    break;
                default:
                    operation = "ins";
                    if(e.getCode().isLetterKey())
                    {
                        if(e.isShiftDown()) // capital letter
                            str = e.getCode().impl_getChar().toUpperCase();
                        else // lowercase letter
                            str = e.getCode().impl_getChar().toLowerCase();
                    }
                    else if (e.getCode().isDigitKey() && !e.isShiftDown())
                    {
                        str = e.getText();
                    }
                    else
                    {
                        str = getKey(e);
                        System.out.println("Str after search in HashMap:");
                        System.out.println(str);
                    }
                    /*
                    else if(e.getCode().isDigitKey() || e.getCode().isWhitespaceKey())
                    {
                        str = e.getCode().impl_getChar();
                        
                        operation = "ins";
                    }
                    */
                    break;
            }
            System.out.println("Str is: ");
            System.out.println(str);
            if (str.equals("Not Found") && !e.getCode().equals(KeyCode.SHIFT) && !e.getCode().isArrowKey()) {
                System.out.println("Not supported character");
                consumeKey = true;
                this.DisplayNotification("Not Supported Character/Action", "Please view the supported characters and action list.");
            }
            else if (str.equals("") || str.equals("Not Found")){
                // do not send empty operations (SHIFT for example)
            }
            else {
                String position = (new Integer(caretPos)).toString();
                String data = operation + ";" + str + ";" + position;
                System.out.println("data: " + data);
                this.UpdateData(data, null);
            }
        } catch (Exception ex) {
            Logger.getLogger(GroupCollaborationPageController.class.getName()).log(Level.SEVERE, null, ex);
        }
        /*
        System.out.println("Key Pressed");
        System.out.println(e.getCode());
        System.out.println(e.getText());
        System.out.println(e.getCode().impl_getChar());
        System.out.println(e.getCode().impl_getChar().equals("A"));
        System.out.println(e.getCode().equals(KeyCode.ENTER));
        if (e.getSource() == this.file_editor) {
            System.out.println("פצצה");
            try {
                int caretPos = this.file_editor.getCaretPosition();
                int rowNum = 0;
                int colNum = 0;
                String text = this.file_editor.getText();
                int counter = 0;
                while(counter != caretPos) {
                    Character ch = text.charAt(counter);
                    if (ch.equals('\n')) {
                        rowNum++;
                        colNum = 0;
                    }
                    else
                        colNum++;
                    counter++;
                }
                
                String operation = "ins";
                String position = "";
                String str = e.getCharacter();
                
                if(str.equals("\n"))
                    str = " ";
                
                int p= this.file_editor.getCaretPosition();
                if(e.getCode().equals(KeyCode.BACK_SPACE))
                {
                    str = "B";
                    operation = "del";
                    //if(p > 0)
                      //  p--;
                } 
                else if(e.getCode().equals(KeyCode.DELETE))
                {
                    str = "D";
                    operation = "del";
                }
                position = (new Integer(p)).toString();

                //String data = operation+";"+str+";"+position; 
                String data = operation + ";" + str + ";" + position;
                System.out.println("data: " + data);
                this.UpdateData(data, null);
            } catch (Exception ex) {
                Logger.getLogger(GroupCollaborationPageController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }*/
    }
    
    @FXML
    public void keyReleased(KeyEvent e) {
        System.out.println("key released");
        System.out.println(e.getCode());
         if (e.getSource() == this.file_editor){ // && (e.getCode() == KeyCode.DELETE || e.getCode() == KeyCode.BACK_SPACE)) {
            System.out.println("פצצה");
            try {
                int caretPos = this.file_editor.getCaretPosition();
                int rowNum = 0;
                int colNum = 0;
                String text = this.file_editor.getText();
                int counter = 0;
                while(counter != caretPos) {
                    Character ch = text.charAt(counter);
                    if (ch.equals('\n')) {
                        rowNum++;
                        colNum = 0;
                    }
                    else
                        colNum++;
                    counter++;
                }
				
                String operation = "ins";
                String position = "";
                String str = e.getCharacter();
                
                if(str.equals("\n"))
                    str = " ";
                
                int p= this.file_editor.getCaretPosition();
                if(e.getCode().equals(KeyCode.BACK_SPACE))
                {
                    str = "B";
                    operation = "del";
                    //if(p > 0)
                      //  p--;
                } 
                else if(e.getCode().equals(KeyCode.DELETE))
                {
                    str = "D";
                    operation = "del";
                }
                position = (new Integer(p)).toString();

                //String data = operation+";"+str+";"+position; 
                String data = operation + ";" + str + ";" + position;
                System.out.println("data: " + data);
                this.UpdateData(data, null);
            } catch (Exception ex) {
                Logger.getLogger(GroupCollaborationPageController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

//    public void keyPressed(KeyEvent e) {
//        if (e.getCode() == KeyCode.RIGHT || e.getCode() == KeyCode.LEFT
//                || e.getCode() == KeyCode.UP || e.getCode() == KeyCode.DOWN) {
//            try {
//                int caretPos = this.file_editor.getCaretPosition();
//                int rowNum = (caretPos == 0) ? 1 : 0;
//                for (int offset = caretPos; offset > 0;) {
//                    offset = Utilities.getRowStart(jTextArea1, offset) - 1;
//                    rowNum++;
//                }
//                int offset = Utilities.getRowStart(jTextArea1, caretPos);
//                int colNum = caretPos - offset + 1;
//                String position = String.format("Row: %s, Col: %s", rowNum, colNum);
//
//                this.file_editor.setText(position + " " + e.getCharacter());
//            } catch (BadLocationException ex) {
//                Logger.getLogger(FileEditor.class.getName()).log(Level.SEVERE, null, ex);
//
//            }
//        }
//    }  
    
    
    // Drawer related functions
    
    @FXML
    public void mouseEnteredDrawer(MouseEvent event){
        drawer.open();
    } 
    
    @FXML
    public void mouseExitedDrawer(MouseEvent event){
        drawer.close();
    }
    
    public void SaveFile(){
        // get the current text and ask the user where to save it
        String content = this.GetText().replaceAll("\n", System.getProperty("line.separator")); 
        FileChooser fileChooser = new FileChooser();

        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Text files", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);

        //Show save file dialog
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try {
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write(content);
                fileWriter.close();
                this.DisplayNotification("File Saved", "Text saved successfully to " + file.getName());
            } catch (IOException ex) {
                this.DisplayNotification("Save Error", ex.getMessage());
            }
        }
        else{
            this.DisplayNotification("File Not Saved", "No file was chosen.");
        }
    }    
    
    public void copyToClipboard() {
        // copy the current text to the clipboard
        try {
            String content = this.GetText().replaceAll("\n", System.getProperty("line.separator"));
            StringSelection selection = new StringSelection(content);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, selection);
            this.DisplayNotification("Copy", "Copied text to clipboard.");
        } catch (Exception ex) {
            this.DisplayNotification("Copy Error", "Couldn't copy text to clipboard");
        }
    }
    
    public void exitGroup() {
        // ask user if he really wants to exit the group
        JFXDialogLayout content = new JFXDialogLayout();
        content.setHeading(new Label("Exit Group?"));
        Image img = new Image("Resources/falcon_icon.png");
        ImageView imgview = new ImageView(img);
        imgview.setFitHeight(70);
        imgview.setFitWidth(70);
        imgview.preserveRatioProperty();

        JFXButton yes_btn = new JFXButton("Yes");
        JFXButton no_btn = new JFXButton("No");
        JFXButton cancel_btn = new JFXButton("Cancel");

        VBox vb = new VBox();
        HBox hb1 = new HBox();
        ImageView imgview2 = new ImageView(new Image("Resources/falcon_icon.png"));
        imgview2.setFitHeight(70);
        imgview2.setFitWidth(70);
        imgview2.preserveRatioProperty();
        imgview2.setVisible(false);
        hb1.getChildren().addAll(yes_btn, no_btn, cancel_btn);
        hb1.setPadding(new Insets(10, 10, 10, 10));
        hb1.setSpacing(10.0);
        vb.getChildren().addAll(new Label("Quit Group Meeting?\nFile will not be saved.", imgview));
        content.setBody(vb);
        content.setActions(hb1);
        JFXDialog dialog = new JFXDialog(this.myController, content, JFXDialog.DialogTransition.CENTER);
        dialog.setOverlayClose(false); // don't close dialog unless buttons have been pressed
        // set action events for buttons and for dialog itself
        yes_btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // close socket connection and return to home page
                if(Context.getInstance().isGroupHost()) // close the collaboration server
                    LocalGroupServerIsOpen = false;
                else  // client in chat, close connection
                    stopMeeting();
                dialog.close();
                Context.getInstance().updateMeetingEnded();
                myController.setScreen(main.GroupMeetingID);
            }
        });
        no_btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                dialog.close();
            }
        });
        cancel_btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                dialog.close();
            }
        });
        dialog.show();
    }
}
