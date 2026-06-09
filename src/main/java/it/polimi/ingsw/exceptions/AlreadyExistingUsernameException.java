package it.polimi.ingsw.exceptions;

/**
 * Exception thrown when a player tries to register a username that is already in use.
 */
public class AlreadyExistingUsernameException extends RuntimeException {

    /**
     * Creates a new {@code AlreadyExistingUsernameException} with the given detail message.
     *
     * @param message the detail message describing the cause of the exception
     */
    public AlreadyExistingUsernameException(String message) {
        super(message);
    }
}