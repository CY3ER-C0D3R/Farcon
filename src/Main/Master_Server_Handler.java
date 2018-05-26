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

package Main;

import GroupCollaborationPage.GroupCollaborationPageController;
import GroupMeetingPage.GroupMeetingPageController;
import Main.FXMLDocumentController;
import OnlineChatPage.OnlineChatPageController;
import Common.Context;
import Common.Utils;
import SignInPage.SignIn_FormController;
import SignUpPage.SignUp_FormController;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author admin
 */
public class Master_Server_Handler extends Thread {
    
    private FXMLDocumentController f;
    private GroupMeetingPageController g;
    private GroupCollaborationPageController gc;
    private SignUp_FormController s_u;
    private SignIn_FormController s_i;
    private OnlineChatPageController o;
    
    BufferedReader input;
    OutputStreamWriter output;
    //DataOutputStream output;
    Socket serverSocket;
    String username; 
    String id;
    String rc_password;
    String status;
    // Group inforamtaion
    String type;
    // online chat information
    String sender_username;
    String chat_message;
    String chat_message_info;
    static boolean shouldExit = false;

    public Master_Server_Handler(BufferedReader input, OutputStreamWriter output, Socket serverSocket){
        this.f = Context.getInstance().getF();
        this.input = input;
        this.output = output;
        this.serverSocket = serverSocket;
    }

    @Override
    public void run() {
        while (true)
        {
            try {
                // Client Waits for server to send message
                
                //System.out.println("here in master server handler");
                //System.out.println(input);
                
                if(input != null) {
                    String line = input.readLine();
                    
                    //System.out.println("passsed string line");
                    //System.out.println(line);
                    
                    // create json object from the data sent by the client
                    JSONObject jsonObject = new JSONObject(line);
                    Handle_Message(jsonObject);
                }
            } catch (Exception ex) {
                // make sure that this is done once only
                synchronized (Master_Server_Handler.class){
                    if (!shouldExit)
                    {
                        shouldExit = true;
                        System.out.println("Here in master_server_handler, got exception:");
                        System.err.println(ex.getMessage());
                        f.DisplayNotification("Master server error", ex.getMessage());
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                Context.getInstance().UpdateStatusBar("Master server offline", false);
                                //f.updateStatusBar("Master server offline", false);
                            }
                        });
                    }
                }
                /*Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        //FXMLDocumentController.exit();
                        Platform.exit();
                        System.exit(0);
                    }
                });*/
                break;
            }
        }
    }
    
    public void send_message(JSONObject jsonObject){
        try {
            output.write(jsonObject.toString() + "\n");
            output.flush();
        } catch (IOException ex) {
            Logger.getLogger(Master_Server_Handler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void Handle_Message(JSONObject jsonObject){
        String action = ""; 
        JSONObject parameters = new JSONObject();
        try {
            action = jsonObject.getString("Action");
            parameters = jsonObject.getJSONObject("Parameters");
        } catch (JSONException ex) {
            Logger.getLogger(Master_Server_Handler.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(!action.equals("") && parameters != null){
            // call function according to action, and send it the parameters
            // sign-up and sign-in replies
            if(action.equals("sign-up")){
                this.Sign_Up_Reply(parameters);
            }
            else if(action.equals("sign-in")){
                Sign_In_Reply(parameters);
            }
            else if(action.equals("sign-out")){
                Sign_Out_Reply(parameters);
            }
            // remote control related replies
            else if (action.equals("register-local-server")){
                this.Register_Local_Server_Reply(parameters);
            }
            else if(action.equals("remote-control-request")){
                this.ReturnRemoteHost_Reply(parameters);
            }
            else if (action.equals("remote-control-attempt")){
                this.UpdateRemoteHost_Attempt(parameters);
            }
            // group related replies
            else if(action.equals("init-group-server")){
                this.InitializeGroupServer_Reply(parameters);
            }
            else if(action.equals("register-group-server")){
                this.Register_Group_Server_Reply(parameters);
            }
            else if(action.equals("join-group-request")){
                this.ReturnGroupHost_Reply(parameters);
            }
            else if(action.equals("group-server-closed")){
                this.DisconnectFromGroup(parameters);
            }
            else if(action.equals("update-group-data")){
                this.UpdateGroupData_Reply(parameters);
            }
            // online chat related replies
            else if (action.equals("online-chat")){
                this.UpdateOnlineChat(parameters);
            }
            // other updates from server
            else if (action.equals("update-connected-users")){
                this.UpdateConnectedUsers(parameters);
            }
        }
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
    
    public void Sign_Up_Reply(JSONObject jsonObject){
        // function updates the client of his sign up status
        
        this.s_u = Context.getInstance().getS_f();
        
        try {    
            String status = jsonObject.getString("Status");
            if(status.equals("")) //sign up was successful
            {
                this.DisplayNotification("Sign Up", "Sign Up was successful!");
                // update gui with new username, id and password
                this.username = jsonObject.getString("Username");
                this.id = jsonObject.getString("ID");
                this.rc_password = jsonObject.getString("Password");
                Platform.runLater(new Runnable() {
                     @Override
                     public void run() {
                        f.setUsername(username);
                        f.setID(id);
                        // change btn to sign in as sign up was complete
                        f.changeToSignIn(null);
                        f.setRC_Password(rc_password);
                        f.updatePage();
                        
                        // reset the page to default values
                        if(s_u != null)
                            s_u.updatePage();
                     }
                 });
            }
            else{ // sign up was unsuccessful
                this.DisplayNotification("Unsuccessful Sign Up", status);
            }   
        } catch (JSONException ex) {
            Logger.getLogger(Master_Server_Handler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void Sign_In_Reply(JSONObject jsonObject){
        // function updates the client of his sign up status
        this.s_i = Context.getInstance().getS_i();
        
        try {    
            String status = jsonObject.getString("Status");
            String imageDataString = jsonObject.getString("Profile_Picture");
            if(status.equals("")) //sign in was successful
            {
                System.out.println("Signed in successfully.");
                this.DisplayNotification("Sign In", "Sign In was successful!");
                // update gui with new username, id, password and profile photo
                this.username = jsonObject.getString("Username");
                this.id = jsonObject.getString("ID");
                this.rc_password = jsonObject.getString("Password");
                // update signed in variable
                Context.getInstance().setSignedIn(true);
                // update username, id and rc_password variables
                Context.getInstance().setUsername(username);
                Context.getInstance().setId(id);
                Context.getInstance().setRc_password(rc_password);
                Platform.runLater(new Runnable() {
                     @Override
                     public void run() {
                         FileOutputStream fos = null;
                         try {
                             f.updatePage();
                             // change sign in button to sign out
                             System.out.println("Here in sign in, changing to sign out");
                             if(s_i != null)
                                 s_i.changeToSignOut();
                             // update profile picture on all pages
                             // update resource from which photo will be updated
                             File file = new File(Paths.get(".").toAbsolutePath().normalize().toString() + "\\src\\Resources\\007-profile-photo." + Context.getInstance().getPhoto_extention());
                             fos = new FileOutputStream(file);
                             fos.write(Utils.decodeImage(imageDataString));
                             fos.close();
                             Context.getInstance().UpdateProfilePicture();
                             //return to remote control page - "Home Page"
                             f.setHomePage();
                         } catch (FileNotFoundException ex) {
                             Logger.getLogger(Master_Server_Handler.class.getName()).log(Level.SEVERE, null, ex);
                         } catch (IOException ex) {
                             Logger.getLogger(Master_Server_Handler.class.getName()).log(Level.SEVERE, null, ex);
                         } finally {
                             try {
                                 fos.close();
                             } catch (IOException ex) {
                                 Logger.getLogger(Master_Server_Handler.class.getName()).log(Level.SEVERE, null, ex);
                             }
                         }
                     }
                 });
            }
            else{ // sign up was unsuccessful
                this.DisplayNotification("Unsuccessful Sign In", status);
                // change sign in button to function again
                if(s_i != null)
                    s_i.changeToSignIn();
            }   
        } catch (JSONException ex) {
            Logger.getLogger(Master_Server_Handler.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // request an update for connected users (to update the username)
        jsonObject = new JSONObject();
        JSONObject parameters = new JSONObject();
        try {
            jsonObject.put("Action", "request-update-connected-users");
            jsonObject.put("Parameters", parameters);
        } catch (JSONException e) {
            Logger.getLogger(Master_Server_Handler.class.getName()).log(Level.SEVERE, null, e);
        }
        send_message(jsonObject);
    }
    
    public void Sign_Out_Reply(JSONObject jsonObject){
        // function updates the client of his sign out status
        // and updates the new id and rc_password
        
        try {    
            String status = jsonObject.getString("status");
            if(status.equals("")) //sign in was successful
            {
                System.out.println("Signed out successfully.");
                this.DisplayNotification("Sign Out", "Signed Out.");
                // update gui with new username, id, password and profile photo
                this.id = jsonObject.getString("ID");
                this.username = id;
                this.rc_password = jsonObject.getString("RC_Password");
                // update signed in variable
                Context.getInstance().setSignedIn(false);
                Platform.runLater(new Runnable() {
                     @Override
                     public void run() {
                        f.setUsername(username);
                        f.setID(id);                        
                        f.setRC_Password(rc_password);
                        f.updatePage();
                        // make sign in available again
                        if(s_i != null)
                            s_i.changeToSignIn();
                        // update profile picture on all pages to default
                        Context.getInstance().UpdateProfilePicture(); 
                     }
                 });
            }
            else{ // sign up was unsuccessful
                this.DisplayNotification("Unsuccessful Sign Out", status);
                // make sure the sign out button is still available
                if(s_i != null)
                    s_i.changeToSignOut();
            }   
        } catch (JSONException ex) {
            Logger.getLogger(Master_Server_Handler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void Register_Local_Server_Reply(JSONObject jsonObject){
        try {
            status = jsonObject.getString("Status");
        } catch (JSONException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
        //local server is now registered, so it can be started
        //update status bar accordingly
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Context.getInstance().UpdateStatusBar(status, false);
                //f.updateStatusBar(status, false);
                if(status.equals("secure-connection") || status.equals("unsecure-connection")){
                    System.out.println("Local Server Registered. Started server module...");
                    f.setLocalServerStatus(true); // open the local server
                    f.server.StartLocalServer(); // Start the localserver
                }
                else
                    System.out.println("Local Server hasn't been Resgistered on Master Server.");
            }
        });
    }
    
    public void ReturnRemoteHost_Reply(JSONObject jsonObject){
        // fuction updates client about his status
        
        try {    
            String status = jsonObject.getString("Status");
            if(status.equals("available")) //remote server is open and ready for out connection
            {
                String remote_id = jsonObject.getString("ID");
                String remote_ip = jsonObject.getString("IP");
                int remote_port = jsonObject.getInt("Port");
                
                Platform.runLater(new Runnable() {
                     @Override
                     public void run() {
                        f.connectToRemoteServer(remote_ip, remote_port, remote_id);
                     }
                 });
            }
            else { // remote server is not open and not ready for out connection
                if (status.equals("unavailable")) {
                    this.DisplayNotification("Couldn't connect to Remote Server", "Remote server is unavailable");
                } else if (status.equals("Data not Supplied")) {
                    this.DisplayNotification("Couldn't connect to Remote Server", "Remote ID/Username have not been entered.");
                } else {
                    this.DisplayNotification("Error", "unexpected error occured."); //for error managment
                }
                //update status bar
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        Context.getInstance().UpdateStatusBar("Couldn't connect to Remote Server", true);
                        //f.updateStatusBar("Couldn't connect to Remote Server", true);
                    }
                });
            }
        } catch (JSONException ex) {
            Logger.getLogger(Master_Server_Handler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void UpdateRemoteHost_Attempt(JSONObject jsonObject) {
        // function updates status bar with the connection attempt

        try {
            String status = jsonObject.getString("Status");
            String remote_id = jsonObject.getString("ID");
            System.out.println(String.format("%s is attempting to connect to local host.", remote_id));
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    Context.getInstance().UpdateStatusBar(status, false);
                }
            });
        } catch (JSONException ex) {
            System.err.println(ex.getMessage());
        }
    }
    
    public void InitializeGroupServer_Reply(JSONObject jsonObject){
        
        this.g = Context.getInstance().getG();
        try {    
            String status = jsonObject.getString("Status");
            if(status.equals("")) //everything is ok and can proceed.
            {
                String gid = jsonObject.getString("GID");
                String g_password = jsonObject.getString("G_Password");
                
                Platform.runLater(new Runnable() {
                     @Override
                     public void run() {
                         g.updateGroupMeetingDetails(gid, g_password);
                     }
                 });
            }
            else { // can't open new group session   // todo - go over this part, maybe delete
                if (status.equals("unavailable")) {
                    this.DisplayNotification("Couldn't connect to Remote Server", "Remote server is unavailable");
                } else if (status.equals("Data not Supplied")) {
                    this.DisplayNotification("Couldn't connect to Remote Server", "Remote ID/Username have not been entered.");
                } else {
                    this.DisplayNotification("Error", "unexpected error occured."); //for error managment
                }
                //update status bar
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        Context.getInstance().UpdateStatusBar("Couldn't connect to Remote Server", true);
                        //f.updateStatusBar("Couldn't connect to Remote Server", true);
                        //g.updateStatusBar();
                    }
                });
            }
        } catch (JSONException ex) {
            Logger.getLogger(Master_Server_Handler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void Register_Group_Server_Reply(JSONObject jsonObject){
        this.g = Context.getInstance().getG();  // group meeting page controller
        this.gc = Context.getInstance().getGc(); // group collaboration page controller
        
        try {
            status = jsonObject.getString("Status");
            type = jsonObject.getString("Type");
        } catch (JSONException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
        //local group server is now registered, so it can be started
        //update status bar accordingly
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (gc != null && type.equals("collaboration"))
                {
                    Context.getInstance().UpdateStatusBar(status, false);
                    if(status.equals("secure-connection") || status.equals("unsecure-connection")){
                        System.out.println("Collaboration Group Server Registered. Started server module...");
                        gc.setLocalGroupServerIsOpen(true); // open the local group server
                        gc.startMeeting(); // Start the local group server
                    }
                    else
                        System.out.println("Local Group Server hasn't been Resgistered on Master Server.");
                }
                else if (g != null && type.equals("presentation"))
                {
                    Context.getInstance().UpdateStatusBar(status, false);
                    if(status.equals("secure-connection") || status.equals("unsecure-connection")){
                        System.out.println("Presentation Group Server Registered. Started server module...");
                        g.setLocalPresentationServerIsOpen(true); // open the local group server
                        g.presentationServer.StartLocalServer();// Start the local presentation group server
                    }
                    else
                        System.out.println("Local Group Server hasn't been Resgistered on Master Server.");
                }
            }
        });
    }
    
    public void ReturnGroupHost_Reply(JSONObject jsonObject){
        // fuction updates client about his status
        
        try {    
            String status = jsonObject.getString("Status");
            if(status.equals("available")) //remote group server is open and ready for out connection
            {
                String remote_group_id = jsonObject.getString("GID");
                String remote_group_ip = jsonObject.getString("IP");
                int remote_group_port = jsonObject.getInt("Port");
                String type = jsonObject.getString("Type");
                
                Platform.runLater(new Runnable() {
                     @Override
                     public void run() {
                        if(g == null)
                            g = Context.getInstance().getG();
                        g.connectToRemoteGroupServer(remote_group_ip, remote_group_port, remote_group_id, type);
                     }
                 });
            }
            else { // remote server is not open and not ready for out connection
                if (status.equals("unavailable")) {
                    this.DisplayNotification("Couldn't connect to Remote Group Server", "Remote server is unavailable");
                } else if (status.equals("Data not Supplied")) {
                    this.DisplayNotification("Couldn't connect to Remote Group Server", "Remote Group ID/Name has not been entered.");
                } else {
                    this.DisplayNotification("Error", "unexpected error occured."); //for error managment
                }
                //update status bar
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        Context.getInstance().UpdateStatusBar("Couldn't connect to Remote Group Server", true);
                        //f.updateStatusBar("Couldn't connect to Remote Group Server", true);
                    }
                });
            }
        } catch (JSONException ex) {
            Logger.getLogger(Master_Server_Handler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void DisconnectFromGroup(JSONObject jsonObject){
        
        try {    
            System.out.println("Here in master server handler disconnect from group...");
            String status = jsonObject.getString("Status");         
                Platform.runLater(new Runnable() {
                     @Override
                     public void run() {
                        if(g == null)
                            g = Context.getInstance().getG();
                        g.stopGroupMeeting(status);
                     }
                 });
        } catch (JSONException ex) {
            Logger.getLogger(Master_Server_Handler.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public void UpdateGroupData_Reply(JSONObject jsonObject) {
        try {
            System.out.println("Here in master server handler update group data reply...");
            String status = jsonObject.getString("Status");
            String info = jsonObject.getString("info");
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    if (gc == null) {
                        gc = Context.getInstance().getGc();
                    }
                    gc.DisplayNotification(status, info);
                }
            });
        } catch (JSONException ex) {
            Logger.getLogger(Master_Server_Handler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void UpdateOnlineChat(JSONObject jsonObject){
        this.o = Context.getInstance().getOnlineChatController();
                
        try {
            sender_username = jsonObject.getString("username");
            chat_message = jsonObject.getString("message");
            chat_message_info = jsonObject.getString("message-info");
        } catch (JSONException ex) {
            Logger.getLogger(Master_Server_Handler.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        System.out.println("Received message: " + chat_message + " from user: " + sender_username);
        
        //update conversation view
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                o.receiveMessageFromServer(sender_username, chat_message, chat_message_info);
            }
        });
    }
    
    public void UpdateConnectedUsers(JSONObject jsonObject){
        
        try {
            String connected_users = jsonObject.getString("connected-users");
            //update connected users view
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    System.out.println("YES THIS IS WHERE THE PROBLEM IS");
                    System.out.println(f==null);
                    System.out.println(connected_users);
                    if(f != null)
                        f.updateConnectedUsers(connected_users);
                }
            });
        } catch (JSONException ex) {
            Logger.getLogger(Master_Server_Handler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
