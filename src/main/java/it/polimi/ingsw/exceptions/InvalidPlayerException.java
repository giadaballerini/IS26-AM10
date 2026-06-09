package it.polimi.ingsw.exceptions;

/**
 * Exception thrown when a player tries to do an action not during his turn.
 */
public class InvalidPlayerException extends RuntimeException {

    /**
     * Creates a new {@code InvalidPlayerException} with the given detail message.
     *
     * @param message the detail message describing the cause of the exception
     */
    public InvalidPlayerException(String message) {
        super(message);
    }
}
