package it.polimi.ingsw.exceptions;

public class OccupiedTileException extends RuntimeException {
    public OccupiedTileException(String message) {
        super(message);
    }
}
