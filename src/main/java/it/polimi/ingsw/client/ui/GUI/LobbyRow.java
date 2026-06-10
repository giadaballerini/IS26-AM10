package it.polimi.ingsw.client.ui.GUI;

/**
 * Data model for a single row in the lobby list table displayed in the main menu.
 * Each instance represents an available game lobby that the player can join,
 * and exposes the columns shown in the {@link javafx.scene.control.TableView}:
 * lobby ID, current and maximum player count, and the lobby owner's nickname.
 */
public class LobbyRow {

    /** The unique identifier of the lobby. */
    private final int id;

    /** The number of players currently in the lobby. */
    private final int giocatoriAttuali;

    /** The maximum number of players allowed in the lobby. */
    private final int maxGiocatori;

    /** The nickname of the player who created the lobby. */
    private final String proprietario;

    /**
     * Creates a new lobby row with the given lobby details.
     *
     * @param id              the unique identifier of the lobby
     * @param giocatoriAttuali the number of players currently in the lobby
     * @param maxGiocatori    the maximum number of players allowed in the lobby
     * @param proprietario    the nickname of the player who created the lobby
     */
    public LobbyRow(int id, int giocatoriAttuali, int maxGiocatori, String proprietario) {
        this.id = id;
        this.giocatoriAttuali = giocatoriAttuali;
        this.proprietario = proprietario;
        this.maxGiocatori = maxGiocatori;
    }

    /** @return the unique identifier of the lobby */
    public int getId() {
        return id;
    }

    /**
     * @return a formatted string showing the current and maximum player count
     *         (e.g. {@code "2/4"})
     */
    public String getGiocatori() {
        return giocatoriAttuali + "/" + maxGiocatori;
    }

    /** @return the nickname of the player who created the lobby */
    public String getProprietario() {
        return proprietario;
    }
}