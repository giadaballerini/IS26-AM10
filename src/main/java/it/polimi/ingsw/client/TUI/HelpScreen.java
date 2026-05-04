package it.polimi.ingsw.client.TUI;

import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.jline.utils.InfoCmp;

import java.io.IOException;
import java.util.List;

public class HelpScreen {
    private final Terminal terminal;
    private final LineReader reader;
    private int screenW;
    private int screenH;

    private char[][] screenBuffer;
    private AttributedStyle[][] colorBuffer;

    private static final List<Explanation> EXPLANATIONS = List.of(
            new Explanation("move <n>", "Sposta il tuo personaggio alla casella numero n"),
            new Explanation("draw <id>", "Pesca la carta con l'ID specificato"),
            new Explanation("skip", "Salta il turno corrente"),
            new Explanation("create <n>", "Crea una nuova partita con n giocatori"),
            new Explanation("join", "Visualizza le partite disponibili"),
            new Explanation("choose <id>", "Accedi alla partita con ID specificato"),
            new Explanation("help", "Mostra questa schermata di aiuto"),
            new Explanation("exit", "Disconnettiti ed esci dal gioco"),
            new Explanation("info <id>", "Visualizza le informazioni della carta con l'ID specificato")
    );

    public HelpScreen(Terminal terminal, LineReader reader) {
        this.terminal = terminal;
        this.reader = reader;
        this.screenW = terminal.getWidth();
        this.screenH = terminal.getHeight();
    }

    public void display() throws IOException {
        saveTerminalState();
        renderHelpScreen();
        waitForBackspace();
        restoreTerminalState();
    }

    private void saveTerminalState() {
        terminal.puts(InfoCmp.Capability.enter_ca_mode);
        terminal.puts(InfoCmp.Capability.cursor_invisible);
        terminal.flush();
    }

    private void restoreTerminalState() {
        terminal.puts(InfoCmp.Capability.exit_ca_mode);
        terminal.puts(InfoCmp.Capability.cursor_visible);
        terminal.flush();
        terminal.writer().print("\033[0m");
        terminal.writer().print("\033[2J");
        terminal.writer().print("\033[H");
        terminal.writer().flush();
    }

    private void renderHelpScreen() {
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
        for (Explanation cmd : EXPLANATIONS) {
            if (row >= screenH - 5) break;

            // Nome comando in giallo
            printAt(row, 4, cmd.name, screenW - 8,
                    AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
            row++;

            String description = "  → " + cmd.description;
            printAt(row, 4, description, screenW - 8,
                    AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE));
            row += 2;
        }

        String footer = "Premi BACKSPACE per tornare indietro...";
        printAt(screenH - 2, (screenW - footer.length()) / 2, footer,
                AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN));

        flushBuffers();
    }

    private void printAt(int row, int col, String text, AttributedStyle style) {
        if (row < 0 || row >= screenH || col < 0) return;

        for (int i = 0; i < text.length(); i++) {
            int c = col + i;
            if (c >= screenW) break;
            screenBuffer[row][c] = text.charAt(i);
            colorBuffer[row][c] = style;
        }
    }

    private void printAt(int row, int col, String text, int maxWidth, AttributedStyle style) {
        if (maxWidth <= 0 || text == null || text.isEmpty()) return;
        if (row < 0 || row >= screenH || col < 0 || col >= screenW) return;

        String out = text.length() > maxWidth
                ? text.substring(0, maxWidth - 3) + "..."
                : text;

        printAt(row, col, out, style);
    }

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

    private void waitForBackspace() throws IOException {
        while (true) {
            try {
                int c = terminal.reader().read();
                if (c == '\b')
                    break;
            } catch (Exception e) {
                break;
            }
        }
    }

    private static class Explanation {
        String name;
        String description;

        Explanation(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }
}
