package it.polimi.ingsw.client;

import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.exceptions.InvalidCardException;
import it.polimi.ingsw.network.dto.*;
import java.util.ArrayList;
import java.util.List;

public class VirtualModel {
    private List<TileDTO> queue;
    private List<TileDTO> board;
    private List<CardDTO> upperList;
    private List<CardDTO> lowerList;
    private int numPlayers;
    private GamePhaseEnum currPhaseState;
    public int currAge;
    private List<PlayerDTO> players;
    private String currPlayer;
    private ActionsDTO toDoActions;
    private int currTurn;
    private String nickname;
    private List<PlayerStatusDTO> playerStatuses;
    private List<PlayerStatsDTO> playerStats;

    public VirtualModel(String nickname){
        this.queue = new ArrayList<>();
        this.board = new ArrayList<>();
        this.upperList = new ArrayList<>();
        this.lowerList = new ArrayList<>();
        this.numPlayers = 0;
        this.currPhaseState = null;
        this.currAge = 1;
        this.players = new ArrayList<>();
        this.currPlayer = "";
        this.currTurn = 0;
        this.nickname = nickname;
        this.playerStatuses = new ArrayList<>();
        this.playerStats = new ArrayList<>();
        this.toDoActions = new ActionsDTO(0,0, false);
    }
    public VirtualModel(){
        this.queue = new ArrayList<>();
        this.board = new ArrayList<>();
        this.upperList = new ArrayList<>();
        this.lowerList = new ArrayList<>();
        this.numPlayers = 0;
        this.currPhaseState = null;
        this.currAge = 1;
        this.players = new ArrayList<>();
        this.currPlayer = "";
        this.currTurn = 0;
        this.playerStatuses = new ArrayList<>();
        this.playerStats = new ArrayList<>();
        this.toDoActions = new ActionsDTO(0,0, false);
    }


    public List<TileDTO> getQueue() {
        return new ArrayList<>(queue);
    }

    public List<TileDTO> getBoard() {
        return new ArrayList<>(board);
    }

    public List<CardDTO> getUpperList() {
        return new ArrayList<>(upperList);
    }
    public List<CardDTO> getLowerList() {
        return new ArrayList<>(lowerList);
    }
    public int getNumPlayers() {
        return numPlayers;
    }
    public GamePhaseEnum getCurrentPhase() {
        return currPhaseState;
    }

    public int getCurrAge() {
        return currAge;
    }

    public List<PlayerDTO> getPlayers(){
        return new ArrayList<>(players);
    }

    public String getCurrPlayer(){
        return currPlayer;
    }
    public ActionsDTO getToDoActions(){
        return new ActionsDTO(toDoActions);
    }
    public int getCurrTurn() {
        return currTurn;
    }
    public List<PlayerStatsDTO> getPlayerStats() {return new ArrayList<>(playerStats);}
    public List<PlayerStatusDTO> getPlayerStatuses() {return new ArrayList<>(playerStatuses);}


    public synchronized void update(BoardDTO b) {
        this.numPlayers = b.getNumPlayers();
        this.queue = new ArrayList<>(b.getqueueTiles());
        this.board = new ArrayList<>(b.getboardTiles());
        this.upperList = new ArrayList<>(b.getUpperList());
        this.lowerList = new ArrayList<>(b.getLowerList());
        this.players = new ArrayList<>(b.getPlayers());
        this.playerStats = new ArrayList<>(b.getPlayerStats());
        this.currPhaseState = b.getCurrentPhase();
        this.currPlayer = b.getCurrentPlayerNickname();
        this.toDoActions =b.getTodoActions();
        this.currTurn = b.getCurrTurn();
        if (this.playerStatuses.isEmpty()) {
            for (PlayerDTO p : this.players) {
                this.playerStatuses.add(new PlayerStatusDTO(p.getNickname()));
            }
        }
    }


    public synchronized void onMoveUpdate(TileDTO tile){
        for(int i = 0;i < board.size();i++){
            if(board.get(i).getId() == tile.getId()){
                board.set(i, new TileDTO(tile, currPlayer, true));
                break;
            }
        }
        for(int i = 0;i < queue.size();i++){
            if(queue.get(i).getPlayer().equals(currPlayer)){
                queue.set(i, new TileDTO(queue.get(i), "", false));
                break;
            }
        }
    }

    public synchronized void onCurrPlayerUpdate(String player){
        currPlayer = player;
    }

    public synchronized void onPhaseUpdate(PhaseDTO phase){
        currPhaseState = phase.getPhase();
    }

    //per rimuovere if-else si potrebbe implementare strategy pattern
    public synchronized void onDrawUpdate(CardDTO c, String nickname){
        boolean found = false;
        for(int i = 0;i < upperList.size() && !found; i++){
            if(upperList.get(i).getId() == c.getId()){
                upperList.remove(i);
                found = true;
            }
        }
        for(int i = 0; i < lowerList.size() && !found; i++){
            if(lowerList.get(i).getId() == c.getId()){
                lowerList.remove(i);
                found = true;
            }
        }
        for(int i = 0;i < players.size();i++){
            if(players.get(i).getNickname().equals(nickname)){
                if(CardRegistry.getType(c.getId()) == CardTypeEnum.BUILDING) {
                    List<CardDTO> builds = new ArrayList<>(players.get(i).getMyBuildings());
                    builds.add(c);
                    players.set(i, players.get(i).withMyBuilds(builds));
                }else {
                    List<CardDTO> chars = new ArrayList<>(players.get(i).getMyCharacters());
                    chars.add(c);
                    players.set(i, players.get(i).withMyCharacters(chars));
                }
                break;
            }
        }
    }

    public synchronized void onStatusUpdate(PlayerStatusDTO status) {
        for (int i = 0; i < playerStatuses.size(); i++) {
            if (playerStatuses.get(i).getNickname().equals(status.getNickname())) {
                playerStatuses.set(i, new PlayerStatusDTO(
                        status.getNickname(),
                        status.hasProtection(),
                        status.hasDoubleShamanIncome(),
                        status.isExtraFlag(),
                        status.isPaintFlag(),
                        status.getCategoryDiscounts(),
                        status.isHuntBonus()
                ));
                break;
            }
        }
    }

    public synchronized void onStatsUpdate(PlayerStatsDTO stats){
        for(int i = 0; i < playerStats.size();i++){
            if(playerStats.get(i).getNickname().equals(stats.getNickname())){
                playerStats.set(i, new PlayerStatsDTO(playerStats.get(i).getNickname(), stats.getnFood(), stats.getPPs(), stats.getnStars(), stats.getTotBuildDisc(), stats.getFoodDiscount()));
            }
        }
    }

    public synchronized void updateAllStats(List<PlayerStatsDTO> stats){
        this.playerStats = new ArrayList<>(stats);
    }

    public synchronized void onReturnToQueue(TileDTO tileDTO, PlayerStatsDTO playerStatsDTO){

        for(int i = 0; i < queue.size(); i++){
            if(queue.get(i).getId() == tileDTO.getId()){
                queue.set(i, new TileDTO(tileDTO, playerStatsDTO.getNickname(), true));
                break;
            }
        }
        for(int i = 0; i < board.size(); i++){
            TileDTO boardTile = board.get(i);
            if(boardTile.isOccupied() && boardTile.getPlayer().equals(currPlayer)){
                board.set(i, new TileDTO(boardTile, "", false)); // copia della board tile, non della queue tile
                break;
            }
        }
        for(int i = 0; i < playerStats.size(); i++){
            if(playerStats.get(i).getNickname().equals(playerStatsDTO.getNickname())){
                playerStats.set(i, new PlayerStatsDTO(
                        playerStats.get(i).getNickname(),
                        playerStatsDTO.getnFood(),
                        playerStatsDTO.getPPs(),
                        playerStatsDTO.getnStars(),
                        playerStatsDTO.getTotBuildDisc(),
                        playerStatsDTO.getFoodDiscount()));
                break;
            }
        }
    }

    public synchronized void onChangeAge(ChangeAgeDTO c){
        this.currAge = c.getAge();
        this.upperList = new ArrayList<>(c.getUpperList());
        this.lowerList = new ArrayList<>(c.getLowerList());
    }

    public synchronized void onEvent(EventDTO e){

    }


    public String getNickname(){
        return this.nickname;
    }


    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateToDoActions(ActionsDTO a){
        toDoActions = new ActionsDTO(a);
    }

    public void skip() {
        toDoActions = new ActionsDTO(0,0, false);
    }

    public CardDTO findCardById(int cardId) {
        for(CardDTO c : upperList) {
            if(c.getId() == cardId) {
                return c;
            }
        }
        for(CardDTO c : lowerList) {
            if(c.getId() == cardId) {
                return c;
            }
        }
        for(PlayerDTO p : players) {
            for(CardDTO c : p.getMyCharacters()) {
                if(c.getId() == cardId) {
                    return c;
                }
            }
            for(CardDTO c : p.getMyBuildings()) {
                if(c.getId() == cardId) {
                    return c;
                }
            }
        }
        throw new InvalidCardException("La carta con ID " + cardId + " non è presente nel gioco.");
    }
}
