package it.polimi.ingsw.model.interfaces;

import it.polimi.ingsw.persistency.GameSnapshot;

/**
 * Marks a class as capable of serializing its current state into a
 * {@link GameSnapshot}.
 *
 * <p>Implemented by {@link it.polimi.ingsw.model.gamemanager.GameManager},
 * which is the entry point for capturing the full game state when the server
 * needs to persist a match.</p>
 */
public interface Snapshotable {

    /**
     * Captures the current state of this object into a {@link GameSnapshot}.
     *
     * @param matchId the unique identifier of the match being snapshotted
     * @return a snapshot representing the current game state; never {@code null}
     */
    GameSnapshot toSnapshot(int matchId);
}