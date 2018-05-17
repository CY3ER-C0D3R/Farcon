/*
 * Farcon Software 
 *
 * This program a Group Collaboration and
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
 * This class is the starting point of the application.
 * Sets the properties for the program and the window, and loads the main
 * screens to the ScreensController. Changing screens is done by using 
 * the static variables defined in this class.
 * @author Yuval Stein
 */
public class main extends Application{
    
    /**
     * scene is the main scene of the application and can be referred to 
     * indirectly from other classes.
     */
    private static Scene scene;
    /**
     * robot is used to control the computer when needed and can be referred to 
     * indirectly from other classes.
     */
    private static Robot robot;
    /**
     * mainContainer is used to control the current page displayed and to load 
     * a different page when needed. mainContainer can be referred to directly
     * from other classes.
     */
    public static ScreensController mainContainer = new ScreensController();
    
    /**
     * ID of the Home page file, also referred to as the 
     * Remote Control information page file.
     */
    public static String homePageID = "main";
    public static String homePageFile = "FXMLDocument.fxml";
    /**
     * ID of the Sign Up page file.
     */
    public static String SignUpPageID = "SignUpPage";
    public static String SignUpPageFile = "SignUp_Form.fxml";
    /**
     * ID of the Sign In page file.
     */
    public static String SignInPageID = "SignInPage";
    public static String SignInPageFile = "SignIn_Form.fxml";
    /**
     * ID of the Remote Control pane page file.
     */
    public static String RemotePageID = "RemoteControlPage";
    public static String RemotePageFile = "RemoteControlPaneFXML.fxml";
    /**
     * ID of the Group Meeting page file.
     */
    public static String GroupMeetingID = "GroupMeetingPage";
    public static String GroupMeetingPageFile = "GroupMeetingPage.fxml";
    /**
     * ID of the Group Collaboration page file.
     */
    public static String GroupCollaborationID = "GroupCollaborationPage";
    public static String GroupCollaborationPageFile = "GroupCollaborationPage.fxml";
    /**
     * ID of the Online Chat page file.
     */
    public static String OnlineChatID = "OnlineChatPage";
    public static String OnlineChatPageFile = "OnlineChatPage.fxml";
    
    /**
     * 
     * @param stage 
     * @throws Exception 
     */
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
        // set default profile photo
        Context.getInstance().UpdateProfilePicture();
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
