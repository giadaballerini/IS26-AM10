package it.polimi.ingsw.model.gamemanager;

import it.polimi.ingsw.enumerations.GamePhaseEnum;

/**
 * Game phase that runs at the end of each round, after all players have
 * resolved their draw actions.
 *
 * <p>On entry, the current round's event cards are applied. The transition
 * logic then branches into one of three paths:</p>
 * <ul>
 *   <li>If any player has pending skippable draw actions, transitions to
 *       {@link OptionalDrawPhaseState} so those actions can be resolved.</li>
 *   <li>If the current turn is 10 (the final turn of an age), transitions to
 *       {@link PlayEventPhaseState} to resolve the age-end event.</li>
 *   <li>Otherwise, the board is refilled, the turn counter is incremented,
 *       and the game returns to {@link SetupPhaseState} for the next round.</li>
 * </ul>
 */
class EndRoundPhaseState implements GamePhaseState {

    /**
     * Determines the next phase based on pending skippable draws and the
     * current turn number.
     *
     * @param context the {@link GameManager} driving the state machine
     * @return {@link OptionalDrawPhaseState} if skippable draws are pending;
     *         {@link PlayEventPhaseState} if the age has ended (turn 10);
     *         {@link SetupPhaseState} otherwise
     */
    @Override
    public GamePhaseState nextPhase(GameManager context) {
        if (context.hasAnySkippableDraws()) {
            return new OptionalDrawPhaseState();
        }
        if (context.getCurrTurn() == 10) {
            return new PlayEventPhaseState();
        } else {
            context.refillBoard();
            context.incrementTurn();
            return new SetupPhaseState();
        }
    }

    /**
     * Executes entry logic for this phase: applies the event cards for the
     * current round and advances to the next phase.
     *
     * @param context the {@link GameManager} driving the state machine
     */
    public void onEntry(GameManager context) {
        context.playEvent();
    }

    /**
     * Returns the {@link GamePhaseEnum} constant identifying this phase.
     *
     * @return {@link GamePhaseEnum#END_ROUND}
     */
    @Override
    public GamePhaseEnum getPhase() { return GamePhaseEnum.END_ROUND; }
}