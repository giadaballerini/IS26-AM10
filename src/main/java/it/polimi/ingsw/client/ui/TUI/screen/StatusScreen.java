package it.polimi.ingsw.client.ui.TUI.screen;

import it.polimi.ingsw.client.ui.TUI.utility.TUIColorMapper;
import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.network.dto.PlayerDTO;
import it.polimi.ingsw.network.dto.PlayerStatusDTO;
import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.jline.utils.InfoCmp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * TUI screen that displays the active status flags of all players in the current match.
 * For each player, it shows their nickname in their pawn color and the list of currently
 * active flags (such as hunt bonus, protection, discounts, and double shaman income),
 * or a "no active flags" message if none are set. The screen blocks until the player
 * presses Backspace to return to the previous view.
 */
public class StatusScreen {

    private final Terminal terminal;

    /** The current status of every player in the match, including their active flags. */
    private final List<PlayerStatusDTO> statuses;

    /** The list of all players in the match, used to resolve pawn colors for name styling. */
    private final List<PlayerDTO> players;

    /** Width of the terminal in characters, read at render time. */
    private int screenW;

    /** Height of the terminal in lines, read at render time. */
    private int screenH;

    /**
     * Character buffer holding the text content of each cell before it is flushed
     * to the terminal.
     */
    private char[][] screenBuffer;

    /**
     * Style buffer holding the ANSI style of each cell, applied when the buffers
     * are flushed to the terminal.
     */
    private AttributedStyle[][] colorBuffer;

    /**
     * The full list of flag entries to check for each player, in the order they are
     * displayed on the status screen.
     */
    private static final List<FlagEntry> FLAGS = List.of(
            new FlagEntry("Hunt",             PlayerStatusDTO::isHuntBonus),
            new FlagEntry("Paint",            PlayerStatusDTO::isPaintFlag),
            new FlagEntry("Extra pescata",    PlayerStatusDTO::isExtraFlag),
            new FlagEntry("Protezione",       PlayerStatusDTO::hasProtection),
            new FlagEntry("Sconto Painter",   s -> s.hasDiscountFor(CardTypeEnum.PAINTER)),
            new FlagEntry("Sconto Crafter",   s -> s.hasDiscountFor(CardTypeEnum.CRAFTER)),
            new FlagEntry("Sconto Gatherer",  s -> s.hasDiscountFor(CardTypeEnum.GATHERER)),
            new FlagEntry("Double Shaman",    PlayerStatusDTO::hasDoubleShamanIncome)
    );

    /**
     * Creates a new status screen.
     *
     * @param terminal the JLine terminal to render on
     * @param statuses the current status of every player in the match
     * @param players  the list of all players in the match, used to resolve pawn colors
     */
    public StatusScreen(Terminal terminal, List<PlayerStatusDTO> statuses, List<PlayerDTO> players) {
        this.terminal = terminal;
        this.statuses = statuses;
        this.players  = players;
    }

    /**
     * Renders the status screen and blocks until the player presses Backspace.
     * Saves and restores the terminal state and attributes around the display.
     *
     * @throws IOException if a terminal I/O error occurs
     */
    public void display() throws IOException {
        Attributes savedAttributes = terminal.getAttributes();
        terminal.puts(InfoCmp.Capability.cursor_invisible);
        terminal.flush();
        try {
            render();
            waitForBackspace();
        } finally {
            terminal.puts(InfoCmp.Capability.cursor_visible);
            terminal.writer().print("\033[0m");
            terminal.writer().flush();
            terminal.setAttributes(savedAttributes);
        }
    }

    /**
     * Initializes the screen and color buffers to match the current terminal dimensions,
     * writes the title and the status entry for each player into them, then flushes
     * the result to the terminal. For each player, active flags are listed in green,
     * separated by vertical bars; if none are active, a placeholder message is shown instead.
     */
    private void render() {
        screenW = terminal.getWidth();
        screenH = terminal.getHeight();

        screenBuffer = new char[screenH][screenW];
        colorBuffer  = new AttributedStyle[screenH][screenW];
        for (int i = 0; i < screenH; i++) {
            for (int j = 0; j < screenW; j++) {
                screenBuffer[i][j] = ' ';
                colorBuffer[i][j]  = AttributedStyle.DEFAULT;
            }
        }

        String title = "=== STATO DEI GIOCATORI ===";
        printAt(2, (screenW - title.length()) / 2, title,
                AttributedStyle.DEFAULT.foreground(AttributedStyle.BRIGHT));
        printAt(3, 0, "─".repeat(screenW), AttributedStyle.DEFAULT);

        int row = 5;
        for (PlayerStatusDTO status : statuses) {
            if (row >= screenH - 4) break;

            AttributedStyle nameStyle = players.stream()
                    .filter(p -> p.getNickname().equals(status.getNickname()))
                    .findFirst()
                    .map(p -> TUIColorMapper.getPlayerJlineColor(p.getColor()).bold())
                    .orElse(AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW).bold());
            printAt(row, 4, status.getNickname(), nameStyle);
            row++;

            List<String> active = new ArrayList<>();
            for (FlagEntry f : FLAGS) {
                if (f.test(status)) active.add(f.label);
            }

            if (active.isEmpty()) {
                printAt(row, 6, "Nessun flag attivo",
                        AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE));
            } else {
                printAt(row, 6, String.join("  |  ", active),
                        AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN));
            }
            row += 2;
        }

        String footer = "Premi BACKSPACE per tornare indietro...";
        printAt(screenH - 2, (screenW - footer.length()) / 2, footer,
                AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN));

        flushBuffers();
    }

    /**
     * Blocks the current thread until the player presses Backspace (ASCII 8 or 127).
     * Any other key press is ignored.
     */
    private void waitForBackspace() {
        try {
            while (true) {
                int c = terminal.reader().read();
                if (c == 8 || c == 127) return;
            }
        } catch (Exception ignored) {}
    }

    /**
     * Writes the given text into the screen and color buffers at the specified position,
     * clipping any characters that fall outside the terminal width.
     *
     * @param row   the zero-based row index to write at
     * @param col   the zero-based column index to start writing from
     * @param text  the text to write; ignored if {@code null}
     * @param style the ANSI style to apply to the written characters
     */
    private void printAt(int row, int col, String text, AttributedStyle style) {
        if (row < 0 || row >= screenH || col < 0 || text == null) return;
        for (int i = 0; i < text.length(); i++) {
            int c = col + i;
            if (c >= screenW) break;
            screenBuffer[row][c] = text.charAt(i);
            colorBuffer[row][c]  = style;
        }
    }

    /**
     * Flushes the screen and color buffers to the terminal.
     * Clears any remaining content below the rendered area.
     */
    private void flushBuffers() {
        terminal.puts(InfoCmp.Capability.cursor_home);
        AttributedStringBuilder asb = new AttributedStringBuilder();
        for (int i = 0; i < screenH; i++) {
            for (int j = 0; j < screenW - 1; j++) {
                asb.style(colorBuffer[i][j]);
                asb.append(screenBuffer[i][j]);
            }
            if (i < screenH - 1) asb.append("\n");
        }
        asb.style(AttributedStyle.DEFAULT);
        terminal.writer().print(asb.toAnsi());
        terminal.puts(InfoCmp.Capability.clr_eos);
        terminal.writer().flush();
    }

    /**
     * Functional interface for reading a single boolean flag from a {@link PlayerStatusDTO}.
     */
    @FunctionalInterface
    private interface FlagReader {

        /**
         * Tests the given player status for a specific flag condition.
         *
         * @param s the player status to test
         * @return {@code true} if the flag is active, {@code false} otherwise
         */
        boolean test(PlayerStatusDTO s);
    }

    /**
     * Associates a display label with a {@link FlagReader} that checks whether
     * the corresponding flag is active for a given player.
     */
    private static class FlagEntry {

        /** The label displayed on the status screen when this flag is active. */
        final String label;

        /** The reader used to check whether this flag is active for a given player status. */
        final FlagReader reader;

        /**
         * Creates a new flag entry.
         *
         * @param label  the display label for this flag
         * @param reader the function used to test whether this flag is active
         */
        FlagEntry(String label, FlagReader reader) {
            this.label  = label;
            this.reader = reader;
        }

        /**
         * Tests whether this flag is active for the given player status.
         *
         * @param s the player status to test
         * @return {@code true} if this flag is active, {@code false} otherwise
         */
        boolean test(PlayerStatusDTO s) {
            return reader.test(s);
        }
    }
}