/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GroupMeetingPage;

import GroupCollaborationPage.LocalGroupServer;
import Common.RemoteConnectionData;
import Common.ControlledScreen;
import Common.ScreensController;
import SignUpPage.SignUp_FormController;
import Main.main;
import Main.FXMLDocumentController;
import Common.Context;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXHamburger;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXRadioButton;
import com.jfoenix.controls.JFXTextField;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import javax.imageio.ImageIO;
import org.controlsfx.control.Notifications;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * FXML Controller class
 *
 * @author admin
 */
public class GroupMeetingPageController implements Initializable, ControlledScreen {

    public ScreensController myController;
    RemoteConnectionData rc_group_data;
    
    static GroupMeetingPageController singleton = null;
    
    private Socket clientsock;
    private OutputStreamWriter output;
    private BufferedReader input;
    
    public LocalGroupServer server;
    
    private String my_gid;
    private String my_group_password;
    private String remote_group_id;
    private String remote_group_password;
    private LocalGroupServer group_server;
    private Notifications notificationbuilder;
    private String lastStatus;
    private Timer timer;
    
    private String clientName;
    
    @FXML
    private Label label;
    
//    @FXML
//    private JFXHamburger hamburger;
    
    @FXML
    private Circle status_circle;
    
    @FXML
    private Label status_label;
    
    @FXML
    private Label gid_field;
    
    @FXML
    private Label group_password_field;

    @FXML
    private JFXTextField id_combobox;
    
    @FXML
    private JFXTextField meeting_name_field;
    
    @FXML
    private JFXRadioButton collaboration_radio_btn;

    @FXML
    private JFXRadioButton presentation_radio_btn;
   
    @FXML
    private JFXButton start_meeting_btn;
    
    @FXML
    private ImageView profile_photo_img;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        singleton = this;
        Context.getInstance().setG(singleton);
        this.id_combobox.setFocusTraversable(true);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                id_combobox.requestFocus();
            }
        });
        this.clientsock = Context.getInstance().getClientsock();
        this.output = Context.getInstance().getOutput();
        this.input = Context.getInstance().getInput();
        System.out.println("Here in group meeting page initiaize, output and input seem to be umm");
        System.out.println(output);
        System.out.println(input);
        System.out.println(Context.getInstance().isConnectedToMasterServer());
        this.lastStatus = Context.getInstance().getLastStatus();
        updateStatusBar();
        // after updating the status bar check for more updates about the 
        // group meeting information, using the updateGroupMeetingDetails()
        // function.
    }  
    
    public void updateStatusBar() {
        // function updates the status bar according to the data in Context
        this.status_label.setText(Context.getInstance().getStatusBarStatus());
        this.status_circle.setStyle(Context.getInstance().getStatusBarStyle());
        // based on the color of the status circle decide if connect or not
        if(this.status_circle.getStyle().equals("-fx-fill: #e60000")) // red
        {
            this.my_gid = null;
            this.my_group_password = null;
        }
        updateGroupMeetingDetails();
    }
    
    public void updateGroupMeetingDetails(){
        this.clientsock = Context.getInstance().getClientsock();
        this.output = Context.getInstance().getOutput();
        this.input = Context.getInstance().getInput();
        if(this.my_gid == null && this.my_group_password == null)
            requestGroupMeetingDetails();
    }
    
    public void SetProfilePicture(boolean signedIn) {
        BufferedImage bufferedImage = null;
        try {
            if(signedIn)
                bufferedImage = ImageIO.read(new File(Paths.get(".").toAbsolutePath().normalize().toString() + "/build/classes/Resources/007-profile-photo.png"));
            else //use default picture
                bufferedImage = ImageIO.read(new File(Paths.get(".").toAbsolutePath().normalize().toString() + "/build/classes/Resources/004-user.png"));
        } catch (IOException ex) {
            Logger.getLogger(SignUp_FormController.class.getName()).log(Level.SEVERE, null, ex);
        }
        Image img = SwingFXUtils.toFXImage(bufferedImage, null);
        this.profile_photo_img.setImage(img);
    }
    
    @Override
    public void setScreenParent(ScreensController screenParent) {
        myController = screenParent;
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
    
    public void SendMessage(JSONObject jsonObject, boolean displayMessage){
        try {
            if(Context.getInstance().isConnectedToMasterServer())
            {
                output.write(jsonObject.toString() + "\n");
                output.flush();
            }
            else 
            {
                System.out.println("Didn't preform action, Not connected to Master Server.");
                if(displayMessage)
                    DisplayNotification("Error", "Couldn't Preform action, not connected to Master Server");
            }
        } catch (IOException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void requestGroupMeetingDetails(){
        //
        System.out.println("Requesting Group ID and session password...");
        JSONObject jsonObject = new JSONObject();
        JSONObject parameters = new JSONObject();
        try {
            jsonObject.put("Action", "init-group-server");
            jsonObject.put("Parameters", parameters); //no parameters are required in init server 
        } catch (JSONException ex) {
            Logger.getLogger(GroupMeetingPageController.class.getName()).log(Level.SEVERE, null, ex);
        }
        SendMessage(jsonObject, false);
        // update GUI after getting the correct information from the Master Server
        //this.updateGroupMeetingDetails();
    }
    
    public void updateGroupMeetingDetails(String group_id, String group_password){
        this.my_gid = group_id;
        this.my_group_password = group_password;
        this.gid_field.setText(my_gid);
        this.group_password_field.setText(my_group_password);
        Context.getInstance().setRemote_group_data(new RemoteConnectionData(null, 0, my_gid, my_group_password));
        //group_server = new LocalGroupServer(my_gid, my_group_password);
    }    
    
//    public void setLocalGroupServerStatus(boolean status){
//        //this.LocalGroupServerIsOpen = status; // true or false
//        //System.out.println("status changed to " + status);
//    }
    
    @FXML
    private void startGroupMeeting(ActionEvent event){
        // todo - change text to 'cancel' and allow user to cancel a meeting 
        // that started
        this.start_meeting_btn.setDisable(true);
        String type;
        if(this.collaboration_radio_btn.isSelected())
            type = "collaboration";
        else
            type = "presentation";
        if(Context.getInstance().isConnectedToMasterServer())
        {
            //this.server = new LocalGroupServer(my_gid, my_group_password, type);
            // change to collaboration page
            Context.getInstance().setGroupHost(true);  // client will be the host of the group meeting
            startCollaborationMeeting();
        }
        else
        {
            DisplayNotification("Error", String.format("Cannot start %s Meeting.\nNot connected to Master Server", type));
            this.start_meeting_btn.setDisable(false);
        }
    }
     
    public void startCollaborationMeeting(){
        main.mainContainer.loadScreen(main.GroupCollaborationID, main.GroupCollaborationPageFile);
        myController.setScreen(main.GroupCollaborationID);
    }
        
    public void stopGroupMeeting(String status){
        // stop group meeting and display the status why meeting stoped
        System.out.println("Here, stopping group meeting");
        this.start_meeting_btn.setDisable(false);
        String type = this.rc_group_data.getType();
        System.out.println("Group meeting type: " + type);
        
        if (type.equals("collaboration")){
            if(Context.getInstance().getGc() != null)
                Context.getInstance().getGc().stopMeeting();
        }
        DisplayNotification("Session Ended", status);
        //this.ChangeToRemoteControl(null);
        // return status bar to last status
        lastStatus = Context.getInstance().getLastStatus();
        Context.getInstance().UpdateStatusBar(lastStatus, false);
        // go back to the group meeting page
        myController.setScreen(main.GroupMeetingID); 
    }
    
    public void startCollaborationMeetingWithFileSelection(){
        // todo add file selection        
        String selectedFile = "C:\\Users\\admin\\Desktop\\FileTesting\\new\\test.txt";
        Context.getInstance().setSelectedFileName(selectedFile);
        System.out.println("umm here file name is " + Context.getInstance().getSelectedFileName());
        startCollaborationMeeting();
    }
    
    @FXML
    private void requestconnectionToRemoteGroupServer(Event event) {
        String remote_gid = this.id_combobox.getText();
        clientName = this.meeting_name_field.getText();
        String remote_group_name = "";
        // if name not entered do not allow connection // todo - check for duplicate names in group
        if (clientName.equals("")) {
            this.DisplayNotification("Invalid Name", "Please enter a temporary group name\nto identify in group.");
        } else {
            // check if id entered is username or not
            if (Pattern.matches(".*[a-zA-Z].*", remote_gid)) {
                remote_group_name = remote_gid;
                remote_gid = "";
                System.out.println("Remote Username entered: " + remote_group_name);
            }
            if (!Context.getInstance().isConnectedToMasterServer()) {
                DisplayNotification("Error", "Couldn't connect to remote host since\n not connected to Master Server");
            } else if (!remote_gid.equals("") || !remote_group_name.equals("")) {
                // update status bar 
                System.out.println("Connecting to remote Group server");

                Context.getInstance().UpdateStatusBar("Connecting to remote Group server...", false);
                //updateStatusBar("Connecting to remote Group server...", false);

                //request ip, port for the remote server from the Master Server 
                JSONObject jsonObject = new JSONObject();
                JSONObject parameters = new JSONObject();
                try {
                    jsonObject.put("Action", "join-group-request");
                    parameters.put("GID", remote_gid);
                    parameters.put("GroupName", remote_group_name);
                    jsonObject.put("Parameters", parameters);
                } catch (JSONException ex) {
                    Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                }
                SendMessage(jsonObject, true);
            } else // display a notification
            {
                this.DisplayNotification("Invalid group ID", "Please enter a name or a valid group ID number");
            }
        }
    }

    public void connectToRemoteGroupServer(String ip, int port, String remote_GID, String type){
        // update status bar 
        System.out.println("Group server authentication, Password needs to be entered.");
        
        Context.getInstance().UpdateStatusBar("Authenticating...", false);
        //updateStatusBar("Authenticating...", false);
        
        //request password from user
        JFXDialogLayout content = new JFXDialogLayout();
        content.setHeading(new Label("FARCON Authentication"));
        Image img = new Image("Resources/falcon_icon.png");
        ImageView imgview = new ImageView(img);
        imgview.setFitHeight(70);
        imgview.setFitWidth(70);
        imgview.preserveRatioProperty();
        
        JFXButton log_on_btn = new JFXButton("Log On");
        JFXButton cancel_btn = new JFXButton("Cancel");
        
        VBox vb = new VBox();
        HBox hb1 = new HBox();
        HBox hb2 = new HBox();
        ImageView imgview2 = new ImageView(new Image("Resources/falcon_icon.png"));
        imgview2.setFitHeight(70);
        imgview2.setFitWidth(70);
        imgview2.preserveRatioProperty();
        imgview2.setVisible(false);
        JFXPasswordField rc_password_field = new JFXPasswordField();
        hb1.getChildren().addAll(new Label("Password: ", imgview2), rc_password_field);
        hb2.getChildren().addAll(log_on_btn, cancel_btn);
        //hb1.setPadding(new Insets(10,10,10,10));
        hb1.setAlignment(Pos.CENTER);
        hb2.setPadding(new Insets(10,10,10,10));
        hb1.setSpacing(10.0);
        hb2.setSpacing(10.0);
        vb.getChildren().addAll(new Label("Please Enter the Group Password", imgview), hb1);
        content.setBody(vb);
        content.setActions(hb2);
        JFXDialog dialog = new JFXDialog(this.myController, content, JFXDialog.DialogTransition.CENTER);
        dialog.setOverlayClose(false); // don't close dialog unless buttons have been pressed
        // set action events for buttons and for dialog itself
        log_on_btn.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                remote_group_password = rc_password_field.getText();
                dialog.close();
                System.out.println("Password entered: " + remote_group_password);
                System.out.println("Authenticating...");
                //connect to local group server on remote computer
                System.out.println(String.format("Attempting to connect to remote group server on %s, %d", ip, port));
                rc_group_data = new RemoteConnectionData(ip, port, remote_GID, remote_group_password, type, clientName);
                Context.getInstance().setRemote_group_data(rc_group_data);
                if(type.equals("collaboration")){
                    startCollaborationMeeting();
                }
                else{
                    //todo - presentation mode
                }
            }
        });
        cancel_btn.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                remote_group_password = null;
                dialog.close();
                System.out.println("Canceled. Disconnecting from Remote Server.");
                //handle further, disconnect etc.
            }
        });
        dialog.show();
    }
    
    @FXML
    private void changeToSignUp(ActionEvent event){
        myController.setScreen(main.SignUpPageID);
    }
    
    @FXML
    private void changeToSignIn(ActionEvent event){
        myController.setScreen(main.SignInPageID);
    }
    
    @FXML
    public void ChangeToRemoteControl(ActionEvent event){
        myController.setScreen(main.homePageID);
    }
    
    @FXML
    public void ChangeToOnlineChat(ActionEvent event){
        myController.setScreen(main.OnlineChatID);
    }
}
