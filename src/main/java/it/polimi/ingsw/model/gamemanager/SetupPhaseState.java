package it.polimi.ingsw.model.gamemanager;

import it.polimi.ingsw.enumerations.GamePhaseEnum;

/**
 * Game phase in which all players move their pawn onto a board tile.
 *
 * <p>On entry, the current board state is broadcast to all clients. Players
 * take turns making their move one at a time: after each move the phase checks
 * whether the queue is empty, meaning that all players have left it and are now on the
 * board. While players remain in the queue, the next player is advanced and
 * the phase loops on itself. Once the queue is empty, the first player to
 * enter {@link DrawPhaseState} is selected via {@code nextPlayer()} inside
 * that state's {@code onEntry}.</p>
 *
 * <p>Note: board tile effects are not triggered during this phase; they are
 * resolved later in {@link DrawPhaseState}.</p>
 */
class SetupPhaseState implements GamePhaseState {

    /**
     * Transitions to {@link DrawPhaseState} once all players have moved onto
     * the board (queue is empty), or advances to the next player and stays in
     * this phase otherwise.
     *
     * @param context the {@link GameManager} driving the state machine
     * @return {@link DrawPhaseState} if the queue is empty;
     *         {@code this} otherwise
     */
    @Override
    public GamePhaseState nextPhase(GameManager context) {
        if (context.isQueueEmpty()) {
            return new DrawPhaseState();
        } else {
            context.nextPlayer();
            return this;
        }
    }

    /**
     * Executes entry logic for this phase: broadcasts the current board state
     * to all clients.
     *
     * @param context the {@link GameManager} driving the state machine
     */
    @Override
    public void onEntry(GameManager context) {
        context.showBoard();
    }

    /**
     * Returns the {@link GamePhaseEnum} constant identifying this phase.
     *
     * @return {@link GamePhaseEnum#SETUP_PHASE}
     */
    @Override
    public GamePhaseEnum getPhase() { return GamePhaseEnum.SETUP_PHASE; }
    /**
     * Creates a new {@code SetupPhaseState} instance.
     */
    SetupPhaseState() {
    }
}