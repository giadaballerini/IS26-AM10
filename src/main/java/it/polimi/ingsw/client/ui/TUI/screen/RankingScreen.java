package it.polimi.ingsw.client.ui.TUI.screen;

import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.jline.utils.InfoCmp;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class RankingScreen {

    private static final int TOP_PADDING      = 2;
    private static final int GAP_AFTER_SEP    = 1;
    private static final int GAP_BEFORE_TABLE = 1;
    private static final int FOOTER_OFFSET    = 2;
    private static final int LEFT_MARGIN      = 4;
    private static final int BACKSPACE        = 8;
    private static final int BACKSPACE_ALT    = 127;

    private final Terminal terminal;
    private final List<Map.Entry<String, Integer>> entries;
    private final int numPlayers;

    private int screenW;
    private int screenH;
    private char[][] screenBuffer;
    private AttributedStyle[][] colorBuffer;

    public RankingScreen(Terminal terminal, Map<String, Integer> ranks, int numPlayers) {
        this.terminal = terminal;
        this.numPlayers = numPlayers;
        this.entries = ranks.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .toList();
    }

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

    private void saveTerminalState() {
        terminal.puts(InfoCmp.Capability.cursor_invisible);
        terminal.flush();
    }

    private void restoreTerminalState() {
        terminal.puts(InfoCmp.Capability.cursor_visible);
        terminal.writer().print("\033[0m");
        terminal.writer().flush();
    }

    private void renderScreen() {
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

            for (int i = 0; i < entries.size(); i++) {
                if (row >= screenH - FOOTER_OFFSET) break;

                Map.Entry<String, Integer> entry = entries.get(i);
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

    private void printAt(int row, int col, String text, AttributedStyle style) {
        if (row < 0 || row >= screenH || col < 0) return;
        for (int i = 0; i < text.length(); i++) {
            int c = col + i;
            if (c >= screenW) break;
            screenBuffer[row][c] = text.charAt(i);
            colorBuffer[row][c] = style;
        }
    }

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