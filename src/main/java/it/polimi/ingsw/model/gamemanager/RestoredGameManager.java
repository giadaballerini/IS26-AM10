package it.polimi.ingsw.model.gamemanager;

import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.observer.ModelObserver;
import it.polimi.ingsw.persistency.GameSnapshot;

import java.util.List;
import java.util.stream.Stream;

/**
 * A {@link GameManager} that restores a previously saved match from a
 * {@link GameSnapshot}.
 *
 * <p>The constructor rebuilds the full game state (deck, board, queue, players,
 * phase, pending actions, etc.) from the snapshot via
 * {@link GameState#GameState(GameSnapshot, List)}, then re-establishes
 * tile-to-player object references that are broken by JSON deserialization
 * via {@link #reconnectTilePlayers(List)}. Once constructed,
 * {@link #resume()} broadcasts the restored state to all connected clients
 * so they can resync their views.</p>
 */
public class RestoredGameManager extends GameManager {
    /**
     * Constructs a {@code RestoredGameManager} from a saved snapshot.
     *
     * <p>The full game state is reconstructed from {@code snapshot} via
     * {@link GameState#GameState(GameSnapshot, List)}, after which
     * tile-to-player object references (broken by JSON deserialization) are
     * repaired by {@link #reconnectTilePlayers(List)}. The current phase is
     * then resolved from {@link GameSnapshot#getCurrentPhase()}.</p>
     *
     * @param snapshot             the snapshot to restore from; must not be {@code null}
     * @param observers            the list of observers for the restored match;
     *                             must not be {@code null}
     * @param onGameEndedCallback  callback invoked when the match ends;
     *                             must not be {@code null}
     */
    public RestoredGameManager(GameSnapshot snapshot, List<ModelObserver> observers, Runnable onGameEndedCallback) {
        super(observers, snapshot.getPlayers(), snapshot.getNumPlayers(), onGameEndedCallback);
        setState(new GameState(snapshot, snapshot.getPlayers()));
        reconnectTilePlayers(snapshot.getPlayers());
        reconnectSkippableDrawOwners(snapshot.getPlayers());
        setCurrPhaseState(phaseStateFrom(snapshot.getCurrentPhase()));
    }



    /**
     * Broadcasts the restored game state to all connected clients so that
     * they can resync their views after reconnecting.
     *
     * <p>Sends the current board state, the active game phase, the
     * current player's nickname and the players' status.</p>
     */
    public void resume() {
        LOG.info("[RestoredGameManager] Ripresa partita dal salvataggio – fase: "
                + getCurrPhase());
        showBoard();
        notifyPhaseUpdate();
        notifyCurrPlayerUpdate();
    }

    /**
     * Re-establishes player references for all tiles on the board and in the
     * queue after deserialization from a {@link GameSnapshot}.
     *
     * <p>Jackson deserializes tiles and players independently, leaving
     * tile-to-player object references broken. This method repairs them by
     * matching the nickname stored in each tile against the deserialized
     * player list.</p>
     *
     * @param savedPlayers the list of deserialized players from the snapshot
     */
    private void reconnectTilePlayers(List<Player> savedPlayers) {
        Stream.concat(getBoard().stream(), getQueue().stream()).forEach(tile -> {
            String nickname = tile.getPlayerNickname();
            if (nickname == null || nickname.isEmpty()) return;
            savedPlayers.stream()
                    .filter(p -> p.getNickname().equals(nickname))
                    .findFirst()
                    .ifPresent(tile::setPlayer);
        });
    }
    /**
     * Re-establishes the {@code owner} reference on every pending skippable
     * draw action of every restored player, after deserialization from a
     * {@link GameSnapshot}.
     *
     * <p>Jackson excludes {@link it.polimi.ingsw.model.action.Action#getOwner()}
     * from serialization to avoid a circular reference back to {@link Player},
     * so every {@code Action} inside a deserialized player's pending skippable
     * draws has a {@code null} owner until this method repairs it.</p>
     *
     * @param savedPlayers the list of deserialized players from the snapshot
     */
    private void reconnectSkippableDrawOwners(List<Player> savedPlayers) {
        savedPlayers.forEach(Player::reconnectSkippableDrawOwners);
    }

    /**
     * Maps a {@link GamePhaseEnum} value to the corresponding
     * {@link GamePhaseState} implementation.
     *
     * @param phase the phase to map; must not be {@code null}
     * @return the matching {@link GamePhaseState}
     * @throws IllegalArgumentException if {@code phase} does not correspond to
     *                                  any known phase state
     */
    private GamePhaseState phaseStateFrom(GamePhaseEnum phase) {
        return switch (phase) {
            case SETUP_PHASE         -> new SetupPhaseState();
            case DRAW_PHASE          -> new DrawPhaseState();
            case OPTIONAL_DRAW_PHASE -> new OptionalDrawPhaseState();
            case END_TURN            -> new EndTurnPhaseState();
            case END_ROUND           -> new EndRoundPhaseState();
            case PLAY_EVENT          -> new PlayEventPhaseState();
            case END_GAME            -> new EndGamePhaseState();
            case NONE                -> throw new IllegalArgumentException("Cannot restore a match from NONE phase");
        };
    }
}