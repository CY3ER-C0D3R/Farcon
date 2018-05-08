package RemoteControlPage;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import Common.Context;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * FXML Controller class
 *
 * @author admin
 */
public class RemoteControlPaneDrawerController implements Initializable {
    
    RemoteControlPaneFXMLController r;
    String id;
    String password;
    
    @FXML
    private Label meeting_details;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        r = Context.getInstance().getRemoteControlPaneController();
        id = r.getID();
        password = r.getPassword();
        System.out.println("Here initializing Remote Control pane drawer controller");
        System.out.println(id);
        System.out.println(password);
        updateInformation();
    }    
    
    public void updateInformation(){
        String info = String.format("Remote Control Details:\n\nID:\n%s\nPassword:\n%s",this.id, this.password);
        this.meeting_details.setText(info);
    } 
    
    @FXML
    public void copyButtonClicked(){
        
    }
    
    @FXML
    public void closeButtonClicked(){
        
    }
    
    @FXML
    public void exitButtonClicked(){
        System.out.println("Remote control exit button clicked");
        r.exitRemoteControl();
    }
    
}
