package it.polimi.ingsw.client.ui.TUI.screen;

import it.polimi.ingsw.client.ui.TUI.utility.TUIColorMapper;
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

public class EventScreen {

    private final Terminal terminal;
    private final EventDTO events;
    private final Map<String, PlayerStatsDTO> beforeMap;
    private final List<PlayerDTO> players;

    private int screenW, screenH;
    private char[][] screenBuffer;
    private AttributedStyle[][] colorBuffer;

    public EventScreen(Terminal terminal, EventDTO events, List<PlayerStatsDTO> statsBefore, List<PlayerDTO> players) {
        this.terminal  = terminal;
        this.events    = events;
        this.players   = players;
        this.beforeMap = statsBefore.stream()
                .collect(Collectors.toMap(PlayerStatsDTO::getNickname, s -> s));
    }

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
            if(before.getPPs() != after.getPPs())
                printAt(row++, leftMargin + 4,
                        String.format("PP:     %d → %d", before.getPPs(), after.getPPs()),
                        colorFor(after.getPPs() - before.getPPs()));
            if(before.getnFood() != after.getnFood())
                printAt(row++, leftMargin + 4,
                        String.format("Cibo:   %d → %d", before.getnFood(), after.getnFood()),
                        colorFor(after.getnFood() - before.getnFood()));
            if(before.getnStars() != after.getnStars())
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

    private AttributedStyle colorFor(int delta) {
        if (delta > 0) return AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN);
        if (delta < 0) return AttributedStyle.DEFAULT.foreground(AttributedStyle.RED);
        return AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE);
    }

    private void printAt(int row, int col, String text, AttributedStyle style) {
        if (row < 0 || row >= screenH || col < 0 || text == null) return;
        for (int i = 0; i < text.length(); i++) {
            int c = col + i;
            if (c >= screenW) break;
            screenBuffer[row][c] = text.charAt(i);
            colorBuffer[row][c]  = style;
        }
    }

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