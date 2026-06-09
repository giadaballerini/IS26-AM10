package it.polimi.ingsw.exceptions;

/**
 * Exception thrown when an incorrect password is provided during authentication.
 */
public class WrongPasswordException extends RuntimeException {

    /**
     * Creates a new {@code WrongPasswordException} with the given detail message.
     *
     * @param message the detail message describing the cause of the exception
     */
    public WrongPasswordException(String message) {
        super(message);
    }
}
