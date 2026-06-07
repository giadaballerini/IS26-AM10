package it.polimi.ingsw.client.ui.GUI;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class NetworkChoiceController {

    @FXML
    private Button socketButton;

    @FXML
    private Button rmiButton;

    @FXML
    private void connectWithSocket(){
        socketButton.setDisable(true);
        rmiButton.setDisable(true);
        loadTable();
    }
    private void ConnectWithRMI(){
        socketButton.setDisable(true);
        rmiButton.setDisable(true);
        loadTable();
    }

    private void loadTable(){

    }
}
