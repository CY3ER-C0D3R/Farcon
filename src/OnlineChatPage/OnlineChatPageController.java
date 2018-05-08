/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package OnlineChatPage;

import Common.ControlledScreen;
import Common.ConversationView;
import Main.FXMLDocumentController;
import Common.ScreensController;
import SignUpPage.SignUp_FormController;
import Main.main;
import Common.Context;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Circle;
import javax.imageio.ImageIO;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * FXML Controller class
 *
 * @author admin
 */
public class OnlineChatPageController implements Initializable, ControlledScreen, OnlineChatInterface {
    
    ScreensController myController;
    static OnlineChatPageController singleton = null;
    ConversationView c;
    
    private Socket clientsock;
    private OutputStreamWriter output;
    private BufferedReader input;
    
    @FXML
    private BorderPane chat_room_pane;
    
    @FXML
    private ImageView profile_photo_img;
    
    @FXML
    private Label status_label;
    
    @FXML
    private Circle status_circle;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        singleton = this;
        
        this.clientsock = Context.getInstance().getClientsock();
        this.output = Context.getInstance().getOutput();
        this.input = Context.getInstance().getInput();
        Context.getInstance().setOnlineChatController(singleton);
        updateStatusBar();
        
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                c = new ConversationView("Online Chat", singleton);
                chat_room_pane.setCenter(c);
            }
        });
    }
    
    public void updateStatusBar() {
        // function updates the status bar according to the data in Context
        this.status_label.setText(Context.getInstance().getStatusBarStatus());
        this.status_circle.setStyle(Context.getInstance().getStatusBarStyle());
    }
    
    public void SetProfilePicture(boolean signedIn) {
        BufferedImage bufferedImage = null;
        try {
            if(signedIn)
                bufferedImage = ImageIO.read(new File(Paths.get(".").toAbsolutePath().normalize().toString() + "/build/classes/Resources/007-profile-photo.png"));
            else //use default picture
                bufferedImage = ImageIO.read(new File(Paths.get(".").toAbsolutePath().normalize().toString() + "/build/classes/Resources/004-user.png"));
        } catch (IOException ex) {
            Logger.getLogger(OnlineChatPageController.class.getName()).log(Level.SEVERE, null, ex);
        }
        Image img = SwingFXUtils.toFXImage(bufferedImage, null);
        this.profile_photo_img.setImage(img);
    }
    
    public void SendMessage(JSONObject jsonObject){
        try {
            output.write(jsonObject.toString() + "\n");
            output.flush();
        } catch (IOException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void sendMessageToServer(String message, String messageInfo) {
        
        System.out.println("Sending Online Chat Message: " + message);
        System.out.println("Message Info: " + messageInfo);
        
        JSONObject jsonObject = new JSONObject();
        JSONObject parameters = new JSONObject();
        try {
            jsonObject.put("Action", "online-chat");
            parameters.put("message", message);
            parameters.put("message-info", messageInfo);
            jsonObject.put("Parameters", parameters);
            
            //send data to server
            SendMessage(jsonObject);
            
        } catch (JSONException ex) {
            Logger.getLogger(SignUp_FormController.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }

    @Override
    public void receiveMessageFromServer(String sender_username, String message, String messageInfo) {
        c.receiveMessage(sender_username, message, messageInfo);
    }

    @Override
    public void setScreenParent(ScreensController screenParent) {
        myController = screenParent;
    }
    
    @FXML
    public void changeToSignUp(ActionEvent event){
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
    public void ChangeToGroupMeeting(ActionEvent event){
        myController.setScreen(main.GroupMeetingID);
    }
}
