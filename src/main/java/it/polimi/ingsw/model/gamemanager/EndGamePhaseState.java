package it.polimi.ingsw.model.gamemanager;

import it.polimi.ingsw.enumerations.GamePhaseEnum;

/**
 * Terminal game phase in which final scores are calculated and the match
 * winner is determined.
 *
 * <p>On entry, the final score count is performed and the registered callback
 * is invoked to notify the rest of the system that the match has ended.
 * This phase has no successor: {@link #nextPhase(GameManager)} always returns
 * {@code this}.</p>
 */
class EndGamePhaseState implements GamePhaseState {

    /**
     * Returns {@code this}, as the end-game phase has no successor.
     *
     * @param context the {@link GameManager} driving the state machine
     * @return {@code this}
     */
    @Override
    public GamePhaseState nextPhase(GameManager context) { return this; }

    /**
     * Executes entry logic for this phase: computes final scores and fires
     * the end-of-game callback to notify the rest of the system that the
     * match has ended.
     *
     * @param context the {@link GameManager} driving the state machine
     */
    public void onEntry(GameManager context) {
        context.finalScoreCount();
        if (context.getOnGameEndedCallback() != null) {
            context.getOnGameEndedCallback().run();
        }
    }

    /**
     * Returns the {@link GamePhaseEnum} constant identifying this phase.
     *
     * @return {@link GamePhaseEnum#END_GAME}
     */
    @Override
    public GamePhaseEnum getPhase() { return GamePhaseEnum.END_GAME; }
    /**
     * Creates a new {@code EndGamePhaseState} instance.
     */
    EndGamePhaseState() {
    }
}