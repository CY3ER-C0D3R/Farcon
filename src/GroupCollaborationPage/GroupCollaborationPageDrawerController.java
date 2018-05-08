/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GroupCollaborationPage;

import Common.Context;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;

/**
 * FXML Controller class
 *
 * @author admin
 */
public class GroupCollaborationPageDrawerController implements Initializable {
    
    private GroupCollaborationPageController gc;
    private String GID;
    private String G_Password;
    
    @FXML
    private Label group_meeting_details;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        gc = Context.getInstance().getGc();
        this.GID = gc.getGroupID();
        this.G_Password = gc.getGroupPassword();
        updateGroupInformation();
    }    
    
    public void updateGroupInformation(){
        String info = String.format("Meeting Details:\n\nGroup ID:\n%s\nGroup Password:\n%s",this.GID, this.G_Password);
        this.group_meeting_details.setText(info);
    }
    
    @FXML
    public void copyButtonClicked(ActionEvent event){
        gc.copyToClipboard();
    }
    
    @FXML
    public void saveButtonClicked(ActionEvent event){
        // trigger the save function in gc
        gc.SaveFile();
    }
    
    @FXML
    public void exitGroupButtonClicked(ActionEvent event){
        gc.exitGroup();
    }
    
}
