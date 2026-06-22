package it.polimi.ingsw.model.gamemanager;

import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.network.dto.*;
import it.polimi.ingsw.observer.ModelObserver;

import java.util.List;
import java.util.Map;

/**
 * Broadcasts game events to all registered {@link ModelObserver} instances.
 *
 * <p>Each method converts model objects to their DTO counterparts where needed
 * and forwards the notification to every observer in the listener list.
 * Some methods apply per-observer filtering, sending the notification only
 * to the current player.</p>
 */
public class GameNotifier {

    /** The observers to which all notifications are broadcast. */
    private final List<ModelObserver> listeners;

    /**
     * Constructs a {@code GameNotifier} with the given list of observers.
     *
     * @param listeners the observers to notify; must not be {@code null}
     */
    public GameNotifier(List<ModelObserver> listeners) {
        this.listeners = listeners;
    }

    /**
     * Notifies all observers that the active player has changed.
     *
     * @param currPlayer nickname of the player who is now taking their turn
     */
    public void notifyCurrPlayerUpdate(String currPlayer) {
        for (ModelObserver c : listeners) {
            c.onCurrPlayerUpdate(currPlayer);
        }
    }

    /**
     * Notifies all observers that a player has moved onto a board tile.
     *
     * @param tdto       DTO of the tile the player moved to
     * @param currPlayer nickname of the player who made the move
     */
    public void notifyMoveUpdate(TileDTO tdto, String currPlayer) {
        for (ModelObserver c : listeners) {
            c.onMoveUpdate(tdto, currPlayer);
        }
    }

    /**
     * Notifies all observers that the game phase has changed.
     *
     * @param currPhaseState the new active phase state
     */
    void notifyPhaseUpdate(GamePhaseState currPhaseState) {
        PhaseDTO phaseDTO = new PhaseDTO(currPhaseState.getPhase());
        for (ModelObserver c : listeners) {
            c.onPhaseUpdate(phaseDTO);
        }
    }

    /**
     * Notifies all observers that the match has ended, providing each observer
     * with their personal ranking positions.
     *
     * @param statsList        final statistics for all players
     * @param globalPositions  map from nickname to global ranking position
     * @param rankingPositions map from nickname to in-match finishing position
     */
    void notifyGameEnding(List<PlayerStatsDTO> statsList, Map<String, Integer> globalPositions, Map<String, Integer> rankingPositions) {
        for (ModelObserver listener : listeners) {
            int rankingPos = rankingPositions.getOrDefault(listener.getNickname(), -1);
            int globalPos = globalPositions.getOrDefault(listener.getNickname(), -1);
            listener.onGameEnding(statsList, rankingPos, globalPos);
        }
    }

    /**
     * Notifies all observers that a player has drawn a card.
     *
     * @param currPlayer the player who drew the card
     * @param card       the card that was drawn
     */
    void notifyDrawUpdate(Player currPlayer, Card card) {
        CardDTO cardDTO = card.toDTO();
        for (ModelObserver c : listeners) {
            c.onDrawUpdate(cardDTO, currPlayer.getNickname());
        }
    }

    /**
     * Notifies all observers of an update to the given player's active bonus
     * flags.
     *
     * @param currPlayer the player whose status has changed
     */
    void notifyStatusUpdate(Player currPlayer) {
        PlayerStatusDTO status = currPlayer.toStatusDTO();
        for (ModelObserver c : listeners) {
            c.onStatusUpdate(status);
        }
    }

    /**
     * Notifies all observers of an update to the given player's statistics,
     * triggered by a specific card.
     *
     * @param currPlayer the player whose statistics have changed
     */
    void notifyStatsUpdate(Player currPlayer) {
        PlayerStatsDTO statsDto = currPlayer.toStatsDTO();
        for (ModelObserver c : listeners) {
            c.onStatsUpdate(statsDto);
        }
    }

    /**
     * Notifies all observers that a game event has been triggered.
     *
     * @param events DTO describing the event that occurred
     */
    void notifyEventUpdate(EventDTO events) {
        for (ModelObserver c : listeners) {
            c.onEvent(events);
        }
    }

    /**
     * Sends the current board state to all observers.
     *
     * @param dto DTO representing the full board state
     */
    void showBoard(BoardDTO dto) {
        for (ModelObserver c : listeners) {
            c.showBoard(dto);
        }
    }

    /**
     * Notifies all observers that the given player has skipped their action.
     *
     * @param currPlayer the player who skipped
     */
    void notifySkip(Player currPlayer) {
        for (ModelObserver c : listeners) {
            c.notifySkip(currPlayer.getNickname());
        }
    }

    /**
     * Notifies observers of the draw actions currently available.
     *
     * <p>The current player receives the real {@link ActionsDTO}; all other
     * observers receive an empty actions DTO to signal that no actions are
     * available to them.</p>
     *
     * @param dto        the draw actions available to the current player
     * @param currPlayer nickname of the player who may take a draw action
     */
    void notifyDrawable(ActionsDTO dto, String currPlayer) {
        for (ModelObserver c : listeners) {
            if (c.getNickname().equals(currPlayer))
                c.notifyDrawable(dto);
            else
                c.notifyDrawable(new ActionsDTO(0, 0, false));
        }
    }

    /**
     * Notifies all observers that a player has been returned to the queue,
     * providing the updated tile and player statistics.
     *
     * @param statsDTO updated statistics of the player who was returned to the queue
     * @param t        DTO of the tile the player vacated
     */
    void notifyReturnToQueue(PlayerStatsDTO statsDTO, TileDTO t) {
        for (ModelObserver c : listeners) {
            c.onReturnToQueue(t, statsDTO);
        }
    }

    /**
     * Notifies all observers that the game has advanced to a new age.
     *
     * @param ageDTO DTO carrying the details of the age change
     */
    void notifyChangeAge(ChangeAgeDTO ageDTO) {
        for (ModelObserver c : listeners) {
            c.onChangeAge(ageDTO);
        }
    }
}