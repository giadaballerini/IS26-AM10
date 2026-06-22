package it.polimi.ingsw.network.dto;

import java.io.Serializable;

/**
 * Carries the set of actions still available to the active player for the
 * current turn.
 *
 * <p>Sent by the server whenever the player's action count changes.
 */
public class ActionsDTO implements Serializable {

    /** Number of draws the player may still take from the upper row. */
    int upDraws;

    /** Number of draws the player may still take from the lower row. */
    int downDraws;

    /** Whether the player is allowed to skip the remaining actions. */
    boolean skippable;

    /**
     * Creates an {@code ActionsDTO} with the given action count.
     *
     * @param upDraws   draws available from the upper row
     * @param downDraws draws available from the lower row
     * @param skippable {@code true} if the player may skip remaining actions
     */
    public ActionsDTO(int upDraws, int downDraws, boolean skippable) {
        this.upDraws = upDraws;
        this.downDraws = downDraws;
        this.skippable = skippable;
    }

    /**
     * Copy constructor.
     *
     * @param a the instance to copy
     */
    public ActionsDTO(ActionsDTO a) {
        this.upDraws = a.upDraws;
        this.downDraws = a.downDraws;
        this.skippable = a.skippable;
    }

    /**
     * Returns the number of draws still available from the upper row.
     *
     * @return upper-row draw count
     */
    public int getUpDraws() {
        return upDraws;
    }

    /**
     * Returns the number of draws still available from the lower row.
     *
     * @return lower-row draw count
     */
    public int getDownDraws() {
        return downDraws;
    }

    /**
     * Returns whether the player may skip the remaining actions for this turn.
     *
     * @return {@code true} if skipping is allowed
     */
    public boolean isSkippable() {
        return skippable;
    }
}