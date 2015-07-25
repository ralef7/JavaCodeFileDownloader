package alef.uchicago.edu;

/**
 * Sample Skeleton for 'threaded.fxml' Controller Class
 */

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;

public class ThreadedController {

    @FXML // fx:id="mnuQuit"
    private MenuItem mnuQuit; // Value injected by FXMLLoader

    @FXML // fx:id="lblResult"
    private Label lblResult; // Value injected by FXMLLoader

    @FXML // fx:id="btnGo"
    private Button btnGo; // Value injected by FXMLLoader

    @FXML
    void btnGoAction(ActionEvent event) {

        lblResult.setText("Result");

    }

    @FXML
    void mnuQuitAction(ActionEvent event) {

        System.exit(0);

    }


}

