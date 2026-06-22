package it.polimi.ingsw.exceptions;

/**
 * Exception thrown when an action is performed during an invalid game phase.
 */
public class InvalidPhaseException extends GameException {

    /**
     * Creates a new {@code InvalidPhaseException} with the given detail message.
     *
     * @param message the detail message describing the cause of the exception
     */
    public InvalidPhaseException(String message) {
        super(message);
    }
}
