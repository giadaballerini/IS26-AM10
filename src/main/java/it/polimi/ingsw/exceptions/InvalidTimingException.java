package it.polimi.ingsw.exceptions;

/**
 * Exception thrown when a command is issued outside its valid context:
 * either a lobby command is issued while a match is already in progress,
 * or a game command is issued before joining a match.
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
