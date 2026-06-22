package it.polimi.ingsw.client.command;

import it.polimi.ingsw.client.Client;

/**
 * Command that scrolls the players panel in the TUI by a fixed number of rows.
 *
 * <p>Mapped to the {@code pgdn} (scroll down) and {@code pgup} (scroll up)
 * TUI commands. The scroll amount is determined at construction time by
 * {@link it.polimi.ingsw.client.ui.tui.utility.CommandParser}.</p>
 */
public class ScrollPlayersCommand implements Command {

    /** Number of rows to scroll; positive scrolls down, negative scrolls up. */
    private final int delta;

    /**
     * Constructs a new {@code ScrollPlayersCommand}.
     *
     * @param delta rows to scroll (positive = down, negative = up)
     */
    public ScrollPlayersCommand(int delta) {
        this.delta = delta;
    }

    /**
     * Scrolls the players panel by {@link #delta} rows.
     *
     * @param client the client instance used to forward the scroll request to the UI
     */
    @Override
    public void execute(Client client) {
        client.scrollPlayersPanel(delta);
    }

    /**
     * Indicates that this command should not clear the log area.
     *
     * @return {@code false}
     */
    @Override
    public boolean shouldClearLogs() { return false; }
}