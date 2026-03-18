module it.polimi.ingsw {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;


    //EXPORT PACCHETTI IN CUI SI TROVANO Card E Tile per parsing di Jackson
    exports it.polimi.ingsw.model.entities.card;
    exports it.polimi.ingsw.model.entities.tile;

    //APRE i paccheti a Jackson
    opens it.polimi.ingsw.model.entities.card to com.fasterxml.jackson.databind;
    opens it.polimi.ingsw.model.entities.tile to com.fasterxml.jackson.databind;


    opens it.polimi.ingsw.view to javafx.fxml;
    exports it.polimi.ingsw.model;
    exports it.polimi.ingsw.enumerations;
    exports it.polimi.ingsw.exceptions;
    exports it.polimi.ingsw.observer;

}