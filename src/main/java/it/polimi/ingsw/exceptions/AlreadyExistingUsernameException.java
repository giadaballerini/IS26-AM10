package it.polimi.ingsw.exceptions;

public class AlreadyExistingUsernameException extends RuntimeException {
    public AlreadyExistingUsernameException(String message) {
        super(message);
    }
}
