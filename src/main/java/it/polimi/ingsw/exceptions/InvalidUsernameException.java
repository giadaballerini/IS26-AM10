package it.polimi.ingsw.exceptions;

/**
 * Exception thrown when a player tries to register an invalid username:
 * either already in use or blank/null.
 */
public class InvalidUsernameException extends Exception {

    /**
     * Creates a new {@code InvalidUsernameException} with the given detail message.
     *
     * @param message the detail message describing the cause of the exception
     */
    public InvalidUsernameException(String message) {
        super(message);
    }
}