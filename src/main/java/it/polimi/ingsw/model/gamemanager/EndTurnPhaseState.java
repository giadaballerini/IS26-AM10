package it.polimi.ingsw.model.gamemanager;

import it.polimi.ingsw.enumerations.GamePhaseEnum;

/**
 * Game phase that runs at the end of a single player's turn, returning them
 * to the queue and triggering any queue tile effects.
 *
 * <p>On entry, the current player is moved back to the queue, their queue
 * tile effects are applied, and the updated state is broadcast to all clients.
 * The transition logic then branches into one of three paths:</p>
 * <ul>
 *   <li>If not all players have moved back to the queue yet (queue size is less
 *       than the number of players), the next player takes their turn and the
 *       game transitions to {@link DrawPhaseState}.</li>
 *   <li>If all players are in the queue and any player has pending skippable
 *       draw actions, transitions to {@link OptionalDrawPhaseState}.</li>
 *   <li>Otherwise all players have completed their turn and the game
 *       transitions to {@link EndRoundPhaseState}.</li>
 * </ul>
 */
class EndTurnPhaseState implements GamePhaseState {

    /**
     * Determines the next phase based on how many players have moved onto the
     * board and whether any skippable draws are pending.
     *
     * @param context the {@link GameManager} driving the state machine
     * @return {@link DrawPhaseState} if not all players have moved back to the queue;
     *         {@link OptionalDrawPhaseState} if skippable draws are pending;
     *         {@link EndRoundPhaseState} otherwise
     */
    @Override
    public GamePhaseState nextPhase(GameManager context) {
        if (context.getQueueSize() < context.getNumPlayers()) {
            context.nextPlayer();
            return new DrawPhaseState();
        }
        if (context.hasAnySkippableDraws()) {
            return new OptionalDrawPhaseState();
        }
        context.nextPlayer();
        return new EndRoundPhaseState();
    }

    /**
     * Executes entry logic for this phase: returns the current player to the
     * queue, applies queue tile effects, and notifies all clients.
     *
     * @param context the {@link GameManager} driving the state machine
     */
    public void onEntry(GameManager context) {
        context.execEndTurn();
    }

    /**
     * Returns the {@link GamePhaseEnum} constant identifying this phase.
     *
     * @return {@link GamePhaseEnum#END_TURN}
     */
    @Override
    public GamePhaseEnum getPhase() { return GamePhaseEnum.END_TURN; }
}