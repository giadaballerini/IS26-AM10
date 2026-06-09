package it.polimi.ingsw.exceptions;

/**
 * Exception thrown when an already occupied tile is selected to move into.
 */
public class OccupiedTileException extends RuntimeException {

    /**
     * Creates a new {@code OccupiedTileException} with the given detail message.
     *
     * @param message the detail message describing the cause of the exception
     */
    public OccupiedTileException(String message) {
        super(message);
    }
}
