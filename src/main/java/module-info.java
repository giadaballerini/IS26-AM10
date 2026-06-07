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
    requires com.fasterxml.jackson.core;
    requires javafx.graphics;
    requires javafx.base;
    requires java.logging;


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
    opens it.polimi.ingsw.network.server to java.rmi, com.fasterxml.jackson.databind;
    opens it.polimi.ingsw.network.server.rmi to java.rmi;
    opens it.polimi.ingsw.network.client.rmi to java.rmi;
    opens it.polimi.ingsw.client.rmi to java.rmi;
    opens it.polimi.ingsw.client to java.rmi, com.fasterxml.jackson.databind;
    opens it.polimi.ingsw.network.dto to java.rmi, com.fasterxml.jackson.databind;

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
    opens it.polimi.ingsw.persistency to com.fasterxml.jackson.databind;
    exports it.polimi.ingsw.model.gamemanager;
    exports it.polimi.ingsw.client.socket to java.rmi;
    opens it.polimi.ingsw.client.socket to com.fasterxml.jackson.databind, java.rmi;
    opens it.polimi.ingsw.model.action;
    //Apre i pacchetti a javaFX
    opens it.polimi.ingsw.client.ui.GUI to javafx.graphics, javafx.fxml;
    exports it.polimi.ingsw.client.ui to java.rmi;
    opens it.polimi.ingsw.client.ui to com.fasterxml.jackson.databind, java.rmi;
    exports it.polimi.ingsw.client.data to java.rmi;
    opens it.polimi.ingsw.client.data to com.fasterxml.jackson.databind, java.rmi;
    exports it.polimi.ingsw.client.ui.TUI to java.rmi;
    opens it.polimi.ingsw.client.ui.TUI to com.fasterxml.jackson.databind, java.rmi;
    exports it.polimi.ingsw.client.ui.TUI.utility to java.rmi;
    opens it.polimi.ingsw.client.ui.TUI.utility to com.fasterxml.jackson.databind, java.rmi;
}