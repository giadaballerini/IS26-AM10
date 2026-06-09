package it.polimi.ingsw.exceptions;

/**
 * Exception thrown when an invalid lobby size is specified during lobby creation.
 */
public class InvalidLobbySizeException extends RuntimeException {

    /**
     * Creates a new {@code InvalidLobbySizeException} with the given detail message.
     *
     * @param message the detail message describing the cause of the exception
     */
    public InvalidLobbySizeException(String message) {
        super(message);
    }
}
