module it.polimi.ingsw.progettosoftware {
    requires javafx.controls;
    requires javafx.fxml;


    opens it.polimi.ingsw.view to javafx.fxml;
    exports it.polimi.ingsw.model;
    exports it.polimi.ingsw.enumerations;
    exports it.polimi.ingsw.exceptions;
    exports it.polimi.ingsw.observer;
}