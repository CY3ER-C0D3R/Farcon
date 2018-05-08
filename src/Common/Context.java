/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Common;

import Main.FXMLDocumentController;
import GroupCollaborationPage.GroupCollaborationPageController;
import GroupMeetingPage.GroupMeetingPageController;
import OnlineChatPage.OnlineChatPageController;
import RemoteControlPage.RemoteControlPaneFXMLController;
import SignInPage.SignIn_FormController;
import SignUpPage.SignUp_FormController;
import java.io.BufferedReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import javafx.scene.shape.Circle;
import javafx.scene.control.Label;

/**
 *
 * @author admin
 */
public class Context {
    private final static Context instance = new Context();
    
    private String serverhostname;
    private String serverIP;
    private int serverPort;
    private int connectionTimeout;
    
    private FXMLDocumentController f;
    private GroupMeetingPageController g;
    private SignUp_FormController s_u;
    private SignIn_FormController s_i;
    private GroupCollaborationPageController gc;
    private OnlineChatPageController OnlineChatController;
    private RemoteControlPaneFXMLController RemoteControlPaneController;
    
    private Socket clientsock;
    private OutputStreamWriter output;
    private BufferedReader input;
    
    private RemoteConnectionData remote_data;
    private RemoteConnectionData remote_group_data;
    private String selectedFileName;
    private boolean GroupHost;
    
    private boolean connectedToMasterServer;
    private boolean signedIn; // is the user logged on or not?
    
    private String status; // saves the current status of the status bar
    private String style; // saves the css format of the status bar (mainly the color)
    private String lastStatus;  // saves the status of the status bar
    private Timer timer;
    
        
    public static Context getInstance() {
        return instance;
    }

    public String getServerhostname() {
        return serverhostname;
    }

    public void setServerhostname(String serverhostname) {
        this.serverhostname = serverhostname;
    }

    public String getServerIP() {
        return serverIP;
    }

    public void setServerIP(String serverIP) {
        this.serverIP = serverIP;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }
    
    public int getConnectionTimout() {
        return this.connectionTimeout;
    }
    
    public void setConnectionTimeout(int connectionTimeout){
        this.connectionTimeout = connectionTimeout;
    }
    
    public FXMLDocumentController getF() {
        return f;
    }

    public void setF(FXMLDocumentController f) {
        this.f = f;
    }

    public GroupMeetingPageController getG() {
        return g;
    }

    public void setG(GroupMeetingPageController g) {
        this.g = g;
    }

    public SignUp_FormController getS_f() {
        return s_u;
    }

    public void setS_f(SignUp_FormController s_f) {
        this.s_u = s_f;
    }

    public SignIn_FormController getS_i() {
        return s_i;
    }

    public void setS_i(SignIn_FormController s_i) {
        this.s_i = s_i;
    }    

    public GroupCollaborationPageController getGc() {
        return gc;
    }

    public void setGc(GroupCollaborationPageController gc) {
        this.gc = gc;
    }

    public OnlineChatPageController getOnlineChatController() {
        return OnlineChatController;
    }

    public void setOnlineChatController(OnlineChatPageController OnlineChatController) {
        this.OnlineChatController = OnlineChatController;
    }

    public RemoteControlPaneFXMLController getRemoteControlPaneController() {
        return RemoteControlPaneController;
    }

    public void setRemoteControlPaneController(RemoteControlPaneFXMLController RemoteControlPaneController) {
        this.RemoteControlPaneController = RemoteControlPaneController;
    }
    
    public String getSelectedFileName() {
        return selectedFileName;
    }

    public void setSelectedFileName(String selectedFileName) {
        this.selectedFileName = selectedFileName;
    }

    public boolean isGroupHost() {
        return GroupHost;
    }

    public void setGroupHost(boolean GroupHost) {
        this.GroupHost = GroupHost;
    }
    
    public Socket getClientsock() {
        return clientsock;
    }

    public void setClientsock(Socket clientsock) {
        this.clientsock = clientsock;
    }

    public OutputStreamWriter getOutput() {
        return output;
    }

    public void setOutput(OutputStreamWriter output) {
        this.output = output;
    }

    public BufferedReader getInput() {
        return input;
    }

    public void setInput(BufferedReader input) {
        this.input = input;
    }

    public RemoteConnectionData getRemote_data() {
        return remote_data;
    }

    public void setRemote_data(RemoteConnectionData remote_data) {
        this.remote_data = remote_data;
    }

    public RemoteConnectionData getRemote_group_data() {
        return remote_group_data;
    }

    public void setRemote_group_data(RemoteConnectionData remote_group_data) {
        this.remote_group_data = remote_group_data;
    }
    
    public boolean isConnectedToMasterServer() {
        return connectedToMasterServer;
    }

    public void setConnectedToMasterServer(boolean connectedToMasterServer) {
        this.connectedToMasterServer = connectedToMasterServer;
    }

    public String getLastStatus() {
        return lastStatus;
    }

    public void setLastStatus(String lastStatus) {
        this.lastStatus = lastStatus;
    }

    public boolean isSignedIn() {
        return signedIn;
    }

    public void setSignedIn(boolean signedIn) {
        this.signedIn = signedIn;
    }
    
    // ------------------------------------------------------------------ //
    
    public String parseIP(String ip){
        // function returns a "clean" ip
        if(ip.contains("/")){
            return ip.split("/")[1];
        }
        else
            return ip;
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
    
    public void UpdateProfilePicture(){
        // function updates the profile picture on all the pages
        System.out.println("Updating profile picture");
        f.SetProfilePicture(this.isSignedIn());
        g.SetProfilePicture(this.isSignedIn());
        s_i.SetProfilePicture(this.isSignedIn());
        s_u.SetProfilePicture(this.isSignedIn());
        OnlineChatController.SetProfilePicture(this.isSignedIn());
    }
    
    public void SetStatusBar(String status, String style){
        this.status = status;
        this.style = style;
    }
    
    public String getStatusBarStatus(){
        return this.status;
    }
    
    public String getStatusBarStyle(){
        return this.style;
    }
    
    public void UpdateStatusBar(String connectionStatus, boolean returnBack){
        // if returnBack is true, status bar will go back to normal after 5 seconds
        
        if(connectionStatus.equals("unsecure-connection") || connectionStatus.equals("secure-connection") || connectionStatus.equals("Connected"))
        {
            //update connection succeeded status  
            this.SetStatusBar("Ready to connect (unsecure connection)", "-fx-fill: #1bb21b");  // green status circle
            System.out.println("NOW NOW NOW NOW NOW NOW NOW NOW NOW NOW NOW NOW HEREEEE");
            this.connectedToMasterServer = true;
        }
        else if (connectionStatus.equals("remote-control-only") || connectionStatus.equals("Authenticating...") || connectionStatus.equals("Awaiting Authentication...") || connectionStatus.equals("Connecting to Remote Host...") || connectionStatus.equals("Connecting to remote Group server...")) 
        {
            //update connection half succeeded status
            String statusbar_style = "-fx-fill: #FF7900"; //orange
            String statusbar_status = "";
            if(connectionStatus.equals("remote-control-only")) // error in creating the local server, client can only remote-control others
                statusbar_status = "Remote Control Only (Incoming Connections are unavailable)";
            else // other reasons for orange label
                statusbar_status = connectionStatus;
            
            this.SetStatusBar(statusbar_status, statusbar_style);
        }
        else //error in connecting to Master Server
        {
            //update connection failed status
            System.out.println(connectionStatus);
            this.SetStatusBar(connectionStatus, "-fx-fill: #e60000");  // red status circle
            this.connectedToMasterServer = false;
        }
        
        System.out.println("Here in context, after UpdateStatusBar called");
        System.out.println(this.connectedToMasterServer);
        
        UpdateAllStatusBars();
        
        if(returnBack && !this.lastStatus.equals("")){
            // after 5 seconds return status to the last status (depending on lastStatus)
            timer = new Timer();
            timer.schedule(new UpdateStatus(), 5*1000);
        }
    }
    
    class UpdateStatus extends TimerTask {
        @Override 
        public void run() {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    UpdateStatusBar(lastStatus, false);
                    timer.cancel(); //Terminate the timer thread
                }
            });
        }
    }
    
    public void UpdateAllStatusBars(){
        // function updates all status bars
        System.out.println("Updating status bars");
        if(f != null)
            f.updateStatusBar();
        if(g != null)
            g.updateStatusBar();
        if(s_i != null)
            s_i.updateStatusBar();
        if(s_u != null)
            s_u.updateStatusBar();
        if(OnlineChatController != null)
            OnlineChatController.updateStatusBar();
    }
    
    
//    public void updateStatusBar(Label status_label, Circle status_circle, String connectionStatus, boolean returnBack){
//        // if returnBack is true, status bar will go back to normal after 5 seconds
//        
//        if(connectionStatus.equals("unsecure-connection") || connectionStatus.equals("secure-connection") || connectionStatus.equals("Connected"))
//        {
//            //connection succeeded
//            //System.out.println(String.format("Connected to master server (%s)",connectionStatus));
//            
//            //create a thread that listens to data from master server
//            Master_Server_Handler masterServerConn = new Master_Server_Handler(this.input, this.output, this.clientsock);
//            masterServerConn.start();
//                    
//            //update status
//            status_circle.setStyle("-fx-fill: #1bb21b"); //red
//            status_label.setText("Ready to connect (unsecure connection)");
//            this.connectedToMasterServer = true;
////            try {
////                //get data from master server
////                m.GetIDPassword(Context.getInstance().getClient());
////            } catch (RemoteException ex) {
////                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
////            }
//        }
//        else if (connectionStatus.equals("remote-control-only") || connectionStatus.equals("Authenticating...") || connectionStatus.equals("Awaiting Authentication...") || connectionStatus.equals("Connecting to Remote Host...")) 
//        {
//            //connection half succeeded
//            //System.out.println(String.format("Connected to master server (%s)",connectionStatus));
//            
//            //update status
//            status_circle.setStyle("-fx-fill: #FF7900"); //orange
//            if(connectionStatus.equals("remote-control-only")) // error in creating the local server, client can only remote-control others
//                status_label.setText("Remote Control Only (Incoming Connections are unavailable)");
//            else // other reasons for orange label
//                status_label.setText(connectionStatus);
//        }
//        else //error in connecting to Master Server
//        {
//            //connection failed
//            //System.out.println("Connected to master server failed");
//            
//            status_circle.setStyle("-fx-fill: #e60000"); //red
//            status_label.setText(connectionStatus);
//            this.connectedToMasterServer = false;
//        }
//        
//        Context.getInstance().setConnectedToMasterServer(connectedToMasterServer);
//        
//        if(returnBack && !this.lastStatus.equals("")){
//            // after 5 seconds return status to the last status (depending on lastStatus)
//            timer = new Timer();
//            timer.schedule(new UpdateStatus(), 5*1000);
//        }
//    }
//    
//    class UpdateStatus extends TimerTask {
//        @Override 
//        public void run() {
//            Platform.runLater(new Runnable() {
//                @Override
//                public void run() {
//                    updateStatusBar(lastStatus, false);
//                    timer.cancel(); //Terminate the timer thread
//                }
//            });
//        }
//    }
    
    // ------------------------------------------------------------------ //
}
