package it.polimi.ingsw.network.dto;

import it.polimi.ingsw.enumerations.GamePhaseEnum;

import java.io.Serial;
import java.io.Serializable;

/**
 * Carries the current phase of the game.
 *
 * <p>Sent by the server whenever the game transitions to a new phase so that
 * clients can update their UI accordingly.
 */
public class PhaseDTO implements Serializable {
    /**
     * Required by the {@link Serializable} interface.
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /** The game phase this DTO represents. */
    private final GamePhaseEnum gamePhase;

    /**
     * Creates a {@code PhaseDTO} wrapping the given game phase.
     *
     * @param gamePhase the current game phase
     */
    public PhaseDTO(GamePhaseEnum gamePhase) {
        this.gamePhase = gamePhase;
    }

    /**
     * Returns the game phase carried by this DTO.
     *
     * @return current {@link GamePhaseEnum} value
     */
    public GamePhaseEnum getPhase() {
        return gamePhase;
    }
}