package it.polimi.ingsw.exceptions;

/**
 * Exception thrown when an invalid or unrecognized draw action is used during the game.
 */
public class InvalidDrawException extends RuntimeException {

    /**
     * Creates a new {@code InvalidDrawException} with the given detail message.
     *
     * @param message the detail message describing the cause of the exception
     */
    public InvalidDrawException(String message) {
        super(message);
    }
}
