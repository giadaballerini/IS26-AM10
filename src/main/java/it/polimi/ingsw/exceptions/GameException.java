package it.polimi.ingsw.exceptions;

/**
 * General-purpose exception thrown when an unexpected error occurs during the game.
 */
public class GameException extends RuntimeException {

    /**
     * Creates a new {@code GameException} with the given detail message.
     *
     * @param message the detail message describing the cause of the exception
     */
    public GameException(String message) {
        super(message);
    }
}