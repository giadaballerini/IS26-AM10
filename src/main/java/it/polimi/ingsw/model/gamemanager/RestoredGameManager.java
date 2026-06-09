package it.polimi.ingsw.model.gamemanager;

import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.action.Action;
import it.polimi.ingsw.model.entities.tile.Tile;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.observer.ModelObserver;
import it.polimi.ingsw.persistency.GameSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link GameManager} that restores a previously saved match from a
 * {@link GameSnapshot}.
 *
 * <p>The constructor rebuilds the full game state (deck, board, queue, players,
 * phase, pending actions, etc.) from the snapshot and re-establishes the
 * references between tiles and their occupying players, which are lost during
 * JSON serialization. Once constructed, {@link #resume()} broadcasts the
 * restored state to all connected clients so they can resync their views.</p>
 */
public class RestoredGameManager extends GameManager {

    /**
     * Constructs a {@code RestoredGameManager} from a saved snapshot.
     *
     * <p>All game state is reconstructed from {@code snapshot}: card lists,
     * board and queue tiles, player state, current phase, turn/age counters,
     * and pending actions. Tile-to-player references are re-established by
     * matching nicknames stored in each tile against the deserialized player
     * list.</p>
     *
     * @param snapshot             the snapshot to restore from; must not be {@code null}
     * @param observers            the list of observers for the restored match;
     *                             must not be {@code null}
     * @param onGameEndedCallback  callback invoked when the match ends;
     *                             must not be {@code null}
     */
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

    /**
     * Broadcasts the restored game state to all connected clients so that
     * they can resync their views after reconnecting.
     *
     * <p>Sends the current board state, the active game phase, and the
     * current player's nickname.</p>
     */
    public void resume() {
        LOG.info("[RestoredGameManager] Ripresa partita dal salvataggio – fase: "
                + currPhaseState.getPhase());
        notifier.showBoard(state.toDTO(currPhaseState));
        notifier.notifyPhaseUpdate(currPhaseState);
        notifier.notifyCurrPlayerUpdate(state.getCurrPlayer().getNickname());
    }

    /**
     * Re-establishes player references for all tiles on the board and in the
     * queue by matching the nickname stored in each tile against the
     * deserialized player list.
     *
     * <p>This step is necessary because Jackson deserializes tiles and players
     * independently, leaving tile-to-player object references broken.</p>
     *
     * @param savedPlayers the list of deserialized players from the snapshot
     */
    private void reconnectTilePlayers(List<Player> savedPlayers) {
        state.getBoard().forEach(tile -> reconnectTile(tile, savedPlayers));
        state.getQueue().forEach(tile -> reconnectTile(tile, savedPlayers));
    }

    /**
     * Re-establishes the player reference for a single tile, if it is occupied.
     *
     * @param tile         the tile whose player reference should be restored
     * @param savedPlayers the list of deserialized players to search in
     */
    private void reconnectTile(Tile tile, List<Player> savedPlayers) {
        String nickname = tile.getPlayerNickname();
        if (nickname == null || nickname.isEmpty()) return;
        savedPlayers.stream()
                .filter(p -> p.getNickname().equals(nickname))
                .findFirst()
                .ifPresent(tile::setPlayer);
    }

    /**
     * Maps a {@link GamePhaseEnum} value to the corresponding
     * {@link GamePhaseState} implementation.
     *
     * @param phase the phase to map; must not be {@code null}
     * @return the matching {@link GamePhaseState}; {@code null} if the phase
     *         is not recognized
     */
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