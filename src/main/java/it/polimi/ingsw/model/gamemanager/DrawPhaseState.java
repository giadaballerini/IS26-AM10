package it.polimi.ingsw.model.gamemanager;

import it.polimi.ingsw.enumerations.GamePhaseEnum;

/**
 * Game phase in which players resolve card draw actions triggered by board
 * tile effects.
 *
 * <p>On entry, the next player is advanced, board tile effects are evaluated,
 * and drawable actions are computed. Players standing on a tile with an
 * interactive effect must resolve a draw; all other players receive their
 * tile's instant effect instead. The phase loops until all pending draw
 * actions have been resolved, then transitions to
 * {@link EndTurnPhaseState}.</p>
 */
class DrawPhaseState implements GamePhaseState {

    /**
     * Transitions to {@link EndTurnPhaseState} once all pending draw actions
     * have been resolved, or stays in this phase if any actions remain.
     * The skippable-draw flag is cleared before leaving the phase.
     *
     * @param context the {@link GameManager} driving the state machine
     * @return {@link EndTurnPhaseState} if no actions are pending;
     *         {@code this} otherwise
     */
    public GamePhaseState nextPhase(GameManager context) {
        if (context.getToDoActions().isEmpty()) {
            context.setSkippableDraw(false);
            return new EndTurnPhaseState();
        } else {
            return this;
        }
    }

    /**
     * Returns the {@link GamePhaseEnum} constant identifying this phase.
     *
     * @return {@link GamePhaseEnum#DRAW_PHASE}
     */
    @Override
    public GamePhaseEnum getPhase() {
        return GamePhaseEnum.DRAW_PHASE;
    }

    /**
     * Executes entry logic for this phase: advances to the next player,
     * evaluates board tile effects, and determines which draw actions are
     * available.
     *
     * @param context the {@link GameManager} driving the state machine
     */
    public void onEntry(GameManager context) {
        context.nextPlayer();
        context.checkBoardTileEffects();
        context.checkCanDraw();
    }
    /**
     * Creates a new {@code DrawPhaseState} instance.
     */
    DrawPhaseState() {
    }
}