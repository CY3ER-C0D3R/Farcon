/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Main;

import Common.ControlledScreen;
import RemoteControlPage.LocalServer;
import Common.RemoteConnectionData;
import Common.ScreensController;
import SignUpPage.SignUp_FormController;
import Common.Context;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXHamburger;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.events.JFXDialogEvent;
import com.jfoenix.transitions.hamburger.HamburgerBackArrowBasicTransition;
import com.sun.glass.ui.Pixels;
import com.sun.javafx.robot.FXRobot;
import com.sun.javafx.robot.FXRobotFactory;
import com.sun.glass.ui.Robot;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Timer;
import java.util.TimerTask;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.util.StringConverter;
import javax.imageio.ImageIO;
import javax.net.ssl.SSLSocketFactory;

/**
 *
 * @author admin
 */
public class FXMLDocumentController implements Initializable, ControlledScreen {
    
    ScreensController myController;
    RemoteConnectionData rc_data;
    
    static FXMLDocumentController singleton = null;
    
    private Socket clientsock;
    private OutputStreamWriter output;
    //private DataOutputStream output;
    private BufferedReader input;
    
    // private HamburgerBackArrowBasicTransition burgertask;
    
    private String username;
    private String my_id;
    private String my_rc_password;
    private String remote_id;
    private String remote_rc_password;
    
    private List<String> connected_users;
   
    public LocalServer server;
    public boolean LocalServerIsOpen = false; //becomes true only after registered in Master Server
    
    private Notifications notificationbuilder;
    private String lastStatus;
    private Timer timer;
    
    
    static void exit()
    {
        if (singleton != null && singleton.clientsock != null)
            singleton.closeSock();
    }
    private void closeSock()
    {
        try {
            clientsock.close();
        } catch (IOException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    @FXML
    private Label label;
    
//    @FXML
//    private JFXHamburger hamburger;
    
    @FXML
    private Circle status_circle;
    
    @FXML
    private Circle profile_picture_circle;
    
    @FXML
    private Label status_label;
    
    @FXML
    private Label password_field;

    @FXML
    private Label id_field;
  
    @FXML
    private JFXComboBox<String> id_combobox;

    public void setUsername(String username) {
        this.username = username;
    }
    
    public void setID(String id){
        this.my_id = id;
    }
    
    public String getID(){
        return this.my_id;
    }
    
    public String getPassword(){
        return this.my_rc_password;
    }
    
    public void setHomePage(){
        myController.setScreen(main.homePageID);
    }
    
    public void setRC_Password(String my_rc_password){
        this.my_rc_password = my_rc_password;
    }
    
    public void setLocalServerStatus(boolean status){
        this.LocalServerIsOpen = status; // true or false
    }
    
    public void updatePage(){
        // function updates the page
        this.id_field.setText(this.my_id);
        this.password_field.setText(this.my_rc_password);
        //todo - add username
        //todo - update connected_users list and combobox
    }
    
    public void updateConnectedUsers(String connected_users){
        // function updates the connected users list and the combobox to choose from
        this.id_combobox.getItems().clear();
        StringTokenizer StrTok = new StringTokenizer(connected_users, ";",
                            false);
        System.out.println("Here in client, got this users list: ");
        System.out.println(connected_users);
        while(StrTok.hasMoreTokens())
        {
            String id = StrTok.nextToken();
            if (!id.equals(this.my_id)) {
                this.connected_users.add(id);
                System.out.println(this.connected_users);
                this.id_combobox.getItems().add(id);
            }
        }
    }
    
    public void addConnectedUserToCombobox(String id){
        this.id_combobox.getItems().add(id);
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
    
    @FXML
    private void KeyReleased(KeyEvent event){
        // if enter key was released request connection to remote server
        // with the data entered
        
        if(event.getCode().equals(KeyCode.ENTER)){ 
            System.out.println("Here, enter key released");
            System.out.println(event.getCode().impl_getCode());
            System.out.println(KeyCode.ENTER.impl_getCode());
            requestconnectionToRemoteServer(null);
        }
        else{
            System.out.println("Here, key released");
            System.out.println(event.getCode());
            System.out.println(event.isAltDown());
            System.out.println(event.isControlDown());
            System.out.println(event.isMetaDown());
            System.out.println(event.isShiftDown());
            
            System.out.println(event.getCode().impl_getCode());
            System.out.println(KeyCode.DIGIT9.impl_getCode());
        }
    }
    
    @FXML
    private void requestconnectionToRemoteServer(Event event){
        String remote_id = "";
        if(!this.id_combobox.getSelectionModel().isEmpty())
            remote_id = this.id_combobox.getSelectionModel().getSelectedItem();
        System.out.println(remote_id);
        String remote_Username = "";
        // check if id entered is username or not
        if(Pattern.matches(".*[a-zA-Z].*", remote_id))
        {
            remote_Username = remote_id;
            remote_id = "";
            System.out.println("Remote Username entered: " + remote_Username);
        }
        System.out.println("is connected to master server ?");
        System.out.println(Context.getInstance().isConnectedToMasterServer());
        if(!Context.getInstance().isConnectedToMasterServer())
            DisplayNotification("Error", "Couldn't connect to remote host since\n not connected to Master Server");
        else if(!remote_id.equals("") || !remote_Username.equals(""))
        {
            // update status bar 
            System.out.println("Connecting to remote server");
            
            Context.getInstance().UpdateStatusBar("Connecting to Remote Host...", false);
            //this.updateStatus("Connecting to Remote Host...", false);
            
            //request ip, port for the remote server from the Master Server 
            JSONObject jsonObject = new JSONObject();
            JSONObject parameters = new JSONObject();
            try {
                jsonObject.put("Action", "remote-control-request");
                parameters.put("ID", remote_id);
                parameters.put("Username", remote_Username);
                jsonObject.put("Parameters", parameters);
            } catch (JSONException ex) {
                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            }
            SendMessage(jsonObject);
        }
        else // display a notification
        {
            this.DisplayNotification("Invalid ID", "Please enter a name or an ID number");
        }
    }
    
    public void connectToRemoteServer(String ip, int port, String remote_ID){
        // update status bar 
        System.out.println("Remote server authentication, Password needs to be entered.");
        
        Context.getInstance().UpdateStatusBar("Authenticating...", false);
        
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
        vb.getChildren().addAll(new Label("Please Enter the Remote Host Password", imgview), hb1);
        content.setBody(vb);
        content.setActions(hb2);
        JFXDialog dialog = new JFXDialog(this.myController, content, JFXDialog.DialogTransition.CENTER);
        dialog.setOverlayClose(false); // don't close dialog unless buttons have been pressed
        // set action events for buttons and for dialog itself
        log_on_btn.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                remote_rc_password = rc_password_field.getText();
                dialog.close();
                System.out.println("Password entered: " + remote_rc_password);
                System.out.println("Authenticating...");
                //connect to localserver on remote computer
                System.out.println(String.format("Attempting to connect to remote server on %s, %d", ip, port));
                rc_data = new RemoteConnectionData(ip, port, remote_ID, remote_rc_password);
                Context.getInstance().setRemote_data(rc_data);
                main.mainContainer.loadScreen(main.RemotePageID, main.RemotePageFile);
                myController.setScreen(main.RemotePageID);
            }
        });
        cancel_btn.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                remote_rc_password = null;
                dialog.close();
                System.out.println("Canceled. Disconnecting from Remote Server.");
                //handle further, disconnect etc. todo
                // return status bar to previous
                Context.getInstance().UpdateStatusBar(lastStatus, false);
            }
        });
        dialog.show();
    }
    
    @FXML
    public void changeToSignUp(ActionEvent event){
        //
//        Robot r  = com.sun.glass.ui.Application.GetApplication().createRobot();
//        r.mouseMove(1200, 205);
//        r.mousePress(1);
//        r.mouseRelease(1);
//        
//        System.out.println("Printing ABab:");
//        System.out.println((int)'A');
//        System.out.println((int)'B');
//        System.out.println((int)'a');
//        System.out.println((int)'b');
//        r.keyPress(KeyCode.A.impl_getCode());
//        r.keyPress(KeyCode.B.impl_getCode());
//        r.keyPress(KeyCode.BACK_SLASH.impl_getCode());
//        r.keyPress(KeyCode.CAPS.impl_getCode());
//        r.keyRelease(KeyCode.CAPS.impl_getCode());
//        r.keyPress(KeyCode.A.impl_getCode());
//        r.keyPress(KeyCode.CAPS.impl_getCode());
//        r.keyPress(KeyCode.SHIFT.impl_getCode());
//        r.keyPress(KeyCode.SLASH.impl_getCode());
//        r.keyRelease(KeyCode.SHIFT.impl_getCode());
//        r.keyPress(KeyCode.WINDOWS.impl_getCode());
//        r.keyRelease(KeyCode.WINDOWS.impl_getCode());
//        r.keyPress(KeyCode.A.impl_getCode());
//        r.keyRelease(KeyCode.A.impl_getCode());
//        r.keyPress(KeyCode.WINDOWS.impl_getCode());
//        r.keyRelease(KeyCode.WINDOWS.impl_getCode());
//        String name = KeyCode.A.getName();
//        System.out.println("Key name:");
//        System.out.println(name);
//        r.keyPress(KeyCode.getKeyCode(name).impl_getCode());
//        
        
//        Thread thread = new Thread()
//                {
//            @Override
//            public void run() {
//
//                try {
//                    Thread.sleep(2000);
//                } catch (InterruptedException ex) {
//                    Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
//                }
//                System.out.println("Robot in document controller");
//                
////                FXRobot fxRobot = FXRobotFactory.createRobot(main.getScene());
////                fxRobot.setAutoWaitForIdle(true);
////                fxRobot.mouseMove(10, 10);
////                fxRobot.mousePress(MouseButton.PRIMARY);
////                fxRobot.mouseRelease(MouseButton.PRIMARY);
//            }
//
//        };
        //thread.start();
        /*Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                }
                System.out.println("Robot in document controller");
                
                FXRobot fxRobot = FXRobotFactory.createRobot(main.getScene());
                fxRobot.setAutoWaitForIdle(false);
                fxRobot.mouseMove(10, 10);
                fxRobot.mousePress(MouseButton.PRIMARY);
                fxRobot.mouseRelease(MouseButton.PRIMARY);
            }
        });*/
        
        myController.setScreen(main.SignUpPageID);
    }
    
    @FXML
    public void changeToSignIn(ActionEvent event){
        myController.setScreen(main.SignInPageID);
    }
    
    @FXML
    public void ChangeToGroupMeeting(ActionEvent event){
        myController.setScreen(main.GroupMeetingID);
    }
    
    @FXML
    public void ChangeToOnlineChat(ActionEvent event){
        myController.setScreen(main.OnlineChatID);
    }
    
    @FXML
    public void exitApplication(ActionEvent event) {
        try {
            clientsock.close();
        } catch (IOException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
        Platform.exit();
        
    }
    
    public void SendMessage(JSONObject jsonObject){
        try {
            if(Context.getInstance().isConnectedToMasterServer())
            {
                output.write(jsonObject.toString() + "\n");
                output.flush();
            }
            else 
            {
                DisplayNotification("Error", "Couldn't Preform action, not connect to Master Server");
            }
        } catch (IOException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
//    @FXML
//    private void onHamburgerClick(MouseEvent evt){
//        burgertask.setRate(burgertask.getRate() * -1);
//        burgertask.play();
//    }
    
    
    public void setLastStatus(String status){
        this.lastStatus = status;
        Context.getInstance().setLastStatus(lastStatus);
    }
    
    public void updateStatusBar() {
        // function updates the status bar according to the data in Context
        this.status_label.setText(Context.getInstance().getStatusBarStatus());
        this.status_circle.setStyle(Context.getInstance().getStatusBarStyle());
    }
    
    public void connectToMasterServer(String ip, int port, int connectionTimeout) {
        
        try {
            System.out.println(String.format("Connecting to Master Server on %s, %d", ip, port));

            clientsock = ((SSLSocketFactory) SSLSocketFactory.getDefault()).createSocket();
            clientsock.connect(new InetSocketAddress(ip, port), connectionTimeout);

            //this.clientsock = new Socket(ip, port);
            //this.output = new DataOutputStream(clientsock.getOutputStream());
            output = new OutputStreamWriter(clientsock.getOutputStream(), "UTF-8");
            input = new BufferedReader(new InputStreamReader(clientsock.getInputStream(), "UTF-8"));
            
            
            System.out.println("Here setting the input and output: ");
            Context.getInstance().setClientsock(clientsock);
            Context.getInstance().setOutput(output);
            Context.getInstance().setInput(input);
            System.out.println(Context.getInstance().getInput());
            System.out.println(Context.getInstance().getOutput());

            String line = input.readLine();
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(line);
                // add status later - todo, update secure/unsecure by status that server sends
            } catch (JSONException ex) {
                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (jsonObject != null) //successful connection to Master Server
            {
                System.out.println(jsonObject);
                //create a thread that listens to data from master server		
                System.out.println("Starting the master server handler");
                Master_Server_Handler masterServerConn = new Master_Server_Handler(this.input, this.output, this.clientsock);
                masterServerConn.start();
                
                String status = "unsecure-connection";
                System.out.println("Successfully connected to Master Server, Connection status: " + status);
                setLastStatus(status);
                
                Context.getInstance().UpdateStatusBar(status, false);
                
                System.out.println("Initializing server data");
                try {
                    my_id = jsonObject.getString("ID");
                    username = my_id; //initialize username to the id until user logs in
                    id_field.setText(my_id);
                    my_rc_password = jsonObject.getString("RC_Password");
                    password_field.setText(my_rc_password);
                    //String connected_users = jsonObject.getString("Connected_Users");
                    //updateConnectedUsers(connected_users);
                } catch (JSONException ex) {
                    Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                String status = "Error in connection to Master Server.";
                System.err.println(status);
                setLastStatus(status);
                
                Context.getInstance().UpdateStatusBar(status, false); //error in connection to master server
                
                closeSock();
                return;
            }

            //open new thread for local server connections and wait for server status response
            server = new LocalServer(singleton, username, my_rc_password, my_id);
//            Thread thread = new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    server = new LocalServer(singleton, username, my_rc_password, my_id);
//                }
//            });
//            thread.start();

            //line = input.readLine();
//            String status = "";
//            jsonObject = null;
//            try {
//                jsonObject = new JSONObject(line);
//                status = jsonObject.getString("Status");
//            } catch (JSONException ex) {
//                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
//            }
            //return status;
        } catch (IOException ex) {
            //Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println(ex.getMessage());
            String status = "Unable to connect to master server. Please check Internet connectivity.";
            setLastStatus(status);
            
            Context.getInstance().UpdateStatusBar(status, false);//error in connection to master server
            closeSock();
        }

//        Platform.runLater(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    System.out.println(String.format("Connecting to Master Server on %s, %d", ip, port));
//
//                    clientsock = ((SSLSocketFactory) SSLSocketFactory.getDefault()).createSocket();
//                    clientsock.connect(new InetSocketAddress(ip, port), connectionTimeout);
//
//                    //this.clientsock = new Socket(ip, port);
//                    //this.output = new DataOutputStream(clientsock.getOutputStream());
//                    output = new OutputStreamWriter(clientsock.getOutputStream(), "UTF-8");
//                    input = new BufferedReader(new InputStreamReader(clientsock.getInputStream(), "UTF-8"));
//
//                    Context.getInstance().setClientsock(clientsock);
//                    Context.getInstance().setOutput(output);
//                    Context.getInstance().setInput(input);
//
//                    String line = input.readLine();
//                    JSONObject jsonObject = null;
//                    try {
//                        jsonObject = new JSONObject(line);
//                        // add status later - todo, update secure/unsecure by status that server sends
//                    } catch (JSONException ex) {
//                        Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                    if (jsonObject != null) //successful connection to Master Server
//                    {
//                        String status = "unsecure-connection";
//                        System.out.println("Successfully connected to Master Server, Connection status: " + status);
//                        setLastStatus(status);
//                        updateStatusBar(status, false);
//                        System.out.println("Initializing server data");
//                        try {
//                            my_id = jsonObject.getString("ID");
//                            username = my_id; //initialize username to the id until user logs in
//                            id_field.setText(my_id);
//                            my_rc_password = jsonObject.getString("RC_Password");
//                            password_field.setText(my_rc_password);
//                            //String connected_users = jsonObject.getString("Connected_Users");
//                            //updateConnectedUsers(connected_users);
//                        } catch (JSONException ex) {
//                            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
//                        }
//                    } else {
//                        String status = "Error in connection to Master Server.";
//                        System.err.println(status);
//                        setLastStatus(status);
//                        updateStatusBar(status, false); //error in connection to master server
//                        return;
//                    }
//
//                    //open new thread for local server connections and wait for server status response
//                    server = new LocalServer(singleton, username, my_rc_password, my_id);
////            Thread thread = new Thread(new Runnable() {
////                @Override
////                public void run() {
////                    server = new LocalServer(singleton, username, my_rc_password, my_id);
////                }
////            });
////            thread.start();
//
//                    //line = input.readLine();
////            String status = "";
////            jsonObject = null;
////            try {
////                jsonObject = new JSONObject(line);
////                status = jsonObject.getString("Status");
////            } catch (JSONException ex) {
////                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
////            }
//                    //return status;
//                } catch (IOException ex) {
//                    System.err.println(ex.getMessage());
//                    String status = "Unable to connect to master server. Please check Internet connectivity.";
//                    setLastStatus(status);
//                    updateStatusBar(status, false); //error in connection to master server
//                }
//            }
//        });
    }

    public void SetProfilePicture(boolean signedIn) {
        BufferedImage bufferedImage = null;
        try {
            if(signedIn)
            {
                File f = new File(Paths.get(".").toAbsolutePath().normalize().toString()+"\\src\\Resources\\007-profile-photo." + Context.getInstance().getPhoto_extention());
                bufferedImage = ImageIO.read(f);
            }
            else //use default picture
            {
                File f = new File(Paths.get(".").toAbsolutePath().normalize().toString()+"\\src\\Resources\\004-user.png");
                bufferedImage = ImageIO.read(f);
            } 
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
        Image img = SwingFXUtils.toFXImage(bufferedImage, null);
        this.profile_picture_circle.setFill(new ImagePattern(img));
    }
    
    public void connectToServer()
    {
        //connect to master server
        String remoteServerName = Context.getInstance().getServerhostname();
        String remoteServerIP = Context.getInstance().getServerIP();
        int remoteServerPort = Context.getInstance().getServerPort(); //55323 or 1098
        int connectionTimeout = Context.getInstance().getConnectionTimout();

        try {
            System.out.println(String.format("Attempting connection to remote server %s on port %d...", remoteServerName, remoteServerPort));
            connectToMasterServer(remoteServerName, remoteServerPort, connectionTimeout);

        } catch (Exception ex) {
            System.err.println(String.format("Connection to remote server failed: %s", ex.getMessage()));

        }

        // If connection to remote server failed, try connecting to an online local server
        // that could be potentially opened.
        try {
            System.out.println(String.format("Attempting connection to local server %s on port %d...", remoteServerIP, remoteServerPort));
            connectToMasterServer(remoteServerIP, remoteServerPort, connectionTimeout);

        } catch (Exception ex) {
            System.err.println(String.format("Connection to local server failed: %s", ex.getMessage()));
        }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        singleton = this;
        Context.getInstance().setF(singleton);
        this.connected_users = new ArrayList<String>();
        
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                id_combobox.requestFocus();
            }
        });

        my_id = username = this.id_field.getText(); //username and password are the same until logged in
        my_rc_password = this.password_field.getText();
        
        // try connecting to master server
        connectToServer();
        
        // Wait five seconds each time before retrying
        Thread thread = new Thread()
        {
            @Override
            public void run() {
                super.run();
                while(true)
                {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                    System.out.println("In loop " + status_circle.getStyle());
                    Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
                    System.out.println("Number of threads = " + threadSet.size());
                    if (status_circle.getStyle().equals("-fx-fill: #e60000") && 
                            (status_label.getText().equals("Error in connection to Master Server.") ||
                             status_label.getText().equals("Unable to connect to master server. Please check Internet connectivity.") ||
                             status_label.getText().equals("Master server offline"))) //status that indicates error between master server and client
                    {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                connectToServer();
                            }
                        });
                        System.out.println("In loop connectToServer");
                    }
                }
            }
        };
        thread.start();
    }   
}
