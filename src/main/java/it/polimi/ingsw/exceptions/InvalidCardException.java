package it.polimi.ingsw.exceptions;

/**
 * Exception thrown when an invalid or unrecognized card is used during the game.
 */
public class InvalidCardException extends GameException {

    /**
     * Creates a new {@code InvalidCardException} with the given detail message.
     *
     * @param message the detail message describing the cause of the exception
     */
    public InvalidCardException(String message) {
        super(message);
    }
}
