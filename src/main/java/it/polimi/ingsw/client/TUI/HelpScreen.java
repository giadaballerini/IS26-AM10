package it.polimi.ingsw.client.TUI;

import org.jline.terminal.Terminal;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.jline.utils.InfoCmp;
import org.jline.terminal.Attributes;
import java.io.IOException;
import java.util.List;

public class HelpScreen {
    private final Terminal terminal;
    private int screenW;
    private int screenH;

    private char[][] screenBuffer;
    private AttributedStyle[][] colorBuffer;

    private static final List<Explanation> EXPLANATIONS = List.of(
            new Explanation("move <n>", "Sposta il tuo personaggio alla casella numero n (utilizzabile durante la fase di Setup)"),
            new Explanation("draw <id>", "Pesca la carta con l'ID specificato (utilizzabile durante la fase di Pesca)"),
            new Explanation("skip", "Salta il turno corrente, se possibile (utilizzabile durante la fase di Pesca)"),
            new Explanation("create <n>", "Crea una nuova partita con n giocatori"),
            new Explanation("join", "Visualizza le partite disponibili"),
            new Explanation("choose <id>", "Accedi alla partita con ID specificato"),
            new Explanation("status", "Visualizza lo status di tutti i giocatori"),
            new Explanation("help", "Mostra questa schermata di aiuto"),
            new Explanation("exit", "Disconnettiti ed esci dal gioco"),
            new Explanation("info <id>", "Visualizza le informazioni della carta con l'ID specificato"),
            new Explanation("ranking", "Mostra a schermo la classifica globale delle partite con il numero di giocatori della partita a cui hai partecipato")
    );

    public HelpScreen(Terminal terminal) {
        this.terminal = terminal;
        this.screenW = terminal.getWidth();
        this.screenH = terminal.getHeight();
    }

    public void display() throws IOException {
        Attributes attributes = terminal.getAttributes();
        saveTerminalState();

        try{
            renderHelpScreen();
            waitForBackspace();
        }finally{
            restoreTerminalState();
            terminal.setAttributes(attributes);
        }


    }

    private void saveTerminalState() {
        terminal.puts(InfoCmp.Capability.cursor_invisible);
        terminal.flush();
    }

    private void restoreTerminalState() {

        terminal.puts(InfoCmp.Capability.cursor_visible);
        terminal.writer().print("\033[0m");
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

    private void waitForBackspace() {
        try {
            while (true) {
                int c = terminal.reader().read();
                if (c == 8) {
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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
