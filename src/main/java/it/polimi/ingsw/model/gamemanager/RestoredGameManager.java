package it.polimi.ingsw.model.gamemanager;

import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.action.Action;
import it.polimi.ingsw.model.entities.tile.Tile;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.observer.ModelObserver;
import it.polimi.ingsw.persistency.GameSnapshot;

import java.util.ArrayList;
import java.util.List;

public class RestoredGameManager extends GameManager{
    public RestoredGameManager(GameSnapshot snapshot, List<ModelObserver> observers, Runnable onGameEndedCallback) {
        super(observers, snapshot.getPlayers(), snapshot.getNumPlayers(), onGameEndedCallback);
        state.setDeck(new ArrayList<>(snapshot.getDeck()));
        state.setBuildings(new ArrayList<>(snapshot.getBuildings()));
        state.setUpperList(new ArrayList<>(snapshot.getUpperList()));
        state.setLowerList(new ArrayList<>(snapshot.getLowerList()));
        state.setBoard(new ArrayList<>(snapshot.getBoard()));
        state.setQueue(new ArrayList<>(snapshot.getQueue()));


        reconnectTilePlayers(snapshot.getPlayers());

        state.setCurrAge(snapshot.getCurrAge());
        state.setCurrTurn(snapshot.getCurrTurn());
        state.setSkippableDraw(snapshot.isSkippableDraw());

        this.currPhaseState = phaseStateFrom(snapshot.getCurrentPhase());



        state.getPlayers().stream()
                .filter(p -> p.getNickname().equals(snapshot.getCurrentPlayerNickname()))
                .findFirst()
                .ifPresent(state::setCurrPlayer);

        List<Action> toDoActions = new ArrayList<>();
        if (snapshot.getToDoActions() != null) {
            for (GameSnapshot.PendingAction pa : snapshot.getToDoActions()) {
                state.getPlayers().stream()
                        .filter(player -> player.getNickname().equals(pa.getOwnerNickname()))
                        .findFirst()
                        .ifPresent(owner -> toDoActions.add(new Action(owner, pa.getType())));
            }
        }
        state.setToDoActions(toDoActions);
    }

    public void resume() {
        LOG.info("[RestoredGameManager] Ripresa partita dal salvataggio – fase: "
                + currPhaseState.getPhase());
        notifier.showBoard(state.toDTO(currPhaseState));
        notifier.notifyPhaseUpdate(currPhaseState);
        notifier.notifyCurrPlayerUpdate(state.getCurrPlayer().getNickname());
    }

    private void reconnectTilePlayers(List<Player> savedPlayers) {
        state.getBoard().forEach(tile -> reconnectTile(tile, savedPlayers));
        state.getQueue().forEach(tile -> reconnectTile(tile, savedPlayers));
    }

    private void reconnectTile(Tile tile, List<Player> savedPlayers) {
        String nickname = tile.getPlayerNickname();
        if (nickname == null || nickname.isEmpty()) return;
        savedPlayers.stream()
                .filter(p -> p.getNickname().equals(nickname))
                .findFirst()
                .ifPresent(tile::setPlayer);
    }

    private GamePhaseState phaseStateFrom(GamePhaseEnum phase) {
        return switch (phase) {
            case SETUP_PHASE         -> new SetupPhaseState();
            case DRAW_PHASE          -> new DrawPhaseState();
            case OPTIONAL_DRAW_PHASE -> new OptionalDrawPhaseState();
            case END_TURN            -> new EndTurnPhaseState();
            case END_ROUND           -> new EndRoundPhaseState();
            case END_GAME            -> new EndGamePhaseState();
            default -> null;
        };
    }
}

