package it.polimi.ingsw.client.ui.TUI.screen;

import it.polimi.ingsw.network.dto.PlayerStatsDTO;
import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.jline.utils.InfoCmp;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

/**
 * TUI screen displayed at the end of a match. Renders a full-terminal end game summary
 * showing the player's match ranking position, their global leaderboard position,
 * and a final standings table for all players sorted by Prestige Points and food.
 * The screen blocks until the player presses Backspace to return to the previous view.
 */
public class EndGameScreen {

    /** Number of blank lines before the title. */
    private static final int TOP_PADDING        = 2;

    /** Blank lines between the separator and the ranking position line. */
    private static final int GAP_AFTER_SEP      = 1;

    /** Blank lines between the position lines and the final standings table. */
    private static final int GAP_BEFORE_RANKING = 1;

    /** Distance from the bottom of the screen reserved for the footer prompt. */
    private static final int FOOTER_OFFSET      = 2;

    /** Left margin for content that is not centered. */
    private static final int LEFT_MARGIN        = 4;

    /** Width of the final standings table in characters. */
    private static final int RANKING_WIDTH      = 60;

    private final Terminal terminal;

    /**
     * The final stats of all players, sorted by Prestige Points descending,
     * then by food count descending as a tiebreaker.
     */
    private final List<PlayerStatsDTO> stats;

    /** The client player's final position in the match ranking. */
    private final int rankingPos;

    /** The client player's position in the global leaderboard after the match. */
    private final int globalRankingPos;

    /** The number of players in the match, used to label the global ranking line. */
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
     * Creates a new end game screen.
     *
     * @param terminal         the JLine terminal to render on
     * @param stats            the final stats of all players in the match
     * @param rankingPos       the client player's position in the match ranking
     * @param globalRankingPos the client player's position in the global leaderboard
     */
    public EndGameScreen(Terminal terminal, List<PlayerStatsDTO> stats, int rankingPos, int globalRankingPos) {
        this.terminal = terminal;
        this.stats = stats.stream()
                .sorted(Comparator.comparing(PlayerStatsDTO::getPPs, Comparator.reverseOrder())
                        .thenComparing(PlayerStatsDTO::getnFood, Comparator.reverseOrder()))
                .toList();
        numPlayers = stats.size();
        this.rankingPos = rankingPos;
        this.globalRankingPos = globalRankingPos;
    }

    /**
     * Renders the end game screen and blocks until the player presses Backspace.
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
     * writes all content into them, and flushes the result to the terminal.
     * Renders the title, the player's match and global ranking positions,
     * and the final standings table for all players.
     */
    private void renderScreen() {
        screenW = terminal.getWidth();
        screenH = terminal.getHeight();

        screenBuffer = new char[screenH][screenW];
        colorBuffer = new AttributedStyle[screenH][screenW];

        for (int i = 0; i < screenH; i++) {
            for (int j = 0; j < screenW; j++) {
                screenBuffer[i][j] = ' ';
                colorBuffer[i][j] = AttributedStyle.DEFAULT;
            }
        }

        int row = TOP_PADDING;

        String title = "★  PARTITA TERMINATA  ★";
        printAt(row++, (screenW - title.length()) / 2, title,
                AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW).bold());
        printAt(row++, 0, "─".repeat(Math.max(0, screenW)), AttributedStyle.DEFAULT);

        row += GAP_AFTER_SEP;
        String posLine = "Ti sei classificato/a al posto numero: " + rankingPos;
        printAt(row++, (screenW - posLine.length()) / 2, posLine,
                AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN).bold());

        String globalLine;
        String globalSuffix;
        if (globalRankingPos == 1) {
            globalLine   = "🏆  SEI IL MIGLIORE IN ASSOLUTO!  🏆";
            globalSuffix = "";
        } else if (globalRankingPos <= 3) {
            globalLine   = "🌟  Posizione globale (partite a " + numPlayers + "P): ";
            globalSuffix = "#" + globalRankingPos + "  🌟";
        } else {
            globalLine   = "🌍  Posizione globale (partite a " + numPlayers + "P): ";
            globalSuffix = "#" + globalRankingPos;
        }
        String fullGlobalLine = globalLine + globalSuffix;
        AttributedStyle globalStyle = (globalRankingPos <= 3)
                ? AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW).bold()
                : AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE);
        printAt(row++, (screenW - fullGlobalLine.length()) / 2, fullGlobalLine, globalStyle);

        row += GAP_BEFORE_RANKING;
        row++;

        int rankingStartCol = (screenW - RANKING_WIDTH) / 2;

        printAt(row++, rankingStartCol, "CLASSIFICA FINALE:",
                AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE).bold());
        printAt(row++, rankingStartCol, "─".repeat(RANKING_WIDTH), AttributedStyle.DEFAULT);

        String header = String.format("%-6s %-20s %8s %8s", "#", "Giocatore", "PP", "Cibo");
        printAt(row++, rankingStartCol, header,
                AttributedStyle.DEFAULT.foreground(AttributedStyle.BRIGHT));
        printAt(row++, rankingStartCol, "─".repeat(RANKING_WIDTH), AttributedStyle.DEFAULT);

        int currentRank = 1;
        for (int i = 0; i < stats.size(); i++) {
            if (row >= screenH - FOOTER_OFFSET) break;
            PlayerStatsDTO s = stats.get(i);

            if (i > 0) {
                PlayerStatsDTO prev = stats.get(i - 1);
                if (s.getPPs() != prev.getPPs() || s.getnFood() != prev.getnFood()) {
                    currentRank = i + 1;
                }
            }

            AttributedStyle rowStyle = switch (currentRank) {
                case 1  -> AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW).bold();
                case 2  -> AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE).bold();
                case 3  -> AttributedStyle.DEFAULT.foreground(AttributedStyle.RED);
                default -> AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE);
            };

            String medal = switch (currentRank) {
                case 1  -> "🥇 ";
                case 2  -> "🥈 ";
                case 3  -> "🥉 ";
                default -> "   ";
            };

            String line = String.format("%-6s %-20s %8d %8d %8d",
                    medal + currentRank, s.getNickname(), s.getPPs(), s.getnFood());
            printAt(row++, rankingStartCol, line, rowStyle);
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
                if (c == 8 || c == 127) return;
            }
        } catch (IOException e) {
            System.err.println("Errore durante l'attesa di input: " + e.getMessage());
        }
    }
}