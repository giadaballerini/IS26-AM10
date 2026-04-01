module it.polimi.ingsw {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;


    //EXPORT PACCHETTI IN CUI SI TROVANO Card E Tile per parsing di Jackson
    exports it.polimi.ingsw.model;
    exports it.polimi.ingsw.model.entities.card;
    exports it.polimi.ingsw.model.entities.tile;
    exports it.polimi.ingsw.enumerations;
    exports it.polimi.ingsw.exceptions;
    exports it.polimi.ingsw.observer;


    //APRE i pacchetti a Jackson
    opens it.polimi.ingsw.model.entities.card.types.event to com.fasterxml.jackson.databind;
    opens it.polimi.ingsw.model.entities.card.types.building to com.fasterxml.jackson.databind;
    opens it.polimi.ingsw.model.entities.tile to com.fasterxml.jackson.databind;
    opens it.polimi.ingsw.model.entities.card to com.fasterxml.jackson.databind;
    opens it.polimi.ingsw.enumerations to com.fasterxml.jackson.databind;
    opens it.polimi.ingsw.model.player to com.fasterxml.jackson.databind;
    opens it.polimi.ingsw.model.entities.card.types.character to com.fasterxml.jackson.databind;
    opens it.polimi.ingsw.model.entities.card.effects.instant to com.fasterxml.jackson.databind;
    opens it.polimi.ingsw.model.entities.card.effects.interactive to com.fasterxml.jackson.databind;
    exports it.polimi.ingsw.model.gamemanager;

    //opens it.polimi.ingsw.view to javafx.fxml; DA ATTIVARE QUANDO VIENE CREATA


}