package it.polimi.ingsw.client.ui.tui.screen;

import it.polimi.ingsw.client.ui.tui.utility.TUIColorMapper;
import it.polimi.ingsw.network.dto.CardDTO;
import it.polimi.ingsw.network.dto.EventDTO;
import it.polimi.ingsw.network.dto.PlayerDTO;
import it.polimi.ingsw.network.dto.PlayerStatsDTO;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.jline.utils.InfoCmp;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * TUI screen displayed at the end of each round to show the events that triggered
 * and their effects on each player. For each player, stat changes in Prestige Points,
 * food, and stars are shown as before/after comparisons, color-coded green for gains
 * and red for losses. The screen is displayed for 8 seconds and then dismissed automatically.
 */
public class EventScreen {

    /** JLine terminal handle; owns the raw-mode lifecycle and the output writer. */
    private final Terminal terminal;

    /** The event data for the current round, including triggered event cards and updated player stats. */
    private final EventDTO events;

    /**
     * Snapshot of each player's stats before the events were applied,
     * keyed by nickname for fast lookup.
     */
    private final Map<String, PlayerStatsDTO> beforeMap;

    /** The list of all players in the match, used to resolve pawn colors for name styling. */
    private final List<PlayerDTO> players;

    /** Width of the terminal in characters, read at display time. */
    private int screenW;

    /** Height of the terminal in lines, read at display time. */
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
     * Creates a new event screen for the current round.
     *
     * @param terminal    the JLine terminal to render on
     * @param events      the event data for the round, including triggered cards and updated stats
     * @param statsBefore the stats of all players before the events were applied
     * @param players     the list of all players in the match, used to resolve pawn colors
     */
    public EventScreen(Terminal terminal, EventDTO events, List<PlayerStatsDTO> statsBefore, List<PlayerDTO> players) {
        this.terminal  = terminal;
        this.events    = events;
        this.players   = players;
        this.beforeMap = statsBefore.stream()
                .collect(Collectors.toMap(PlayerStatsDTO::getNickname, s -> s));
    }

    /**
     * Renders the event screen and holds it for 8 seconds, updating the countdown
     * footer every second, then clears the terminal and returns.
     *
     * @throws IOException          if a terminal I/O error occurs
     * @throws InterruptedException if the countdown sleep is interrupted
     */
    public void display() throws IOException, InterruptedException {
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

        String title = "═══════════ EVENTI DEL ROUND ═══════════";
        printAt(2, (screenW - title.length()) / 2, title,
                AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW).bold());
        printAt(3, 0, "─".repeat(screenW), AttributedStyle.DEFAULT);

        int row = 5;
        for (CardDTO c : events.getEvents()) {
            printAt(row++, (screenW - c.getType().toString().length()) / 2,
                    "⚡  " + c.getType(),
                    AttributedStyle.DEFAULT.foreground(AttributedStyle.BRIGHT).bold());
        }

        row++;
        String sep = "──────── Effetti sui giocatori ────────";
        printAt(row++, (screenW - sep.length()) / 2, sep,
                AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN));
        row++;

        int leftMargin = 6;
        for (PlayerStatsDTO after : events.getStats()) {
            PlayerStatsDTO before = beforeMap.get(after.getNickname());
            if (before == null) continue;

            AttributedStyle nameStyle = players.stream()
                    .filter(p -> p.getNickname().equals(after.getNickname()))
                    .findFirst()
                    .map(p -> TUIColorMapper.getPlayerJlineColor(p.getColor()).bold())
                    .orElse(AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE).bold());

            printAt(row++, leftMargin, after.getNickname(), nameStyle);
            row++;
            if (before.getPPs() != after.getPPs())
                printAt(row++, leftMargin + 4,
                        String.format("PP:     %d → %d", before.getPPs(), after.getPPs()),
                        colorFor(after.getPPs() - before.getPPs()));
            if (before.getnFood() != after.getnFood())
                printAt(row++, leftMargin + 4,
                        String.format("Cibo:   %d → %d", before.getnFood(), after.getnFood()),
                        colorFor(after.getnFood() - before.getnFood()));
            if (before.getnStars() != after.getnStars())
                printAt(row++, leftMargin + 4,
                        String.format("Stelle: %d → %d", before.getnStars(), after.getnStars()),
                        colorFor(after.getnStars() - before.getnStars()));
            row += 2;
        }

        for (int i = 8; i > 0; i--) {
            String footer = "Chiusura tra " + i + " secondi...";
            printAt(screenH - 2, (screenW - footer.length()) / 2, footer,
                    AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN));
            flushBuffers();
            Thread.sleep(1000);
        }

        terminal.puts(InfoCmp.Capability.clear_screen);
        terminal.writer().flush();
    }

    /**
     * Returns the ANSI style corresponding to the sign of the given stat delta.
     * Positive deltas are styled green, negative red, and unchanged values white.
     *
     * @param delta the difference between a stat's value after and before the event
     * @return the {@link AttributedStyle} to apply to the stat change line
     */
    private AttributedStyle colorFor(int delta) {
        if (delta > 0) return AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN);
        if (delta < 0) return AttributedStyle.DEFAULT.foreground(AttributedStyle.RED);
        return AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE);
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
}