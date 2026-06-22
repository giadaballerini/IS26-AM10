package it.polimi.ingsw.client.ui.tui.screen;

import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.jline.utils.InfoCmp;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * TUI screen that displays the global leaderboard for matches played with a specific
 * number of players. Players are ranked by their cumulative Points in descending
 * order, with ties broken alphabetically by nickname. The top three positions are
 * highlighted with medal colors and emoji. The screen blocks until the player
 * presses Backspace to return to the previous view.
 */
public class RankingScreen {

    /** Number of blank lines before the title. */
    private static final int TOP_PADDING      = 2;

    /** Blank lines between the separator and the table. */
    private static final int GAP_AFTER_SEP    = 1;

    /** Blank lines between the section header and the first table row. */
    private static final int GAP_BEFORE_TABLE = 1;

    /** Distance from the bottom of the screen reserved for the footer prompt. */
    private static final int FOOTER_OFFSET    = 2;

    /** Left margin for the ranking table content. */
    private static final int LEFT_MARGIN      = 4;

    /** ASCII code for the Backspace key. */
    private static final int BACKSPACE        = 8;

    /** Alternative ASCII code for the Backspace key (Delete on some terminals). */
    private static final int BACKSPACE_ALT    = 127;

    /**
     * The JLine terminal to render on.
     */
    private final Terminal terminal;

    /**
     * The ranking entries sorted by  Points descending, then by nickname ascending,
     * ready to be rendered row by row.
     */
    private final List<Map.Entry<String, Integer>> entries;

    /** The number of players per match for which this ranking applies. */
    private final int numPlayers;

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
     * Creates a new ranking screen.
     *
     * @param terminal   the JLine terminal to render on
     * @param ranks      a map from each player's nickname to their cumulative Prestige Points
     * @param numPlayers the number of players per match for which the ranking applies,
     *                   shown in the screen title
     */
    public RankingScreen(Terminal terminal, Map<String, Integer> ranks, int numPlayers) {
        this.terminal = terminal;
        this.numPlayers = numPlayers;
        this.entries = ranks.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .toList();
    }

    /**
     * Renders the ranking screen and blocks until the player presses Backspace.
     * Saves and restores the terminal state and attributes around the display.
     *
     * @throws IOException if a terminal I/O error occurs
     */
    public void display() throws IOException {
        Attributes attributes = terminal.getAttributes();
        saveTerminalState();
        try {
            renderScreen();
            waitForBackspace();
        } finally {
            restoreTerminalState();
            terminal.setAttributes(attributes);
        }
    }

    /**
     * Saves the terminal state by hiding the cursor.
     */
    private void saveTerminalState() {
        terminal.puts(InfoCmp.Capability.cursor_invisible);
        terminal.flush();
    }

    /**
     * Restores the terminal state by making the cursor visible and resetting ANSI styles.
     */
    private void restoreTerminalState() {
        terminal.puts(InfoCmp.Capability.cursor_visible);
        terminal.writer().print("\033[0m");
        terminal.writer().flush();
    }

    /**
     * Initializes the screen and color buffers to match the current terminal dimensions,
     * writes the title, the ranking table (or an empty state message if no entries are
     * available), and the footer prompt into them, then flushes the result to the
     * terminal.
     * Tied players share the same rank; ranks are assigned by position after sorting.
     *
     * <p>Public so it can be re-invoked by the owning {@code ViewTUI} on a
     * terminal resize while this screen is being displayed, keeping it visible
     * and correctly sized instead of being silently replaced by a redraw of
     * the main game layout.</p>
     */
    public void renderScreen() {
        screenW = terminal.getWidth();
        screenH = terminal.getHeight();

        screenBuffer = new char[screenH][screenW];
        colorBuffer = new AttributedStyle[screenH][screenW];
        for (int i = 0; i < screenH; i++)
            for (int j = 0; j < screenW; j++) {
                screenBuffer[i][j] = ' ';
                colorBuffer[i][j] = AttributedStyle.DEFAULT;
            }

        int row = TOP_PADDING;

        String title = "=== CLASSIFICA GLOBALE — " + numPlayers + " GIOCATORI ===";
        printAt(row++, (screenW - title.length()) / 2, title,
                AttributedStyle.DEFAULT.foreground(AttributedStyle.BRIGHT).bold());
        printAt(row++, 0, "─".repeat(Math.max(0, screenW)), AttributedStyle.DEFAULT);

        row += GAP_AFTER_SEP;

        if (entries.isEmpty()) {
            String empty = "Nessun risultato disponibile.";
            printAt(row, (screenW - empty.length()) / 2, empty,
                    AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE));
        } else {
            row += GAP_BEFORE_TABLE;

            String header = String.format("%-4s %-24s %10s", "#", "Giocatore", "Punti");
            printAt(row++, LEFT_MARGIN, header,
                    AttributedStyle.DEFAULT.foreground(AttributedStyle.BRIGHT));
            printAt(row++, 0, "─".repeat(Math.max(0, screenW)), AttributedStyle.DEFAULT);

            int currentRank = 1;
            int prevPoints = Integer.MIN_VALUE;
            int count = 0;

            for (Map.Entry<String, Integer> entry : entries) {
                if (row >= screenH - FOOTER_OFFSET) break;

                int points = entry.getValue();
                String nickname = entry.getKey();
                count++;

                if (points != prevPoints) {
                    currentRank = count;
                    prevPoints = points;
                }

                AttributedStyle rowStyle = switch (currentRank) {
                    case 1 -> AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW).bold();
                    case 2 -> AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE).bold();
                    case 3 -> AttributedStyle.DEFAULT.foreground(AttributedStyle.RED);
                    default -> AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE);
                };

                String medal = switch (currentRank) {
                    case 1 -> "🥇";
                    case 2 -> "🥈";
                    case 3 -> "🥉";
                    default -> "  ";
                };

                String pointsStr = points > 0 ? "+" + points : String.valueOf(points);
                String line = String.format("%-4s %-24s %10s",
                        medal + currentRank, nickname, pointsStr);
                printAt(row++, LEFT_MARGIN, line, rowStyle);
            }
        }

        String footer = "Premi BACKSPACE per tornare indietro...";
        printAt(screenH - FOOTER_OFFSET, (screenW - footer.length()) / 2, footer,
                AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN));

        flushBuffers();
    }

    /**
     * Writes the given text into the screen and color buffers at the specified position,
     * clipping any characters that fall outside the terminal width.
     *
     * @param row   the zero-based row index to write at
     * @param col   the zero-based column index to start writing from
     * @param text  the text to write
     * @param style the ANSI style to apply to the written characters
     */
    private void printAt(int row, int col, String text, AttributedStyle style) {
        if (row < 0 || row >= screenH || col < 0) return;
        for (int i = 0; i < text.length(); i++) {
            int c = col + i;
            if (c >= screenW) break;
            screenBuffer[row][c] = text.charAt(i);
            colorBuffer[row][c] = style;
        }
    }

    /**
     * Flushes the screen and color buffers to the terminal, applying run-length style
     * compression to minimize ANSI escape sequences. Clears any remaining content
     * below the rendered area.
     */
    private void flushBuffers() {
        terminal.puts(InfoCmp.Capability.cursor_home);
        AttributedStringBuilder asb = new AttributedStringBuilder();
        for (int i = 0; i < screenH; i++) {
            AttributedStyle lastStyle = null;
            for (int j = 0; j < screenW; j++) {
                AttributedStyle currentStyle = colorBuffer[i][j];
                if (currentStyle != null && !currentStyle.equals(lastStyle)) {
                    asb.style(currentStyle);
                    lastStyle = currentStyle;
                }
                asb.append(screenBuffer[i][j]);
            }
            if (i < screenH - 1) asb.append('\n');
        }
        asb.style(AttributedStyle.DEFAULT);
        terminal.writer().print(asb.toAnsi());
        terminal.puts(InfoCmp.Capability.clr_eos);
        terminal.writer().flush();
    }

    /**
     * Blocks the current thread until the player presses Backspace (ASCII 8 or 127).
     * Any other key press is ignored.
     */
    private void waitForBackspace() {
        try {
            while (true) {
                int c = terminal.reader().read();
                if (c == BACKSPACE || c == BACKSPACE_ALT) return;
            }
        } catch (IOException e) {
            System.err.println("Errore durante l'attesa di input: " + e.getMessage());
        }
    }
}