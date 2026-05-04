package it.polimi.ingsw.model.gamemanager;

public interface ApplicableActions {
    void onMoveRequested(String nick, int tilePos);
    void onDrawCardRequested(String nick, int cardId);
    void onSkipRequested(String nick);
    /*void onJoin();
    void onCreate();
    void onRankingRequest();*/
}
