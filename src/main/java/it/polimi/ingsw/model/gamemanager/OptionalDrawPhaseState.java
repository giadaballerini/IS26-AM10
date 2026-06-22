package it.polimi.ingsw.model.gamemanager;

import it.polimi.ingsw.enumerations.GamePhaseEnum;

/**
 * Game phase in which players resolve their pending skippable draw actions.
 *
 * <p>Skippable draws are optional card draws granted by certain card effects
 * that a player may choose to take or forgo. On entry, the pending skippable
 * draws are loaded from the players' queues and the current player is notified
 * of the available actions. The phase loops until all pending actions have
 * been resolved or skipped, then advances the turn and transitions to
 * {@link EndRoundPhaseState}.</p>
 */
public class OptionalDrawPhaseState implements GamePhaseState {

    /**
     * Transitions to {@link EndRoundPhaseState} once all pending skippable
     * draw actions have been resolved, or stays in this phase if any remain.
     * The skippable-draw flag is cleared and the turn is advanced before leaving.
     *
     * @param context the {@link GameManager} driving the state machine
     * @return {@link EndRoundPhaseState} if no actions are pending;
     *         {@code this} otherwise
     */
    public GamePhaseState nextPhase(GameManager context) {
        if (context.getToDoActions().isEmpty()) {
            context.setSkippableDraw(false);
            context.nextPlayer();
            return new EndRoundPhaseState();
        }
        return this;
    }

    /**
     * Executes entry logic for this phase: loads each player's pending
     * skippable draws into the action queue and notifies the current player
     * of their available draw options.
     *
     * @param context the {@link GameManager} driving the state machine
     */
    @Override
    public void onEntry(GameManager context) {
        context.loadSkippableDraws();
    }

    /**
     * Returns the {@link GamePhaseEnum} constant identifying this phase.
     *
     * @return {@link GamePhaseEnum#OPTIONAL_DRAW_PHASE}
     */
    @Override
    public GamePhaseEnum getPhase() {
        return GamePhaseEnum.OPTIONAL_DRAW_PHASE;
    }
    /**
     * Creates a new {@code OptionalDrawPhaseState} instance.
     */
    public OptionalDrawPhaseState() {
    }
}