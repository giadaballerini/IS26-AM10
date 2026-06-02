package it.polimi.ingsw.client;

import it.polimi.ingsw.network.client.ClientToServerActions;

public abstract class Client implements ClientToServerActions {

    public abstract String getNickname();

    public abstract boolean hasAvailableLobbies();

    public abstract void start();

    public abstract boolean isInGame();

    public abstract void showStatus();
    public abstract void help();

    public abstract void info(int cardId);

    public abstract void setUi(UserInterface ui);

    public abstract VirtualModel getModel();
}
