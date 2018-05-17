/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
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
    
    public HashMap<Socket, ObjectOutputStream> clientSockets;
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
        
        this.clientSockets = new HashMap<>();
        
        RemoteConnectionData r = Context.getInstance().getRemote_group_data();
        System.out.println(r);
        if(r != null)
            this.SetRemoteData(Context.getInstance().getRemote_group_data());
        
        try {
            VBox box = FXMLLoader.load(getClass().getResource("/View/GroupCollaborationPageDrawer.fxml"));
            this.drawer.setSidePane(box);
            this.drawer.setDefaultDrawerSize(120);
        } catch (IOException ex) {
            Logger.getLogger(GroupCollaborationPageController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
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
            }
            System.out.println("umm here file name is " + Context.getInstance().getSelectedFileName());
            server = new LocalGroupServer(remote_group_ID, remote_group_Password);
            //server.StartLocalGroupServer();
        }
         
        r = Context.getInstance().getRemote_group_data();
        System.out.println(r);
        if(r != null)
            this.SetRemoteData(Context.getInstance().getRemote_group_data());
        
        System.out.println("Starting the collaboration server...");
        System.out.println("ID,Password I have is:");
        System.out.println(this.remote_group_ID);
        System.out.println(this.remote_group_Password);
       
        if(!Context.getInstance().isGroupHost())  // client is a part of the remote group but not the host
        {
            rgc = new RemoteGroupClient(this.clientName, this.clientID, this, this.remote_group_ID, this.remote_group_Password, this.remote_group_ip, this.remote_group_port);
        }
        file_editor.setFocusTraversable(true);
        
        // add listener to file editor for ctrl+s to save the file
        final KeyCombination keyCombinationCtrlS = new KeyCodeCombination(
                KeyCode.S, KeyCombination.CONTROL_DOWN);
        final KeyCombination keyCombinationCtrlC = new KeyCodeCombination(
                KeyCode.C, KeyCombination.CONTROL_DOWN);

        this.file_editor.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (keyCombinationCtrlS.match(event)) {
                    System.out.println("CTRL + S Pressed");
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
        System.out.println("Setting Socket list : ");
        System.out.println(clientSockets);
    }
    
    public void removeFromSocketList(Socket sock){
        this.clientSockets.remove(sock);
    }
    
    public void startMeeting(){
        if(this.LocalGroupServerIsOpen)
            this.server.StartLocalGroupServer();
    }
    
    public void stopMeeting(){
        System.out.println("rgc stop");
        rgc.stop();
        System.out.println(Context.getInstance().getLastStatus());
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
                                System.out.println("Notification Clicked");
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
        System.out.println(row);
        System.out.println(col);
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
        //String row = position[0];
        //String col = position[1];
        //this.SetCaretPosition(Integer.parseInt(row), Integer.parseInt(col));
        this.file_editor.positionCaret(Integer.parseInt(position));
        this.file_editor.insertText(this.file_editor.getCaretPosition(), content);
        //todo - check how to invalidate
    }
    
    public void DelChar(String content, String position){
        //String row = position[0];
        //String col = position[1];
        int pos = Integer.parseInt(position);
        this.file_editor.positionCaret(pos);
        //this.SetCaretPosition(Integer.parseInt(row), Integer.parseInt(col));
        System.out.println("About to delete");
        //System.out.println(String.format("%s, %s",row,col));
        System.out.println(this.file_editor.getCaretPosition());
        //pos = this.jTextArea1.getCaretPosition();
        try{
            this.file_editor.replaceText(pos,pos+1,"");
        }
        catch(Exception ex){
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
    
    @FXML
    public void keyTyped(KeyEvent e) {
        if (e.getSource() == this.file_editor && e.getCode() != KeyCode.UNDEFINED) {
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
                String position = String.format("Row: %s, Col: %s",rowNum,colNum);
                System.out.println(position);
                
                String operation = "ins";
                position = "";
                
                
                String str = e.getCharacter();
                
                if(str.equals("\n"))
                    str = " ";
                
                int p= this.file_editor.getCaretPosition();
                System.out.println("Here handling keycodes: ");
                System.out.println(e);
                System.out.println(e.getCode().getName());
                //System.out.println(e.getText());
                System.out.println(KeyCode.BACK_SPACE.getName());
                System.out.println(KeyCode.DELETE.getName());
                System.out.println("True or not? ");
                System.out.println(KeyCode.DELETE.impl_getCode() == e.getCode().impl_getCode());
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

                //String data = operation+";"+str+";"+rowNum+";"+colNum; 
                String data = operation + ";" + str + ";" + position;
                System.out.println("data: " + data);
                this.UpdateData(data, null);
            } catch (Exception ex) {
                Logger.getLogger(GroupCollaborationPageController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @FXML
    public void keyPressed(KeyEvent e) {
        System.out.println("KEY PRESSED");
        System.out.println(e.getCode().impl_getCode());
         if (e.getSource() == this.file_editor) {
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
                String position = String.format("Row: %s, Col: %s",rowNum,colNum);
                System.out.println(position);
                
                String operation = "ins";
                position = "";
                
                
                String str = e.getCharacter();
                
                if(str.equals("\n"))
                    str = " ";
                
                int p= this.file_editor.getCaretPosition();
                System.out.println(e.getCode());
                System.out.println(e.getText());
                System.out.println(KeyCode.BACK_SPACE.getName());
                System.out.println(KeyCode.DELETE.getName());
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

                //String data = operation+";"+str+";"+rowNum+";"+colNum; 
                String data = operation + ";" + str + ";" + position;
                System.out.println("data: " + data);
                this.UpdateData(data, null);
            } catch (Exception ex) {
                Logger.getLogger(GroupCollaborationPageController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    @FXML
    public void keyReleased(KeyEvent e) {
        System.out.println("KEY Released");
        System.out.println(e.getCode().impl_getCode());
         if (e.getSource() == this.file_editor && (e.getCode() == KeyCode.DELETE || e.getCode() == KeyCode.BACK_SPACE)) {
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
                String position = String.format("Row: %s, Col: %s",rowNum,colNum);
                System.out.println(position);
                
                String operation = "ins";
                position = "";
                
                
                String str = e.getCharacter();
                
                if(str.equals("\n"))
                    str = " ";
                
                int p= this.file_editor.getCaretPosition();
                System.out.println(e.getCode());
                System.out.println(e.getText());
                System.out.println(KeyCode.BACK_SPACE.getName());
                System.out.println(KeyCode.DELETE.getName());
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

                //String data = operation+";"+str+";"+rowNum+";"+colNum; 
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
                System.err.println(ex.getMessage());
                this.DisplayNotification("Save Error", ex.getMessage());
            }
        }
        else{
            this.DisplayNotification("File Not Saved", "No file was chosen.");
        }
    }    
    
    public void copyToClipboard(){
        // todo
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
                myController.setScreen(main.GroupMeetingID);
            }
        });
        no_btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                dialog.close();
                System.out.println("Canceled. Returning back to group meeting.");
            }
        });
        cancel_btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                dialog.close();
                System.out.println("Canceled. Returning back to group meeting.");
            }
        });
        dialog.show();
    }

}
