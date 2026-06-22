package it.polimi.ingsw.client.ui.tui.screen;

import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.jline.utils.InfoCmp;

import java.io.IOException;
import java.util.List;

/**
 * TUI screen that displays the list of all available commands and their descriptions.
 * Each command is shown with its syntax in yellow followed by a description in white.
 * The screen blocks until the player presses Backspace to return to the previous view.
 */
public class HelpScreen {
    /**
     * JLine terminal handle; owns the raw-mode lifecycle and the output writer.
     */
    private final Terminal terminal;

    /** Width of the terminal in characters, refreshed at render time. */
    private int screenW;

    /** Height of the terminal in lines, refreshed at render time. */
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

    /** The full list of commands and their descriptions to display on the help screen. */
    private static final List<Explanation> EXPLANATIONS = List.of(
            new Explanation("move <n>",    "Sposta il tuo personaggio alla casella numero n (utilizzabile durante la fase di Setup)"),
            new Explanation("draw <id>",   "Pesca la carta con l'ID specificato (utilizzabile durante la fase di Pesca)"),
            new Explanation("skip",        "Salta il turno corrente, se possibile (utilizzabile durante la fase di Pesca)"),
            new Explanation("create <n>",  "Crea una nuova partita con n giocatori"),
            new Explanation("join",        "Visualizza le partite disponibili"),
            new Explanation("choose <id>", "Accedi alla partita con ID specificato"),
            new Explanation("status",      "Visualizza lo status di tutti i giocatori"),
            new Explanation("help",        "Mostra questa schermata di aiuto"),
            new Explanation("quit",        "Abbandona la partita corrente e torna al menu, restando connesso al server"),
            new Explanation("exit",        "Disconnettiti ed esci dal gioco"),
            new Explanation("info <id>",   "Visualizza le informazioni della carta con l'ID specificato"),
            new Explanation("ranking",     "Mostra a schermo la classifica globale delle partite con il numero di giocatori della partita a cui hai partecipato"),
            new Explanation("pgdn",        "Scorri il pannello dei giocatori verso il basso (~1 giocatore)"),
            new Explanation("pgup",        "Scorri il pannello dei giocatori verso l'alto (~1 giocatore)")
    );

    /**
     * Width, in characters, reserved for the command-name column so that all
     * description columns line up regardless of individual name length.
     * Derived from the longest entry in {@link #EXPLANATIONS} plus a small
     * margin, so adding a longer command name later does not require a
     * manual adjustment here.
     */
    private static final int NAME_COLUMN_WIDTH = EXPLANATIONS.stream()
            .mapToInt(e -> e.name.length())
            .max()
            .orElse(10) + 2;

    /**
     * Creates a new help screen.
     *
     * @param terminal the JLine terminal to render on
     */
    public HelpScreen(Terminal terminal) {
        this.terminal = terminal;
        this.screenW = terminal.getWidth();
        this.screenH = terminal.getHeight();
    }

    /**
     * Renders the help screen and blocks until the player presses Backspace.
     * Saves and restores the terminal state and attributes around the display.
     *
     * @throws IOException if a terminal I/O error occurs
     */
    public void display() throws IOException {
        Attributes attributes = terminal.getAttributes();
        saveTerminalState();
        try {
            renderHelpScreen();
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
     * Renders the help screen content into the internal buffers and flushes
     * it to the terminal.
     *
     * <p>Must be {@code public}: {@link it.polimi.ingsw.client.ui.tui.ViewTUI}
     * stores a reference to this method ({@code helpScreen::renderHelpScreen})
     * in its {@code activeOverlayRenderer} field so the help screen can be
     * re-rendered in place on a terminal resize while it is being displayed.</p>
     */
    public void renderHelpScreen() {
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

        String title = "=== COMANDI DISPONIBILI ===";
        printAt(2, (screenW - title.length()) / 2, title,
                AttributedStyle.DEFAULT.foreground(AttributedStyle.BRIGHT));
        printAt(3, 0, "─".repeat(screenW), AttributedStyle.DEFAULT);

        int row = 5;
        int descCol = 4 + NAME_COLUMN_WIDTH + 1;
        for (Explanation cmd : EXPLANATIONS) {
            if (row >= screenH - 3) break;

            printAt(row, 4, cmd.name, NAME_COLUMN_WIDTH,
                    AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));

            String description = "→ " + cmd.description;
            printAt(row, descCol, description, screenW - descCol - 2,
                    AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE));
            row += 2;
        }

        String footer = "Premi BACKSPACE per tornare indietro...";
        printAt(screenH - 2, (screenW - footer.length()) / 2, footer,
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
     * Writes the given text into the screen and color buffers at the specified position,
     * truncating it with an ellipsis if it exceeds {@code maxWidth} characters.
     *
     * @param row      the zero-based row index to write at
     * @param col      the zero-based column index to start writing from
     * @param text     the text to write; ignored if {@code null} or empty
     * @param maxWidth the maximum number of characters to write before truncating
     * @param style    the ANSI style to apply to the written characters
     */
    private void printAt(int row, int col, String text, int maxWidth, AttributedStyle style) {
        if (maxWidth <= 0 || text == null || text.isEmpty()) return;
        if (row < 0 || row >= screenH || col < 0 || col >= screenW) return;
        String out = text.length() > maxWidth
                ? text.substring(0, maxWidth - 3) + "..."
                : text;
        printAt(row, col, out, style);
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
            if (i < screenH - 1) {
                asb.append("\n");
            }
        }
        asb.style(AttributedStyle.DEFAULT);
        terminal.writer().print(asb.toAnsi());
        terminal.puts(InfoCmp.Capability.clr_eos);
        terminal.writer().flush();
    }

    /**
     * Blocks the current thread until the player presses Backspace (ASCII 8).
     * Any other key press is ignored.
     */
    private void waitForBackspace() {
        try {
            while (true) {
                int c = terminal.reader().read();
                if (c == 8) return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Holds the syntax and description of a single TUI command,
     * used to populate the help screen entries.
     */
    private static class Explanation {

        /** The command syntax in the correct form (e.g. {@code "draw <id>"}). */
        String name;

        /** A brief description of what the command does and when it can be used. */
        String description;

        /**
         * Creates a new command explanation entry.
         *
         * @param name        the command syntax
         * @param description the command description
         */
        Explanation(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }
}