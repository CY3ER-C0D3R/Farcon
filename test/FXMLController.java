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


import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

/**
 * FXML Controller class
 *
 * @author admin
 */
public class FXMLController implements Initializable {
    
    
    @FXML
    private AnchorPane pane;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        //ConversationView c = new ConversationView(("Alen"));
        //this.pane.getChildren().add(c);
    } 
    
//    @FXML
//    private void buttonclicked(Event e){
//        System.out.println("here button clicked");
//        DisplayNotification(e.getEventType().toString(), e.getSource().toString());
//    }
    
    @FXML
    public void DisplayNotification(String title, String text) {
        Notifications notificationbuilder = Notifications.create()
                .title("MAIN")
                .text(text)
                .graphic(null)
                .hideAfter(Duration.seconds(5))
                .position(Pos.BOTTOM_RIGHT)
                .onAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        System.out.println("Notification Clicked");
                    }
                });
        notificationbuilder.show();
    }
}
