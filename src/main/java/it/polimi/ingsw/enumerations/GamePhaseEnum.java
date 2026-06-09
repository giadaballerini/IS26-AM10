package it.polimi.ingsw.enumerations;

/**
 * Represents the possible phases of a game turn or round.
 *
 * <p>The game progresses through these phases in sequence,
 * controlling which actions are available to players at any given time.</p>
 */
public enum GamePhaseEnum {

    /** The initial setup phase where players move their pawns. */
    SETUP_PHASE,

    /** The phase in which players draw cards. */
    DRAW_PHASE,

    /** An optional additional draw phase. */
    OPTIONAL_DRAW_PHASE,

    /** The phase marking the end of a player's turn. */
    END_TURN,

    /** The phase marking the end of a round. */
    END_ROUND,

    /** The phase in which the last two events are played. */
    PLAY_EVENT,

    /** The phase marking the end of the game. */
    END_GAME,

    /** A placeholder representing no active phase. */
    NONE
}
