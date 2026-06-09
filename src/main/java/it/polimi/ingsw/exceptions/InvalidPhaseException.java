package it.polimi.ingsw.exceptions;

/**
 * Exception thrown when an action is performed during an invalid game phase,
 * or before joining a game.
 */
public class InvalidPhaseException extends RuntimeException {

    /**
     * Creates a new {@code InvalidPhaseException} with the given detail message.
     *
     * @param message the detail message describing the cause of the exception
     */
    public InvalidPhaseException(String message) {
        super(message);
    }
}
