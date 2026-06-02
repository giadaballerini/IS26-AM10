package it.polimi.ingsw.client.TUI;

import it.polimi.ingsw.network.dto.PlayerStatsDTO;
import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.jline.utils.InfoCmp;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

public class EndGameScreen {

    private static final int TOP_PADDING        = 2;
    private static final int GAP_AFTER_SEP      = 1;
    private static final int GAP_BEFORE_RANKING = 1;
    private static final int FOOTER_OFFSET      = 2;
    private static final int LEFT_MARGIN        = 4;
    private static final int RANKING_WIDTH      = 60;

    private final Terminal terminal;
    private final List<PlayerStatsDTO> stats;
    private final int rankingPos;
    private final int globalRankingPos;
    private final int numPlayers;

    private int screenW;
    private int screenH;
    private char[][] screenBuffer;
    private AttributedStyle[][] colorBuffer;

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
                case 1 -> AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW).bold();
                case 2 -> AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE).bold();
                case 3 -> AttributedStyle.DEFAULT.foreground(AttributedStyle.RED);
                default -> AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE);
            };

            String medal = switch (currentRank) {
                case 1 -> "🥇 ";
                case 2 -> "🥈 ";
                case 3 -> "🥉 ";
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
                if (c == 8 || c == 127) return;
            }
        } catch (IOException e) {
            System.err.println("Errore durante l'attesa di input: " + e.getMessage());
        }
    }
}
