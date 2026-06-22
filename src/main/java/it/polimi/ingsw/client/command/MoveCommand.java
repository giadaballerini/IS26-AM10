package it.polimi.ingsw.client.command;

import it.polimi.ingsw.client.Client;
/**
 * Command that requests the player's token to be placed on a specific tile of the board.
 * This command is only valid during the setup phase, when each player chooses
 * a tile to place their token on. The position is provided as a 1-based index
 * by the user and converted internally to a 0-based tile ID before being sent to the server.
 */
public class MoveCommand implements Command {
    /**
     * The 1-based position of the target tile as entered by the user.
     */
    private final int tilePos;

    /**
     * Creates a command to place the player's totem on the specified board tile.
     *
     * @param tilePos the 1-based position of the target tile as entered by the user
     */
    public MoveCommand(int tilePos) {
        this.tilePos = tilePos;
    }
    /**
     * Executes the command by requesting the server to place the client's totem
     * on the tile at position {@code tilePos - 1} (converted from 1-based user input
     * to 0-based tile ID).
     *
     * @param client the client instance representing the player who wants to move
     */
    public void execute(Client client) {
        client.move(tilePos - 1);
    }
}
