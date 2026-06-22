package it.polimi.ingsw.exceptions;

/**
 * Exception thrown when an invalid or unrecognized tile is selected to move into.
 */
public class InvalidMoveException extends GameException {

    /**
     * Creates a new {@code InvalidMoveException} with the given detail message.
     *
     * @param message the detail message describing the cause of the exception
     */
    public InvalidMoveException(String message) {
        super(message);
    }
}
