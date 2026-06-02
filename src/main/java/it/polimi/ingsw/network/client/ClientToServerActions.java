package it.polimi.ingsw.network.client;



public interface ClientToServerActions{
    boolean login(String nickname);

    void createGame(String nickname, int numPlayers);

    void joinGame(String nickname, int id);

    void move(int tileId);

    void skip();

    void draw(int card);

    void requestRanking();

    void requestJoin();

    void quit();

    void exit();
}
