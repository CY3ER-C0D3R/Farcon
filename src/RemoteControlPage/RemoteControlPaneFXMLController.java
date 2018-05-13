/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RemoteControlPage;

import Common.RemoteGUIInterface;
import Common.RemoteConnectionData;
import Common.RemoteConnectionDataInterface;
import GroupCollaborationPage.GroupCollaborationPageController;
import Common.ControlledScreen;
import Common.ScreensController;
import Main.main;
import Common.Context;
import com.jfoenix.controls.JFXDrawer;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import javafx.scene.input.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.util.Duration;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.controlsfx.control.Notifications;

/**
 * FXML Controller class
 *
 * @author admin
 */
public class RemoteControlPaneFXMLController implements Initializable, ControlledScreen, RemoteConnectionDataInterface, RemoteGUIInterface {

    ScreensController myController;
    RemoteClient rc;
    private String remote_ip;
    private int remote_port;
    private String remote_ID;
    private String remote_Password;
    
    private boolean connected;
    
    @FXML
    private AnchorPane command_pane;
        
    @FXML
    private ImageView command_panel;
    
    @FXML
    private JFXDrawer drawer;
    
    /**
     * Initializes the controller class.
     */
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.connected = false;
        Context.getInstance().setRemoteControlPaneController(this);
        // update remote data
        this.SetRemoteData(Context.getInstance().getRemote_data());
        // link the drawer to this page
        try {
            VBox box = FXMLLoader.load(getClass().getResource("/View/RemoteControlPaneDrawer.fxml"));
            this.drawer.setSidePane(box);
            this.drawer.setDefaultDrawerSize(120);
        } catch (IOException ex) {
            Logger.getLogger(GroupCollaborationPageController.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Check size:");
        System.out.println(this.command_panel.getFitWidth());
        System.out.println(this.command_panel.getFitHeight());
        System.out.println(this.command_pane.getWidth());
        System.out.println(this.command_pane.getHeight());
        // create a new client object and connect to server
        System.out.println("Remote Server Info");
        System.out.println(Context.getInstance().getRemote_data().toString());
        // make remote control pane full screen
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        this.command_pane.setPrefWidth(primaryScreenBounds.getWidth());
        this.command_pane.setPrefHeight(primaryScreenBounds.getHeight());
        // fit the command panel to the same size
        this.command_panel.fitWidthProperty().bind(this.command_pane.widthProperty());
        this.command_panel.fitHeightProperty().bind(this.command_pane.heightProperty());
        this.command_panel.requestFocus();
        rc = new RemoteClient(Context.getInstance().getUsername(), this, this.remote_ID, this.remote_Password, this.remote_ip, this.remote_port);
        
    }    

    @Override
    public void setScreenParent(ScreensController screenParent) {
        myController = screenParent;
    }
    
    public String getID(){
        return this.remote_ID;
    }
    
    public String getPassword(){
        return this.remote_Password;
    }
    
    @Override
    public void SetRemoteData(RemoteConnectionData rc_data) {
        this.remote_ip = rc_data.getRemote_ip();
        this.remote_port = rc_data.getRemote_port();
        this.remote_ID = rc_data.getRemote_ID();
        this.remote_Password = rc_data.getRemote_Password();
    }
    
    public void DisplayNotification(String title, String text) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                javafx.scene.image.Image img = new javafx.scene.image.Image("Resources/falcon_icon.png");
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
    
        /**
     * 
     * @param p point which represents a x or y coordinate
     * @param isX boolean variable to determine if dot is x or y
     * @return calculation of the new location of the dot according to the size
     * of the panel
     */
    public double CalculateRelativePosition(double p, boolean isX){
        if(isX) //dot is on the x scale
            return p / this.command_panel.getBoundsInParent().getWidth();
        //dot is on the y scale
        return p / this.command_panel.getBoundsInParent().getHeight();
    }
    
    public String GetMouseButton(MouseEvent e){
        String keyClicked = "null"; 
        if (e.getButton() == MouseButton.SECONDARY){
            keyClicked = "Right";
        }
        else if (e.getButton() == MouseButton.PRIMARY){
            keyClicked = "Left";
        }
        else if (e.getButton() == MouseButton.MIDDLE){
            keyClicked = "Middle";
        }
        else //todo - check if mouse has more than 3 buttons
        {
            keyClicked = "null";
        }
        return keyClicked;
    }
    
    @Override
    public void setConnected(boolean connected) {
        this.connected = connected;
        if (!this.connected) // wrong password was entered, go back to home screen
        {
            System.out.println("here");
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    //rc.CloseSocket();
                    DisplayNotification("Remote Connection Denied", "Wrong password entered.");
                    myController.setScreen(main.homePageID);
                }
            });
        }
    }
    
    @Override
    public void UpdateScreen(BufferedImage b)
    {
//        BufferedImage dbi = null;
//        int dWidth = b.getWidth();
//        int dHeight = b.getHeight();
//        double fWidth = this.command_pane.getWidth();
//        double fHeight = this.command_pane.getHeight();
//        if(b != null) {
//            dbi = new BufferedImage(dWidth, dHeight, 1);
//            Graphics2D g = dbi.createGraphics();
//            AffineTransform at = AffineTransform.getScaleInstance(fWidth, fHeight);
//            g.drawRenderedImage(b, at);
//        }
        WritableImage picture = SwingFXUtils.toFXImage(b, null);
        System.out.println(picture.getWidth());
        System.out.println(picture.getHeight());
        
//        double width = this.command_pane.getWidth();
//        double height = this.command_pane.getHeight();
//        System.out.println(String.format("%s %s",width,height));
//        System.out.println(String.format("%s %s",picture.getWidth(),picture.getHeight()));


        //System.out.println(picture.getWidth());
        //System.out.println(picture.getHeight());
        //System.out.println(this.command_pane.getWidth());
        //System.out.println(this.command_pane.getHeight());
        //System.out.println(this.command_panel.fitWidthProperty());
        //System.out.println(this.command_panel.fitHeightProperty());
        this.command_panel.setImage(picture);
        this.command_panel.setPreserveRatio(true);
        
//        this.command_panel.setFitWidth(width);
//        this.command_panel.setFitHeight(height);
    }
    
    /**
     * Function updates the information label and sends the mouse pressed event
     * to the server.
     * @param e MouseEvent
     */
    @FXML
    public void mousePressed(MouseEvent e){
        String keyClicked = GetMouseButton(e);
        System.out.println(String.format("Event=Mouse;Type=Pressed;Parameters=%s;Position=%f:%f;",keyClicked,e.getX(),e.getY()));
        System.out.println(keyClicked);
        //calculate correct position of cursor 
        double x = CalculateRelativePosition(e.getX(), true);
        double y = CalculateRelativePosition(e.getY(), false);
        //send commands to server
        this.rc.SendCommand(String.format("Event=Mouse;Type=Pressed;Parameters=%s;Position=%f:%f;",keyClicked,x,y));
        this.rc.RecvReply();
    }
    
    /**
     * Function updates the information label and sends the mouse released event
     * to the server.
     * @param e MouseEvent
     */
    @FXML
    public void mouseReleased(MouseEvent e) {
        String keyClicked = GetMouseButton(e);
        
         //calculate correct position of cursor 
        double x = CalculateRelativePosition(e.getX(), true);
        double y = CalculateRelativePosition(e.getY(), false);
        System.out.println(String.format("Event=Mouse;Type=Released;Parameters=%s;Position=%f:%f;",keyClicked,e.getX(),e.getY()));
        System.out.println(keyClicked);
        //send commands to server
        this.rc.SendCommand(String.format("Event=Mouse;Type=Released;Parameters=%s;Position=%f:%f;",keyClicked,x,y));
        this.rc.RecvReply();
    }
    /*

    /**
     * Function updates the information label from where the mouse entered the 
     * panel.
     * @param e MouseEvent
     */
    @FXML
    public void mouseEntered(MouseEvent e) {
        if(e.getY() > this.command_panel.getFitHeight() || e.getY() < 0 
                || e.getX() > this.command_panel.getFitWidth()
                || e.getX() < 0)
            return;
        //else - display data
        System.out.println(String.format("Mouse Entered from: "
                                    + "%f, %f", e.getX(), e.getY()));
    }
    
    /**
     * Function updates the information label from where the mouse exited the 
     * panel.
     * @param e MouseEvent
     */
    @FXML
    public void mouseExited(MouseEvent e) {
        if(e.getY() > this.command_panel.getFitHeight() || e.getY() < 0 
                || e.getX() > this.command_panel.getFitWidth() 
                || e.getX() < 0)
            return;
        //else - display data
        System.out.println(String.format("Mouse Exited from: "
                                    + "%f, %f", e.getX(), e.getY()));
    }
    
    /**
     * Function updates the information label and sends the mouse dragged event
     * to the server.
     * @param e MouseEvent
     */
    @FXML
    public void mouseDragged(MouseEvent e) {
        if(e.getY() > this.command_panel.getFitHeight() || e.getY() < 0 
                || e.getX() > this.command_panel.getFitWidth() 
                || e.getX() < 0)
            return;
        if(e.getSource() != this.command_panel)
            System.out.println("not command panel");
        else
            System.out.println("command panel");
        //else - display data
        String keyClicked = GetMouseButton(e);
        
        System.out.println(String.format("Mouse is being dragged, now at: %f, %f", e.getX(), e.getY()));
        
        //calculate correct position of cursor 
        double x = CalculateRelativePosition(e.getX(), true);
        double y = CalculateRelativePosition(e.getY(), false);
        //send commands to server
        this.rc.SendCommand(String.format("Event=Mouse;Type=Dragged;Parameters=%s;Position=%f:%f;",keyClicked,x,y));
        this.rc.RecvReply();
    }
    
    /**
     * Function updates the information label and sends the mouse moved event
     * to the server.
     * @param e MouseEvent
     */
    @FXML
    public void mouseMoved(MouseEvent e) {
        if(e.getY() > this.command_panel.getFitHeight() || e.getY() < 0 
                || e.getX() > this.command_panel.getFitWidth() 
                || e.getX() < 0)
            return;
        //else - display data
        String keyClicked = GetMouseButton(e);
        
        System.out.println(String.format("Mouse is being moved, now at: %f, %f", e.getX(), e.getY()));
        System.out.println(keyClicked);
        
        //calculate correct position of cursor 
        double x = CalculateRelativePosition(e.getX(), true);
        double y = CalculateRelativePosition(e.getY(), false);
        
        //send commands to server
        this.rc.SendCommand(String.format("Event=Mouse;Type=Moved;Parameters=%s;Position=%f:%f;",keyClicked,x,y));
        this.rc.RecvReply();
    }  
    
    /**
     * Function updates the information label and sends the mouse wheel moved
     * event to the server.
     * @param e MouseEvent
     */
    @FXML
    public void mouseWheelMoved(ScrollEvent e) {
        
        String message;
        int notches = (int)(e.getDeltaY());
        if (notches < 0) {
            message = "Mouse wheel moved UP "
                         + -notches + " notch(es)" + "\n";
        } else {
            message = "Mouse wheel moved DOWN "
                         + notches + " notch(es)" + "\n";
        }
//        if (e.getScrollType() == ScrollEvent.WHEEL_UNIT_SCROLL) {
//            message += "    Scroll type: WHEEL_UNIT_SCROLL" + "\n";
//            message += "    Scroll amount: " + e.getScrollAmount()
//                    + " unit increments per notch" + "\n";
//            message += "    Units to scroll: " + e.getUnitsToScroll()
//                    + " unit increments" + "\n";
//            message += "    Vertical unit increment: "
//               // + scrollPane.getVerticalScrollBar().getUnitIncrement(1)
//                + " pixels" + "\n";
//        } else { //scroll type == MouseWheelEvent.WHEEL_BLOCK_SCROLL
//            message += "    Scroll type: WHEEL_BLOCK_SCROLL" + "\n";
//            message += "    Vertical block increment: "
//               // + scrollPane.getVerticalScrollBar().getBlockIncrement(1)
//                + " pixels" + "\n";
//        }
        System.out.println(message);
        
        //calculate correct position of cursor 
        double x = CalculateRelativePosition(e.getX(), true);
        double y = CalculateRelativePosition(e.getY(), false);
        //send commands to server
        this.rc.SendCommand(String.format("Event=Mouse;Type=WheelMoved;Parameters=%s;Position=%f:%f;",notches,x,y));
        this.rc.RecvReply();
    }
    
    //key listener events
    
    /** Handle the key typed event from the text field. */
    @FXML
    public void keyTyped(KeyEvent e) {
        //displayInfo(e, "KEY TYPED: ");
        System.out.println(e);
    }

    /** Handle the key-pressed event from the text field. */
    
    @FXML
    public void keyPressed(KeyEvent event) {
        
        System.out.println("Key Pressed: " + event.getCode().getName());
        String parameters = String.format("Code=%d;Alt=%b;Ctrl=%b;Meta=%b;Shift=%b", 
                event.getCode().impl_getCode(),
                event.isAltDown(),
                event.isControlDown(),
                event.isMetaDown(),
                event.isShiftDown());
        System.out.println(parameters);
        
        //send key command to server
        this.rc.SendCommand(String.format("Event=Key;Type=Pressed;%s;",parameters));
        this.rc.RecvReply();
    }
    
    public void HandleHotKeys(KeyEvent event){
        // if windows key was pressed, press it again to 'cancel it'
        if (event.getCode().equals(KeyCode.WINDOWS)) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(RemoteControlPaneFXMLController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    System.out.println("Here!");
                    main.getRobot().keyPress(event.getCode().impl_getCode());
                    main.getRobot().keyRelease(event.getCode().impl_getCode());
                }
            });
        }
    }

    /** Handle the key-released event from the text field. */
    @FXML
    public void keyReleased(KeyEvent event) {
       
        System.out.println("Key Released: " + event.getCode().getName());
        String parameters = String.format("Code=%d;Alt=%b;Ctrl=%b;Meta=%b;Shift=%b", 
                event.getCode().impl_getCode(),
                event.isAltDown(),
                event.isControlDown(),
                event.isMetaDown(),
                event.isShiftDown());
        System.out.println(parameters);
        
        // check hotkeys and act accordingly
        HandleHotKeys(event);
        
        //send key command to server
        this.rc.SendCommand(String.format("Event=Key;Type=Released;%s;",parameters));
        this.rc.RecvReply();
    }    
    
    @FXML
    public void mouseEnteredDrawer(MouseEvent event){
        System.out.println("DRAWER OPENING");
        drawer.open();
    } 
    
    @FXML
    public void mouseExitedDrawer(MouseEvent event){
        System.out.println("DRAWER CLOSING");
        drawer.close();
    }
    
    public void exitRemoteControl(){
        this.rc.CloseSocket();
        main.mainContainer.setScreen(main.homePageID);
    }
}
