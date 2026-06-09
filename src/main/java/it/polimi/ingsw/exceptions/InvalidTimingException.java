package it.polimi.ingsw.exceptions;

/**
 * Exception thrown when an action is performed at an invalid time during the game.
 */
public class InvalidTimingException extends RuntimeException {

    /**
     * Creates a new {@code InvalidTimingException} with the given detail message.
     *
     * @param message the detail message describing the cause of the exception
     */
    public InvalidTimingException(String message) {
        super(message);
    }
}
