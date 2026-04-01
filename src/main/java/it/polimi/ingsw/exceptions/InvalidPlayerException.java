package it.polimi.ingsw.exceptions;

public class InvalidPlayerException extends RuntimeException {
    public InvalidPlayerException(String message) {
        super(message);
    }
}
