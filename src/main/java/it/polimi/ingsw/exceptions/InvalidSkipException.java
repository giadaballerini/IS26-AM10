package it.polimi.ingsw.exceptions;

/**
 * Exception thrown when an invalid skip is requested during the game.
 */
public class InvalidSkipException extends GameException {

    /**
     * Creates a new {@code InvalidSkipException} with the given detail message.
     *
     * @param message the detail message describing the cause of the exception
     */
    public InvalidSkipException(String message) {
        super(message);
    }
}
