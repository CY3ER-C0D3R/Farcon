/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SignInPage;

import Common.ControlledScreen;
import Common.ScreensController;
import SignUpPage.SignUp_FormController;
import Main.main;
import Main.FXMLDocumentController;
import Common.Context;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
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
import javafx.scene.shape.Circle;
import javax.imageio.ImageIO;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * FXML Controller class
 *
 * @author admin
 */
public class SignIn_FormController implements Initializable, ControlledScreen {
    
    ScreensController myController;    
    
    private Socket clientsock;
    private OutputStreamWriter output;
    private BufferedReader input;
    
    @FXML
    private Label status_label;

    @FXML
    private JFXPasswordField password_field;

    @FXML
    private JFXTextField username_field;
    
    @FXML
    private JFXButton sign_in_btn;

    @FXML
    private Circle status_circle;

    @FXML
    private ImageView profile_photo_img;
    
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        this.clientsock = Context.getInstance().getClientsock();
        this.output = Context.getInstance().getOutput();
        this.input = Context.getInstance().getInput();
        Context.getInstance().setS_i(this);
        updateStatusBar();
    }   
    
    public void updateStatusBar() {
        // function updates the status bar according to the data in Context
        this.status_label.setText(Context.getInstance().getStatusBarStatus());
        this.status_circle.setStyle(Context.getInstance().getStatusBarStyle());
    }
    
    public void SendMessage(JSONObject jsonObject){
        try {
            output.write(jsonObject.toString() + "\n");
            output.flush();
        } catch (IOException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void changeToSignOut() {
        // function updates the gui so that the client has to sign out 
        // if he wants to enter with a different username
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                sign_in_btn.setText("Sign Out");
                username_field.setDisable(true);
                password_field.setDisable(true);
            }
        });
    }
    
    public void changeToSignIn(){
        // function updates the gui so that the client can sign in
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                sign_in_btn.setText("Sign In");
                username_field.setText("");
                password_field.setText("");
                username_field.setDisable(false);
                password_field.setDisable(false);
            }
        });
    }
    
    @FXML
    public void SignInToServer(ActionEvent event){
        if(this.sign_in_btn.getText().equals("Sign In"))
            SignIn();
        else
            SignOut();
    }
    
    public void SignIn(){
        System.out.println("Attempting to sign in...");
        
        String username = this.username_field.getText();
        String password = this.password_field.getText();
        JSONObject jsonObject = new JSONObject();
        JSONObject parameters = new JSONObject();
        try {
            jsonObject.put("Action", "sign-in");
            parameters.put("username", username);
            parameters.put("password", password);
            jsonObject.put("Parameters", parameters);
            
            //send data to server
            SendMessage(jsonObject);
            
        } catch (JSONException ex) {
            Logger.getLogger(SignUp_FormController.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
    public void SignOut(){
        System.out.println("Attempting to sign out...");
        
        JSONObject jsonObject = new JSONObject();
        JSONObject parameters = new JSONObject();
        try {
            jsonObject.put("Action", "sign-out");
            parameters.put("status", "client signed out");
            jsonObject.put("Parameters", parameters);
            
            //send data to server
            SendMessage(jsonObject);
            
        } catch (JSONException ex) {
            Logger.getLogger(SignUp_FormController.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
    public void SetProfilePicture(boolean signedIn) {
        BufferedImage bufferedImage = null;
        try {
            if(signedIn)
                bufferedImage = ImageIO.read(new File(Paths.get(".").toAbsolutePath().normalize().toString() + "/build/classes/Resources/007-profile-photo." + Context.getInstance().getPhoto_extention()));
            else //use default picture
                bufferedImage = ImageIO.read(new File(Paths.get(".").toAbsolutePath().normalize().toString() + "/build/classes/Resources/004-user.png"));
        } catch (IOException ex) {
            Logger.getLogger(SignUp_FormController.class.getName()).log(Level.SEVERE, null, ex);
        }
        Image img = SwingFXUtils.toFXImage(bufferedImage, null);
        this.profile_photo_img.setImage(img);
    }
    
    @FXML
    private void changeToSignUp(ActionEvent event){
        myController.setScreen(main.SignUpPageID);
    }
    
    @Override
    public void setScreenParent(ScreensController screenParent) {
        myController = screenParent;
    }
    
    @FXML
    public void ChangeToRemoteControl(ActionEvent event){
        myController.setScreen(main.homePageID);
    }
    
    @FXML
    public void ChangeToGroupMeeting(ActionEvent event){
        myController.setScreen(main.GroupMeetingID);
    }
    
    @FXML
    public void ChangeToOnlineChat(ActionEvent event){
        myController.setScreen(main.OnlineChatID);
    }
    
}
