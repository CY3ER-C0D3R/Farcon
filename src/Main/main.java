/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Main;

import Common.ScreensController;
import Common.Context;
import com.sun.glass.ui.Robot;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author admin
 */
public class main extends Application{
    
    private static Scene scene;
    private static Robot robot;
    
    public static ScreensController mainContainer = new ScreensController();
    
    // Home page
    public static String homePageID = "main";
    public static String homePageFile = "FXMLDocument.fxml";
    // Sign Up page
    public static String SignUpPageID = "SignUpPage";
    public static String SignUpPageFile = "SignUp_Form.fxml";
    // Sign In page
    public static String SignInPageID = "SignInPage";
    public static String SignInPageFile = "SignIn_Form.fxml";
    // Remote Control Page
    public static String RemotePageID = "RemoteControlPage";
    public static String RemotePageFile = "RemoteControlPaneTestFXML.fxml";
    //public static String RemotePageFile = "RemoteControlPaneFXML.fxml";
    // Group Meeting Page
    public static String GroupMeetingID = "GroupMeetingPage";
    public static String GroupMeetingPageFile = "GroupMeetingPage.fxml";
    // Group Collaboraton Page
    public static String GroupCollaborationID = "GroupCollaborationPage";
    public static String GroupCollaborationPageFile = "GroupCollaborationPage.fxml";
    // Online Chat Page
    public static String OnlineChatID = "OnlineChatPage";
    public static String OnlineChatPageFile = "OnlineChatPage.fxml";
    
    @Override
    public void start(Stage stage) throws Exception {
        
        robot = com.sun.glass.ui.Application.GetApplication().createRobot();
        
        System.setProperty("javax.net.ssl.trustStore", "./src/Resources/yuval.store");

        //System.setProperty("javax.net.debug", "all");
        
        ReadServerDataFile();
        
        //stage.initStyle(StageStyle.TRANSPARENT);
                
        mainContainer.loadScreen(main.homePageID, main.homePageFile);
        mainContainer.loadScreen(main.SignUpPageID, main.SignUpPageFile);
        mainContainer.loadScreen(main.SignInPageID, main.SignInPageFile);
        mainContainer.loadScreen(main.GroupMeetingID, main.GroupMeetingPageFile);
        mainContainer.loadScreen(main.OnlineChatID, main.OnlineChatPageFile);
        
        
        mainContainer.setScreen(main.homePageID);
                    
        // make sure stage is the topmost window on startup
        stage.setAlwaysOnTop(true);
        stage.setOpacity(0.95);
        
        //update title and icon of application
        stage.setTitle("FARCON");
        stage.getIcons().add(new Image("/Resources/falcon_icon.png"));
        // make the form only resizable to bigger or
        // equal than the following values
        stage.setMinHeight(700);
        stage.setMinWidth(900);
        
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                FXMLDocumentController.exit();
                Platform.exit();
                System.exit(0);
            }
        });
        
        Group root = new Group();
        root.getChildren().addAll(mainContainer);
        scene = new Scene(root);
        mainContainer.prefWidthProperty().bind(scene.widthProperty());
        mainContainer.prefHeightProperty().bind(scene.heightProperty());
        stage.setScene(scene);
        stage.show();
        
        // don't make the stage always on top
        stage.setAlwaysOnTop(false);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    public static Scene getScene() {
        return scene;
    }

    public static Robot getRobot() {
        return robot;
    }
        
    public void ReadServerDataFile(){
        // function reads the server information from the file
        // and update Context with the correct info
        
         JSONParser parser = new JSONParser();

        try {     
            InputStream stream = this.getClass().getResourceAsStream("/Resources/settings.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            Object obj = parser.parse(reader);
            JSONObject jsonObject = (JSONObject) obj;
            
            String ip = (String)jsonObject.get("ip");
            String serverhostname = (String)jsonObject.get("host");
            int port = (int)(long)jsonObject.get("port");
            int connectionTimeout = (int)(long)jsonObject.get("timeout");
            
            Context.getInstance().setServerhostname(serverhostname);
            Context.getInstance().setServerIP(ip);
            Context.getInstance().setServerPort(port);
            Context.getInstance().setConnectionTimeout(connectionTimeout);

        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        } catch (IOException | ParseException e) {
            System.err.println(e.getMessage());
        }
    }
}
