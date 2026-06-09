package it.polimi.ingsw.model.gamemanager;

import it.polimi.ingsw.enumerations.GamePhaseEnum;

/**
 * Represents a single state in the game phase state machine.
 *
 * <p>Each concrete implementation encapsulates the entry logic and transition
 * rules for one game phase. The {@link GameManager} holds the current state
 * and delegates phase progression to it via {@link #nextPhase(GameManager)}.</p>
 */
interface GamePhaseState {

    /**
     * Computes and returns the next state to transition to, given the current
     * context of the game.
     *
     * @param context the {@link GameManager} driving the state machine
     * @return the next {@link GamePhaseState}; never {@code null}
     */
    GamePhaseState nextPhase(GameManager context);

    /**
     * Returns the {@link GamePhaseEnum} constant identifying this phase.
     *
     * @return the phase enum value for this state; never {@code null}
     */
    GamePhaseEnum getPhase();

    /**
     * Executes any logic that should run when this phase is entered.
     * The default implementation is a no-op.
     *
     * @param context the {@link GameManager} driving the state machine
     */
    default void onEntry(GameManager context) {}
}