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

package SignUpPage;

import Common.ControlledScreen;
import Common.ScreensController;
import Main.main;
import Main.FXMLDocumentController;
import Common.Context;
import Common.Utils;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
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
public class SignUp_FormController implements Initializable, ControlledScreen {

    ScreensController myController;

    
    private Socket clientsock;
    private OutputStreamWriter output;
    private BufferedReader input;
    
    String ext = ""; // saves the file extention for later
    
    @FXML
    private JFXTextField email_field;
    
    @FXML
    private JFXTextField username_field;

    @FXML
    private JFXPasswordField password_field;

    @FXML
    private JFXPasswordField confirm_password_field;
    
    @FXML
    private Circle profile_picture_circle;
    
    @FXML
    private Circle profile_photo_img_selection;

    @FXML
    private ImageView profile_photo_img;
    
    @FXML
    private Label status_label;
    
    @FXML
    private Circle status_circle;
    
    @FXML
    private Label username_label;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.clientsock = Context.getInstance().getClientsock();
        this.output = Context.getInstance().getOutput();
        this.input = Context.getInstance().getInput();
        Context.getInstance().setS_f(this);
        updateStatusBar();
        updatePage();
    }

    public void updatePage(){
        try {
            // make all the text and password fields blank
            this.email_field.setText("");
            this.username_field.setText("");
            this.password_field.setText("");
            this.confirm_password_field.setText("");
            // set the default profile photo
            BufferedImage bufferedImage = ImageIO.read(getClass().getResource("/Resources/004-user.png").openStream());
            Image img = SwingFXUtils.toFXImage(bufferedImage, null);
            this.profile_photo_img_selection.setFill(new ImagePattern(img));
        } catch (IOException ex) {
            Logger.getLogger(SignUp_FormController.class.getName()).log(Level.SEVERE, null, ex);
        }
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
            {
                File f = new File(Paths.get(".").toAbsolutePath().normalize().toString()+"\\src\\Resources\\007-profile-photo." + Context.getInstance().getPhoto_extention());
                bufferedImage = ImageIO.read(f);
                // update username label
                this.username_label.setText(Context.getInstance().getUsername());
            }
            else //use default picture
            {
                File f = new File(Paths.get(".").toAbsolutePath().normalize().toString()+"\\src\\Resources\\004-user.png");
                bufferedImage = ImageIO.read(f);
                this.username_label.setText("User");
            } 
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
        Image img = SwingFXUtils.toFXImage(bufferedImage, null);
        this.profile_picture_circle.setFill(new ImagePattern(img));
    }
    
    public void SendMessage(JSONObject jsonObject){
        try {
            output.write(jsonObject.toString() + "\n");
            output.flush();
        } catch (IOException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void SignUpToServer(String email, String username, String password) {

        //profile photo is set by the current picture of user in resources
        File file = new File(Paths.get(".").toAbsolutePath().normalize().toString()+"\\src\\Resources\\007-profile-photo." + ext);
        FileInputStream fis = null;
        String imageDataString = "";
        byte imageData[] = null;
        try {
            fis = new FileInputStream(file);
            imageData = new byte[(int)file.length()];
            fis.read(imageData);
            fis.close();
            // converting Image byte array into Base64 String
            imageDataString = Utils.encodeImage(imageData);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SignUp_FormController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SignUp_FormController.class.getName()).log(Level.SEVERE, null, ex);
        }
                
        JSONObject jsonObject = new JSONObject();
        JSONObject parameters = new JSONObject();
        try {
            jsonObject.put("Action", "sign-up");
            parameters.put("email", email);
            parameters.put("username", username);
            parameters.put("password", password);
            parameters.put("profile-picture", imageDataString);
            parameters.put("picture-extention", ext);
            jsonObject.put("Parameters", parameters);
            
            //send data to server
            SendMessage(jsonObject);
                        
        } catch (JSONException ex) {
            Logger.getLogger(SignUp_FormController.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return false;
        }

        String extension = Utils.getExtension(f);
        if (extension != null) {
            if (extension.equals(Utils.tiff)
                    || extension.equals(Utils.tif)
                    || extension.equals(Utils.gif)
                    || extension.equals(Utils.jpeg)
                    || extension.equals(Utils.jpg)
                    || extension.equals(Utils.png)) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }
    
    private void copyFileUsingChannel(File source, File dest) throws IOException {
        FileChannel sourceChannel = null;
        FileChannel destChannel = null;
        try {
            sourceChannel = new FileInputStream(source).getChannel();
            destChannel = new FileOutputStream(dest).getChannel();
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        } finally {
            sourceChannel.close();
            destChannel.close();
        }
    }
    
    @FXML
    private void choose_profile_photo(MouseEvent event){
        // open file dialog to choose the profile photo
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().addAll(
            new ExtensionFilter("PNG Files", "*.png"),
            new ExtensionFilter("JPG Files", "*.jpg"),
            new ExtensionFilter("JPEG Files", "*.jpeg"),
            new ExtensionFilter("GIF Files", "*.gif"),
            new ExtensionFilter("TIF Files", "*.tif"),
            new ExtensionFilter("TIFF Files", "*.tiff")
        );
        File selectedFile = fc.showOpenDialog(null);
        if(selectedFile != null){
            File source = new File(selectedFile.getAbsolutePath());
            this.ext = Utils.getExtension(source);
            Context.getInstance().setPhoto_extention(ext);
            File dest = new File(Paths.get(".").toAbsolutePath().normalize().toString()+"\\src\\Resources\\007-profile-photo." + ext);
            try {
                copyFileUsingChannel(source, dest);
                BufferedImage bufferedImage = ImageIO.read(selectedFile);
                Image img = SwingFXUtils.toFXImage(bufferedImage, null);
                this.profile_photo_img_selection.setFill(new ImagePattern(img));
            } catch (IOException ex) {
                Logger.getLogger(SignUp_FormController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    @FXML
    private void SignUp(ActionEvent event) {
        //get username and password entered and send data to master server
        if (this.password_field.getText().equals(this.confirm_password_field.getText())) {
            System.out.println("Email " + this.email_field.getText());
            System.out.println("Username " + this.username_field.getText());
            System.out.println("Password " + this.password_field.getText());
            SignUpToServer(this.email_field.getText(), this.username_field.getText(), this.password_field.getText());
        } else //show error message, for now - notification
        {
            DisplayNotification("Error in Confirmation", "Passwords don't match");
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
            }
        });
    }

    @Override
    public void setScreenParent(ScreensController screenParent) {
        myController = screenParent;
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
    
    @FXML
    public void ChangeToOnlineChat(ActionEvent event){
        myController.setScreen(main.OnlineChatID);
    }
}
