package it.polimi.ingsw.exceptions;

/**
 * Exception thrown when an invalid or unrecognized lobby is selected.
 */
public class InvalidLobbyException extends RuntimeException {

    /**
     * Creates a new {@code InvalidLobbyException} with the given detail message.
     *
     * @param message the detail message describing the cause of the exception
     */
    public InvalidLobbyException(String message) {
        super(message);
    }
}
