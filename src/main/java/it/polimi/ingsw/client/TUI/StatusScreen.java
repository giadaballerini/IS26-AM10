package it.polimi.ingsw.client.TUI;

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

public class StatusScreen {

    private final Terminal terminal;
    private final List<PlayerStatusDTO> statuses;
    private final List<PlayerDTO> players;
    private int screenW;
    private int screenH;
    private char[][] screenBuffer;
    private AttributedStyle[][] colorBuffer;

    private static final List<FlagEntry> FLAGS = List.of(
            new FlagEntry("Hunt", PlayerStatusDTO::isHuntBonus),
            new FlagEntry("Paint", PlayerStatusDTO::isPaintFlag),
            new FlagEntry("Extra pescata", PlayerStatusDTO::isExtraFlag),
            new FlagEntry("Protezione", PlayerStatusDTO::hasProtection),
            new FlagEntry("Sconto Painter", s->s.hasDiscountFor(CardTypeEnum.PAINTER)),
            new FlagEntry("Sconto Crafter", s->s.hasDiscountFor(CardTypeEnum.CRAFTER)),
            new FlagEntry("Sconto Gatherer", s->s.hasDiscountFor(CardTypeEnum.GATHERER)),
            new FlagEntry("Double Shaman", PlayerStatusDTO::hasDoubleShamanIncome)
    );

    public StatusScreen(Terminal terminal, List<PlayerStatusDTO> statuses, List<PlayerDTO> players) {
        this.terminal = terminal;
        this.statuses = statuses;
        this.players  = players;
    }

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

    private void waitForBackspace() {
        try {
            while (true) {
                int c = terminal.reader().read();
                if (c == 8 || c == 127) return;
            }
        } catch (Exception ignored) {}
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

    @FunctionalInterface
    private interface FlagReader {
        boolean test(PlayerStatusDTO s);
    }

    private static class FlagEntry {
        final String label;
        final FlagReader reader;

        FlagEntry(String label, FlagReader reader) {
            this.label  = label;
            this.reader = reader;
        }

        boolean test(PlayerStatusDTO s) {
            return reader.test(s);
        }
    }
}