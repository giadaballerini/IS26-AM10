package it.polimi.ingsw.model.gamemanager;

import it.polimi.ingsw.enumerations.GamePhaseEnum;

/**
 * Game phase in which the two final event cards are played at the end of the
 * last age, before scores are calculated.
 *
 * <p>On entry, the final events are applied and all clients are notified.
 * This phase has a single unconditional successor: {@link EndGamePhaseState}.</p>
 */
class PlayEventPhaseState implements GamePhaseState {

    /**
     * Always transitions to {@link EndGamePhaseState}.
     *
     * @param context the {@link GameManager} driving the state machine
     * @return a new {@link EndGamePhaseState}
     */
    @Override
    public GamePhaseState nextPhase(GameManager context) {
        return new EndGamePhaseState();
    }

    /**
     * Executes entry logic for this phase: applies the final event cards and
     * advances to the next phase.
     *
     * @param context the {@link GameManager} driving the state machine
     */
    public void onEntry(GameManager context) {
        context.playEvent();
    }

    /**
     * Returns the {@link GamePhaseEnum} constant identifying this phase.
     *
     * @return {@link GamePhaseEnum#PLAY_EVENT}
     */
    @Override
    public GamePhaseEnum getPhase() { return GamePhaseEnum.PLAY_EVENT; }
}