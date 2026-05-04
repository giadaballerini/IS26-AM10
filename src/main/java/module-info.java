module it.polimi.ingsw {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;
    requires java.rmi;
    requires java.sql;
    requires org.jline;
    requires org.fusesource.jansi;


    //EXPORT PACCHETTI
    exports it.polimi.ingsw.model;
    exports it.polimi.ingsw.model.entities.card;
    exports it.polimi.ingsw.model.entities.tile;
    exports it.polimi.ingsw.enumerations;
    exports it.polimi.ingsw.exceptions;
    exports it.polimi.ingsw.observer;
    exports it.polimi.ingsw.network.client.rmi to java.rmi;
    exports it.polimi.ingsw.network.server.rmi to java.rmi;
    exports it.polimi.ingsw.network.server to java.rmi;
    exports it.polimi.ingsw.network.dto to java.rmi;
    exports it.polimi.ingsw.client.rmi to java.rmi;
    exports it.polimi.ingsw.client to java.rmi;
    //APRE  i pacchetti network a java.rmi
    opens it.polimi.ingsw.network.server to java.rmi;
    opens it.polimi.ingsw.network.server.rmi to java.rmi;
    opens it.polimi.ingsw.network.client.rmi to java.rmi;
    opens it.polimi.ingsw.client.rmi to java.rmi;
    opens it.polimi.ingsw.client to java.rmi, com.fasterxml.jackson.databind;
    opens it.polimi.ingsw.network.dto to java.rmi;

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
    exports it.polimi.ingsw.client.socket to java.rmi;
    opens it.polimi.ingsw.client.socket to com.fasterxml.jackson.databind, java.rmi;


    //opens it.polimi.ingsw.view to javafx.fxml; DA ATTIVARE QUANDO VIENE CREATA


}