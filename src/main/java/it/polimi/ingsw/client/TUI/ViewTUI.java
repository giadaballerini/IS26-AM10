package it.polimi.ingsw.client.TUI;

import it.polimi.ingsw.client.*;
import it.polimi.ingsw.client.commands.Command;
import it.polimi.ingsw.enumerations.CrafterSymbolEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.exceptions.InvalidCardException;
import it.polimi.ingsw.network.dto.*;
import org.jline.reader.*;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.jline.utils.InfoCmp;
import java.io.IOException;
import java.util.*;
import org.jline.terminal.Terminal.Signal;

import static it.polimi.ingsw.client.TUI.CardColorMapper.getCardJlineColor;
import static it.polimi.ingsw.client.TUI.TUIColorMapper.getPlayerJlineColor;

public class ViewTUI implements UserInterface {

    private enum LayoutTier {
        COMPACT  (0, 120),
        STANDARD (120, 180),
        LARGE    (180, 9999);

        final int minCols, maxCols;
        LayoutTier(int minCols, int maxCols) {
            this.minCols = minCols;
            this.maxCols = maxCols;
        }

        static LayoutTier from(int cols) {
            for (LayoutTier t : values())
                if (cols >= t.minCols && cols < t.maxCols) return t;
            return STANDARD;
        }
    }

    private LayoutTier currentTier = LayoutTier.STANDARD;

    VirtualModel model;
    private LineReader reader;
    private Client client;
    private Terminal terminal;

    private static final int MAX_LOGS = 8;
    private final List<LogEntry> logs = new ArrayList<>();

    private char[][] screenBuffer;
    private AttributedStyle[][] colorBuffer;
    private int screenW, screenH;

    private int layoutStartRow;
    private int layoutPanelHeight;
    private int layoutLeftSize;
    private int layoutCenterSize;
    private int layoutRightSize;
    private int layoutSepStart;
    private int layoutLogStart;
    private boolean layoutValid = false;

    private static final Set<String> GROUPABLE_CHARACTERS = Set.of(
            "PAINTER", "GATHERER", "SHAMAN"
    );

    private boolean isGroupable(String type) {
        return type != null && GROUPABLE_CHARACTERS.contains(type.toUpperCase());
    }

    public ViewTUI(VirtualModel model, Client client) {
        this.model = model;
        this.client = client;
        initJline();
        printLogo();
    }

    public void initJline() {
        System.setProperty("jansi.passthrough", "true");
        System.setProperty("org.jline.terminal.jansi", "true");
        System.setProperty("org.jline.terminal.jna", "true");
        try {
            this.terminal = TerminalBuilder.builder().system(true).jansi(true).jna(false).build();
            terminal.puts(InfoCmp.Capability.enter_ca_mode);
            terminal.puts(InfoCmp.Capability.cursor_invisible);
            terminal.flush();
            terminal.handle(Signal.WINCH, signal -> redrawScreen());
            TuiCompleter completer = new TuiCompleter(this);
            this.reader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .completer(completer)
                    .option(LineReader.Option.ERASE_LINE_ON_FINISH, true)
                    .build();
        } catch (IOException e) {
            System.err.println("Errore inizializzazione terminale: " + e.getMessage());
        }
    }

    public void start() {
        boolean loggedIn = false;
        while (!loggedIn) {
            String nickname = reader.readLine("Inserire il nickname: ").trim();
            if (!nickname.isEmpty())
                loggedIn = client.login(nickname);
        }
        redrawScreen();
        while (true) {
            try {
                String input = reader.readLine("Comando> ").trim();
                if (input.isEmpty()) {
                    redrawScreen();
                    continue;
                }
                Command command = CommandParser.parse(input, client);
                if (command != null) {
                    if (command.shouldClearLogs())
                        clearLogs();
                    command.execute(client);
                } else {
                    log("Comando non riconosciuto. Digitare 'help' per ottenere la lista completa dei comandi utilizzabili. Premere TAB per usare l'auto completer.");
                }
            } catch (UserInterruptException | EndOfFileException e) {
                System.exit(0);
            } catch (Exception e) {
                log("Errore: " + e.getMessage());
            }
        }
    }


    private boolean computeLayout() {
        screenW = terminal.getWidth();
        screenH = terminal.getHeight() - 1;
        currentTier = LayoutTier.from(screenW);

        if (screenW < 20 || screenH < 10) {
            layoutValid = false;
            return false;
        }

        layoutLogStart  = screenH - MAX_LOGS;
        layoutSepStart  = layoutLogStart - 1;
        layoutStartRow  = 3;
        layoutPanelHeight = layoutSepStart - layoutStartRow;

        switch (currentTier) {
            case LARGE -> {
                layoutLeftSize   = screenW / 5;
                layoutCenterSize = screenW * 4 / 10;
            }
            case COMPACT -> {
                layoutLeftSize   = screenW / 4;
                layoutCenterSize = screenW / 2;
            }
            default -> {
                layoutLeftSize   = screenW / 6;
                layoutCenterSize = screenW / 2;
            }
        }
        layoutRightSize = screenW - layoutLeftSize - layoutCenterSize;
        layoutValid = true;
        return true;
    }


    private synchronized void redrawScreen() {
        if (terminal == null || reader == null) return;

        terminal.pause();
        terminal.writer().print("\033[3J");

        if (!computeLayout()) {
            terminal.resume();
            return;
        }

        screenBuffer = new char[screenH][screenW];
        for (int i = 0; i < screenH; i++) Arrays.fill(screenBuffer[i], ' ');
        colorBuffer = new AttributedStyle[screenH][screenW];
        for (int r = 0; r < screenH; r++)
            for (int c = 0; c < screenW; c++)
                colorBuffer[r][c] = AttributedStyle.DEFAULT;

        String banner = "*** MESOS ***";
        printAt(1, Math.max(0, (screenW - banner.length()) / 2), banner, screenW);

        if (model != null && model.getQueue() != null && !model.getQueue().isEmpty()) {
            try {
                drawBox(layoutStartRow, 0,                               layoutLeftSize - 1,   layoutPanelHeight);
                drawBox(layoutStartRow, layoutLeftSize,                  layoutCenterSize - 1, layoutPanelHeight);
                drawBox(layoutStartRow, layoutLeftSize + layoutCenterSize, layoutRightSize - 1,  layoutPanelHeight);

                drawQueuePanel  (layoutStartRow + 1, 1,                                          layoutLeftSize - 3);
                drawCenterBoard (layoutStartRow + 1, layoutLeftSize + 1,                          layoutCenterSize - 3);
                drawPlayersPanel(layoutStartRow + 1, layoutLeftSize + layoutCenterSize + 1,       layoutRightSize - 3);
            } catch (Exception ignored) {}
        } else {
            String waitMsg = "In attesa dell'inizio della partita...";
            printAt(screenH / 2, (screenW - waitMsg.length()) / 2, waitMsg, screenW);
        }

        printAt(layoutSepStart, 0, "─".repeat(screenW), screenW);
        for (int i = 0; i < logs.size(); i++) {
            if (layoutLogStart + i < screenH) {
                LogEntry entry = logs.get(i);
                printAt(layoutLogStart + i, 0, entry.message, screenW, entry.style);
            }
        }

        flushBuffers();
        redisplayPrompt();
        terminal.resume();
    }


    private void clearRegion(int startRow, int startCol, int rows, int cols) {
        for (int r = startRow; r < startRow + rows && r < screenH; r++) {
            for (int c = startCol; c < startCol + cols && c < screenW; c++) {
                screenBuffer[r][c] = ' ';
                colorBuffer[r][c]  = AttributedStyle.DEFAULT;
            }
        }
    }


    private synchronized void redrawQueuePanel() {
        if (!layoutValid || screenBuffer == null) { redrawScreen(); return; }
        clearRegion(layoutStartRow + 1, 1, layoutPanelHeight - 2, layoutLeftSize - 3);
        drawQueuePanel(layoutStartRow + 1, 1, layoutLeftSize - 3);
        flushBuffers();
        redisplayPrompt();
    }

    private synchronized void redrawCenterBoard() {
        if (!layoutValid || screenBuffer == null) { redrawScreen(); return; }
        clearRegion(layoutStartRow + 1, layoutLeftSize + 1, layoutPanelHeight - 2, layoutCenterSize - 3);
        drawCenterBoard(layoutStartRow + 1, layoutLeftSize + 1, layoutCenterSize - 3);
        flushBuffers();
        redisplayPrompt();
    }

    private synchronized void redrawPlayersPanel() {
        if (!layoutValid || screenBuffer == null) { redrawScreen(); return; }
        clearRegion(layoutStartRow + 1, layoutLeftSize + layoutCenterSize + 1, layoutPanelHeight - 2, layoutRightSize - 3);
        drawPlayersPanel(layoutStartRow + 1, layoutLeftSize + layoutCenterSize + 1, layoutRightSize - 3);
        flushBuffers();
        redisplayPrompt();
    }

    private synchronized void redrawLogs() {
        if (!layoutValid || screenBuffer == null) { redrawScreen(); return; }
        for (int c = 0; c < screenW; c++) {
            screenBuffer[layoutSepStart][c] = '─';
            colorBuffer[layoutSepStart][c]  = AttributedStyle.DEFAULT;
        }
        for (int i = 0; i < MAX_LOGS && layoutLogStart + i < screenH; i++) {
            Arrays.fill(screenBuffer[layoutLogStart + i], ' ');
            Arrays.fill(colorBuffer[layoutLogStart + i],  AttributedStyle.DEFAULT);
            if (i < logs.size()) {
                LogEntry entry = logs.get(i);
                printAt(layoutLogStart + i, 0, entry.message, screenW, entry.style);
            }
        }
        flushBuffers();
        redisplayPrompt();
    }


    private void flushBuffers() {
        terminal.puts(InfoCmp.Capability.cursor_home);
        AttributedStringBuilder asb = new AttributedStringBuilder();
        for (int i = 0; i < screenH; i++) {
            int j = 0;
            while (j < screenW - 1) {
                char hi = screenBuffer[i][j];
                if (Character.isHighSurrogate(hi) && j + 1 < screenW - 1) {
                    char lo = screenBuffer[i][j + 1];
                    if (Character.isLowSurrogate(lo)) {
                        asb.style(colorBuffer[i][j]);
                        asb.append(hi);
                        asb.append(lo);
                        j += 2;
                        continue;
                    }
                }
                asb.style(colorBuffer[i][j]);
                asb.append(hi);
                j++;
            }
            if (i < screenH - 1) asb.append("\n");
        }
        asb.style(AttributedStyle.DEFAULT);
        terminal.writer().print(asb.toAnsi());
        terminal.puts(InfoCmp.Capability.clr_eos);
        terminal.writer().flush();
    }


    private void redisplayPrompt() {
        terminal.puts(InfoCmp.Capability.cursor_address, terminal.getHeight() - 1, 0);
        terminal.puts(InfoCmp.Capability.clr_eol);
        terminal.flush();
        try {
            if (reader != null) {
                reader.callWidget(LineReader.REDRAW_LINE);
                reader.callWidget(LineReader.REDISPLAY);
            }
        } catch (IllegalStateException ignored) {}
        terminal.writer().flush();
    }


    private synchronized void clearLogs() {
        logs.clear();
    }

    private synchronized void log(String message) {
        logs.add(new LogEntry(message, AttributedStyle.DEFAULT));
        if (logs.size() > MAX_LOGS) logs.removeFirst();
        redrawLogs();
    }

    private synchronized void logColored(String message, AttributedStyle color) {
        logs.add(new LogEntry(message, color));
        if (logs.size() > MAX_LOGS) logs.removeFirst();
        redrawLogs();
    }


    private void drawQueuePanel(int startRow, int startCol, int maxWidth) {
        int row = startRow;

        String nickLabel = "Nickname: ";
        printAt(row, startCol, nickLabel, maxWidth);
        printAt(row++, startCol + nickLabel.length(), model.getNickname(),
                maxWidth - nickLabel.length(), getNicknameStyle(model.getNickname()));

        printAt(row++, startCol, "ORDINE TURNO", maxWidth);
        printAt(row++, startCol, "---------", maxWidth);
        List<TileDTO> queue = model.getQueue();
        for (int j = 0; j < queue.size(); j++) {
            TileDTO tile = queue.get(j);
            if (tile.isOccupied()) {
                String prefix = (j + 1) + ". ";
                printAt(row, startCol, prefix, maxWidth);
                printAt(row, startCol + prefix.length(), tile.getPlayer(),
                        maxWidth - prefix.length(), getNicknameStyle(tile.getPlayer()));
            } else {
                printAt(row, startCol, (j + 1) + ". -", maxWidth);
            }
            row++;
        }

        printAt(row++, startCol, "Fase di gioco:", maxWidth);
        printAt(row++, startCol, phaseToLabel(model.getCurrentPhase()), maxWidth);
        printAt(row++, startCol, "Turno corrente:" + model.getCurrTurn(), maxWidth);
        printAt(row++, startCol, "Era corrente: " + model.getCurrAge(), maxWidth);
        printAt(row++, startCol, "Giocatore corrente:", maxWidth);
        printAt(row, startCol, model.getCurrPlayer(), maxWidth, getNicknameStyle(model.getCurrPlayer()));
    }

    private String phaseToLabel(GamePhaseEnum currentPhase) {
        if (currentPhase == null) return "-";
        return switch (currentPhase) {
            case SETUP_PHASE        -> "Setup";
            case DRAW_PHASE         -> "Pesca";
            case OPTIONAL_DRAW_PHASE -> "Pesca aggiuntiva";
            case END_TURN           -> "Fine turno";
            case END_ROUND          -> "Fine round";
            case PLAY_EVENT         -> "Evento in corso";
            case END_GAME           -> "Fine partita";
            case NONE               -> "";
        };
    }

    private void drawCenterBoard(int startRow, int startCol, int maxWidth) {
        int row = startRow;
        int half = maxWidth / 2;

        printAt(row++, startCol, "*** LISTA SUPERIORE ***", maxWidth);
        for (int i = 0; i < model.getUpperList().size(); i += 2) {
            CardDTO left = model.getUpperList().get(i);
            printAt(row, startCol, buildCardLabel(left), half, getCardJlineColor(left.getType().toString()));
            if (i + 1 < model.getUpperList().size()) {
                CardDTO right = model.getUpperList().get(i + 1);
                printAt(row, startCol + half, buildCardLabel(right), half, getCardJlineColor(right.getType().toString()));
            }
            row++;
        }
        row++;

        int col = startCol;
        for (int i = 0; i < model.getBoard().size(); i++) {
            TileDTO t = model.getBoard().get(i);

            String left = "[" + (i+1) + ":";
            printAt(row, col, left, maxWidth - (col-startCol));
            col += displayWidth(left);

            String playerInitials;
            AttributedStyle style;
            if (t.isOccupied()) {
                String nick = t.getPlayer();
                playerInitials = nick.length() <= 3 ? nick : nick.substring(0, 3);
                style = getNicknameStyle(nick);
            } else {
                playerInitials = "──";
                style = AttributedStyle.DEFAULT;
            }
            printAt(row, col, playerInitials, maxWidth - (col-startCol), style);
            col += displayWidth(playerInitials);

            StringBuilder indicators = new StringBuilder();
            if (t.getUpDraws() > 0)   indicators.append("↑".repeat(t.getUpDraws()));
            if (t.getDownDraws() > 0) indicators.append("↓".repeat(t.getDownDraws()));
            if (t.getFoodAmount() > 0) indicators.append(" C: ").append(t.getFoodAmount());
            if (t.isOccupied() && t.getPlayer().equals(model.getCurrPlayer())
                    && model.getCurrentPhase() == GamePhaseEnum.DRAW_PHASE) {
                ActionsDTO actions = model.getToDoActions();
                if (actions != null && (actions.getUpDraws() + actions.getDownDraws()) > 0) {
                    indicators = new StringBuilder();
                    if (actions.getUpDraws() > 0)   indicators.append("↑".repeat(actions.getUpDraws()));
                    if (actions.getDownDraws() > 0) indicators.append("↓".repeat(actions.getDownDraws()));
                    if (t.getFoodAmount() > 0) indicators.append(" C: ").append(t.getFoodAmount());
                }
            }
            String extraStr = !indicators.isEmpty() ? " " + indicators.toString().trim() : "";

            String right = extraStr + "] ";
            printAt(row, col, right, maxWidth - (col-startCol));
            col += displayWidth(right);
        }
        row++;

        printAt(row++, startCol, "*** LISTA INFERIORE ***", maxWidth);
        for (int i = 0; i < model.getLowerList().size(); i += 2) {
            CardDTO left = model.getLowerList().get(i);
            printAt(row, startCol, buildCardLabel(left), half, getCardJlineColor(left.getType().toString()));
            if (i + 1 < model.getLowerList().size()) {
                CardDTO right = model.getLowerList().get(i + 1);
                printAt(row, startCol + half, buildCardLabel(right), half, getCardJlineColor(right.getType().toString()));
            }
            row++;
        }
    }

    private void drawBox(int startRow, int startCol, int width, int height) {
        if (width < 2 || height < 2) return;
        int endRow = startRow + height - 1;
        int endCol = startCol + width - 1;
        printCharAt(startRow, startCol, '\u250C');
        printCharAt(startRow, endCol,   '\u2510');
        printCharAt(endRow,   startCol, '\u2514');
        printCharAt(endRow,   endCol,   '\u2518');
        for (int i = startCol + 1; i < endCol; i++) {
            printCharAt(startRow, i, '\u2500');
            printCharAt(endRow,   i, '\u2500');
        }
        for (int i = startRow + 1; i < endRow; i++) {
            printCharAt(i, startCol, '\u2502');
            printCharAt(i, endCol,   '\u2502');
        }
    }

    private void printCharAt(int row, int col, char c, AttributedStyle style) {
        if (row < 0 || row >= screenH || col < 0 || col >= screenW) return;
        screenBuffer[row][col] = c;
        colorBuffer[row][col]  = style;
    }

    private void printCharAt(int row, int col, char c) {
        printCharAt(row, col, c, AttributedStyle.DEFAULT);
    }

    private void drawPlayersPanel(int startRow, int startCol, int maxWidth) {
        class Etichetta {
            final String testo, tipo;
            Etichetta(String testo, String tipo) { this.testo = testo; this.tipo = tipo; }
        }

        int row = startRow;
        printAt(row++, startCol, "*** STATO DEI GIOCATORI ***", maxWidth);

        List<PlayerStatsDTO> stats   = model.getPlayerStats();
        List<PlayerDTO>      players = model.getPlayers();

        for (int i = 0; i < stats.size(); i++) {
            Map<String, Integer> grouped = new LinkedHashMap<>();
            List<CardDTO>        singles = new ArrayList<>();
            PlayerStatsDTO s     = stats.get(i);
            AttributedStyle style = getPlayerJlineColor(players.get(i).getColor());

            String statsNick   = s.getNickname();
            String statsSuffix = String.format(": PP:%d | Cibo:%d | Stelle:%d",
                    s.getPPs(), s.getnFood(), s.getnStars());
            printAt(row,   startCol,                           statsNick,   maxWidth,                         style);
            printAt(row++, startCol + displayWidth(statsNick), statsSuffix, maxWidth - displayWidth(statsNick));

            printAt(row++, startCol,
                    String.format("   Sconti -> Edifici:%d | Cibo:%d",
                            s.getTotBuildDisc(), s.getFoodDiscount()),
                    maxWidth);

            if (i < players.size()) {
                for (CardDTO c : players.get(i).getMyCharacters()) {
                    String tipo = c.getType().toString();
                    if (isGroupable(tipo)) grouped.merge(tipo, 1, Integer::sum);
                    else                   singles.add(c);
                }
                singles.addAll(players.get(i).getMyBuildings());
            }

            List<Etichetta> labels = new ArrayList<>();
            grouped.forEach((tipo, amount) -> labels.add(new Etichetta(amount + "x " + tipo, tipo)));
            for (CardDTO card : singles)
                labels.add(new Etichetta(buildCardLabel(card), card.getType().toString()));

            if (labels.isEmpty()) {
                printAt(row++, startCol, "Carte: Nessuna", maxWidth);
            } else {
                printAt(row, startCol, "Carte: ", maxWidth);
                int currentCol = startCol + 7;
                for (int j = 0; j < labels.size(); j++) {
                    Etichetta e     = labels.get(j);
                    String sep      = (j == labels.size() - 1) ? "" : ", ";
                    int space       = displayWidth(e.testo) + displayWidth(sep);
                    if ((currentCol - startCol) + space > maxWidth - 2) {
                        row++;
                        currentCol = startCol + 7;
                    }
                    int remTesto = maxWidth - (currentCol - startCol);
                    printAt(row, currentCol, e.testo, remTesto, getCardJlineColor(e.tipo));
                    currentCol += displayWidth(e.testo);
                    if (!sep.isEmpty()) {
                        int remSep = maxWidth - (currentCol - startCol);
                        printAt(row, currentCol, sep, remSep);
                        currentCol += displayWidth(sep);
                    }
                }
                row++;
            }
            row++;
        }
    }




    private void printAt(int row, int col, String text, int maxWidth, AttributedStyle style) {
        if (maxWidth <= 0 || text == null || text.isEmpty()) return;
        if (row < 0 || row >= screenH || col < 0 || col >= screenW) return;

        int allowed = maxWidth - 1;

        String out;
        if (displayWidth(text) <= allowed) {
            out = text;
        } else if (allowed <= 3) {
            StringBuilder sb = new StringBuilder();
            int used = 0;
            for (int i = 0; i < text.length(); ) {
                int cp = text.codePointAt(i);
                int w  = cpWidth(cp);
                if (used + w > allowed) break;
                sb.appendCodePoint(cp);
                used += w;
                i += Character.charCount(cp);
            }
            out = sb.toString();
        } else {
            StringBuilder sb = new StringBuilder();
            int used = 0;
            int limit = allowed - 3;
            for (int i = 0; i < text.length(); ) {
                int cp = text.codePointAt(i);
                int w  = cpWidth(cp);
                if (used + w > limit) break;
                sb.appendCodePoint(cp);
                used += w;
                i += Character.charCount(cp);
            }
            out = sb + "...";
        }

        int curCol = col;
        int maxCol = Math.min(col + maxWidth - 1, screenW);
        for (int i = 0; i < out.length(); ) {
            int cp = out.codePointAt(i);
            int w  = cpWidth(cp);
            if (curCol + w > maxCol) break;

            if (Character.charCount(cp) == 2) {
                screenBuffer[row][curCol] = out.charAt(i);
                colorBuffer[row][curCol]  = style;
                if (curCol + 1 < maxCol) {
                    screenBuffer[row][curCol + 1] = out.charAt(i + 1);
                    colorBuffer[row][curCol + 1]  = style;
                }
            } else {
                screenBuffer[row][curCol] = (char) cp;
                colorBuffer[row][curCol]  = style;
                if (w == 2 && curCol + 1 < maxCol) {
                    screenBuffer[row][curCol + 1] = ' ';
                    colorBuffer[row][curCol + 1]  = style;
                }
            }
            curCol += w;
            i += Character.charCount(cp);
        }
    }




    private void printAt(int row, int col, String text, int maxWidth) {
        printAt(row, col, text, maxWidth, AttributedStyle.DEFAULT);
    }


    @Override
    public void showBoard() {
        redrawScreen();
    }

    @Override
    public void onMoveUpdate(TileDTO tile, String currPlayer) {
        redrawCenterBoard();
        if (model.getNickname().equals(currPlayer))
            log("Ti sei mossə alla tile: " + (char) ('A' + tile.getId()));
        else
            log(currPlayer + " si e' mossə alla tile: " + (char) ('A' + tile.getId()));
    }

    @Override
    public void onCurrPlayerUpdate(String nickname) {
        clearLogs();
        redrawQueuePanel();
        if (model.getNickname().equals(nickname)) {
            log(">>TOCCA A TE!<<");
            log("Digitare 'help' per ottenere la lista completa dei comandi utilizzabili.");
        }
    }

    @Override
    public void onPhaseUpdate(PhaseDTO phaseDTO) {
        redrawQueuePanel();
    }

    @Override
    public void onDrawUpdate(CardDTO c, String nickname) {
        redrawCenterBoard();
        if (!model.getNickname().equals(nickname))
            log(nickname + " ha pescato la carta: " + c.getId());
    }

    @Override
    public void onEvent(EventDTO events, List<PlayerStatsDTO> statsBefore) {
        try {
            if(!events.isEmpty()) {
                EventScreen screen = new EventScreen(terminal, events, statsBefore, model.getPlayers());
                screen.display();
            }
        } catch (IOException | InterruptedException e) {
            log("Errore nella visualizzazione degli eventi: " + e.getMessage());
        } finally {
            redrawScreen();
        }
    }

    @Override
    public void onReturnToQueue(TileDTO tileDTO, PlayerStatsDTO playerStatsDTO) {
        redrawQueuePanel();
        redrawCenterBoard();
        redrawPlayersPanel();
        showCompletedDraw();
    }

    @Override
    public void onChangeAge(int age) {
        redrawQueuePanel();
        redrawCenterBoard();
        log("E' cambiata l'era! Adesso siamo nell'era: " + age);
    }

    @Override
    public void onStatsUpdate(PlayerStatsDTO stats) {
        model.onStatsUpdate(stats);
        redrawPlayersPanel();
    }

    @Override
    public void onStatusUpdate(PlayerStatusDTO status) {
        model.onStatusUpdate(status);
    }

    @Override
    public void showDrawable() {
        redrawCenterBoard();
        ActionsDTO a = model.getToDoActions();
        if (a.getUpDraws() > 0)
            log(a.getUpDraws() + " pescate rimanenti dalla fila superiore.");
        if (a.getDownDraws() > 0)
            log(a.getDownDraws() + " pescate rimanenti dalla fila inferiore.");
        if ((a.getDownDraws() + a.getUpDraws()) != 0) {
            if (a.isOptionalFlag())
                log("Hai la possibilita' di saltare la fase di pesca.");
            else
                log("Non hai la possibilita' di saltare la fase di pesca.");
        }
    }

    public void showCompletedDraw(){
        log("Hai completato il turno di pesca!");
    }

    @Override
    public void notifySkip(String nickname) {
        if (model.getNickname().equals(nickname)) log("Hai saltato il turno.");
        else log(nickname + " ha saltato il turno.");
    }


    @Override
    public VirtualModel quit() {
        if (terminal != null) {
            terminal.puts(InfoCmp.Capability.clear_screen);
            terminal.puts(InfoCmp.Capability.cursor_home);
            terminal.flush();
        }
        synchronized (logs) { logs.clear(); }
        if (model != null) model = new VirtualModel(model.getNickname());
        layoutValid = false;
        redrawScreen();
        return model;
    }

    @Override
    public void onQuit(String reason) {
        redrawScreen();
        log(reason);
        log("Digitare create <numeroPersone> per creare una nuova partita o join per visualizzare le partite disponibili.");
    }

    @Override
    public void onServerCrash() {
        log("Il server è crashato. Riavviare e riconnettersi ad esso per riprendere la partita.");
    }

    @Override
    public void exit() {
        log("Disconnessione in corso...");
        try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        if (terminal != null) {
            terminal.puts(InfoCmp.Capability.exit_ca_mode);
            terminal.puts(InfoCmp.Capability.cursor_visible);
            terminal.flush();
            try { terminal.close(); } catch (IOException ignored) {}
        }
        System.exit(0);
    }

    public void showStatusScreen() {
        try {
            StatusScreen statusScreen = new StatusScreen(terminal, model.getPlayerStatuses(), model.getPlayers());
            statusScreen.display();
            redrawScreen();
        } catch (IOException e) {
            log("Errore nell'apertura della schermata status: " + e.getMessage());
        }
    }

    @Override
    public void onGameEnding(List<PlayerStatsDTO> stats, int rankingPos, int globalPos) {
        model.updateAllStats(stats);
        redrawScreen();
        try {
            EndGameScreen endGameScreen = new EndGameScreen(terminal, stats, rankingPos, globalPos);
            endGameScreen.display();
            redrawScreen();
        } catch (IOException e) {
            clearLogs();
            log("==============================");
            log("      PARTITA TERMINATA!      ");
            log("==============================");
            log("Ti sei classificato al posto numero: " + rankingPos);
            log("Classifica finale:");
            stats.stream()
                    .sorted(Comparator.comparing(PlayerStatsDTO::getPPs, Comparator.reverseOrder())
                            .thenComparing(PlayerStatsDTO::getnFood, Comparator.reverseOrder()))
                    .forEach(s -> log("- " + s.getNickname() + ": " + s.getPPs() + " Punti, " + s.getnFood() + " Cibo"));
        }
    }

    @Override
    public void showRanking(Map<String, Integer> ranks) {
        try {
            RankingScreen lbScreen = new RankingScreen(terminal, ranks, model.getNumPlayers());
            lbScreen.display();
            redrawScreen();
        } catch (IOException e) {
            log("Errore nell'apertura della classifica: " + e.getMessage());
        }
    }

    @Override
    public void displayLobbies(Map<Integer, List<LobbyDTO>> lobbies) {
        if (lobbies == null || lobbies.isEmpty() || lobbies.values().stream().allMatch(List::isEmpty)) {
            log("Nessuna partita trovata al momento.");
            return;
        }
        log("=== LISTA PARTITE DISPONIBILI ===");
        lobbies.forEach((capacity, list) -> {
            if (!list.isEmpty()) {
                log("Partite per " + capacity + " giocatori:");
                list.forEach(l -> log(" -> ID: " + l.getId() + " | Giocatori: " + l.getNicknames().size() + "/" + capacity));
            }
        });
        log("Usa il comando 'choose <id>' per entrare in una partita.");
    }

    @Override
    public void showLeaderboard(Map<PlayerDTO, Integer> ranks) {}

    @Override
    public void printError(Exception e) { log("ERRORE: " + e.getMessage()); }

    @Override
    public void onLogin(String nickname) {
        clearLogs();
        log("Login effettuato come " + nickname);
        log("Digitare create <numeroPersone> per creare una nuova partita o join per visualizzare le partite disponibili.");
    }

    @Override
    public void info(int cardId) {
        try {
            CardDTO card = model.findCardById(cardId);
            List<String> details = CardInfoHelper.getFormattedDetails(card);
            AttributedStyle cardColor = getCardJlineColor(card.getType().toString());
            for (String s : details) logColored(s, cardColor);
        } catch (InvalidCardException e) {
            printError(e);
        }
    }

    @Override
    public void onCreate(int id) {
        clearLogs();
        log("Partita creata con ID: " + id);
    }

    @Override
    public void onJoin(int id) {
        clearLogs();
        log("Ti sei unito alla partita con ID: " + id);
    }

    @Override
    public void reconnect(int matchId) {
        clearLogs();
        log("Ti sei riunito alla partita con ID: " + matchId + "\nIn attesa degli altri giocatori.");
    }

    private void printLogo() {
        String logo =
                """
                                                                                                                                                                               \s
                                                                                                                                                                               \s
                                                                                                                                                                               \s
                                                                                                                                                                               \s
                                                                 @@@@@@@@@@@@@@@@                                                                                              \s
                                                  .@@@           @@=                     @@@@@@@@@                      @@@@@@@@@@                      @@@@@@@@@              \s
                            @@@                   @@@@           @@@                    @@@      %@                %@@:            @@@                .@@@      @@             \s
                            @@@                  @@@@@           @@@                   @@@+       @@            @@@                   @@*             @@@:       @@            \s
                            @@@                 @@ @@@          -@@@-                  @@@@                   @@+                       @@@@          @@@@                     \s
                           :@@@-               %@  @@@@         @@@@@                  @@@@=                 @@                          @@@@@        @@@@.                    \s
                           @@@@@              .@.  @@@@         @@@@@                  @@@@@*               @:                            @@@@@       @@@@@:                   \s
                           @@@@#@             @%   @@@@         @@@@@                   @@@@@@             @+                              @@@@@       @@@@@@                  \s
                           @@@@ @%           @@    @@@@-        @@@@@                   =@@@@@@           @@                                @@@@@      %@@@@@@                 \s
                          @@@@@  @          @@     @@@@@        @@@@@-                   @@@@@@@@         @                                  @@@@       @@@@@@@@               \s
                          @@@@@  =@        @@      @@@@@        @@@@@@                    @@@@@@@@       .@                                  @@@@        @@@@@@@@              \s
                          @@@@@   @@      .@       @@@@@        @@@@@@                     @@@@@@@@      @@                                  @@@@         @@@@@@@@             \s
                          @@@@@    @      @:       @@@@@@       @@@@@@                      @@@@@@@@     =@                                 @@@@@          @@@@@@@@            \s
                         =@@@@@    @@    @@        %@@@@@       @@@@@@                       @@@@@@@@     @%                                @@@@.           @@@@@@@@           \s
                         %@@@@@     @@  @@          @@@@@@      @@@@@@@@@@@                   @@@@@@@@    @@                               @@@@@             @@@@@@@@          \s
                         @@@@@@      @@ @@          @@@@@@      @@@@@@.                        @@@@@@@@    @@                             @@@@*               @@@@@@@@         \s
                         @@@@@@       @@@           @@@@@@=     @@@@@@                         @@@@@@@*     @@                           @@@@                 @@@@@@@.         \s
                         @@@@@@        @@           @@@@@@@     @@@@@@                          @@@@@@       #@@                       @@@@.                  :@@@@@@          \s
                         @@@@@@                     .@@@@@@     @@@@@@               @@         @@@@@          #@@:                 :@@@@           @@         @@@@@           \s
                         @@@@@@                      @@@@@      @@@@@*                @@       @@@@-              @@@@           @@@@@.              @@       @@@@.            \s
                         @@@@                        @@@        @@@@@.#%%%-            @@@@@@@@@@                     %@@@@@@@@@@@:                   @@@@@@@@@@               \s
                                                                    +@@@@@@@@@@@@@                                                                                             \s
                        """;
        terminal.writer().print("\033[3J");
        terminal.puts(InfoCmp.Capability.clear_screen);
        terminal.writer().println(logo);
        terminal.writer().flush();
    }

    public void displayHelpMessage() {
        try {
            HelpScreen helpScreen = new HelpScreen(terminal);
            helpScreen.display();
            redrawScreen();
        } catch (IOException e) {
            log("Errore nell'apertura della schermata di help: " + e.getMessage());
        }
    }

    private AttributedStyle getNicknameStyle(String nickname) {
        for (PlayerDTO p : model.getPlayers())
            if (p.getNickname().equals(nickname))
                return getPlayerJlineColor(p.getColor());
        return AttributedStyle.DEFAULT;
    }

    private static int cpWidth(int cp) {
        if (cp < 0x1100) return 1;
        // CJK and East-Asian wide ranges (below U+1F000)
        if ((cp <= 0x115F)   // Hangul Jamo
                || (cp >= 0x2E80 && cp <= 0x303E)   // CJK Radicals / Kangxi
                || (cp >= 0x3040 && cp <= 0x33FF)   // Hiragana, Katakana, CJK compat
                || (cp >= 0x3400 && cp <= 0x4DBF)   // CJK Ext-A
                || (cp >= 0x4E00 && cp <= 0x9FFF)   // CJK Unified
                || (cp >= 0xA000 && cp <= 0xA4CF)   // Yi
                || (cp >= 0xAC00 && cp <= 0xD7AF)   // Hangul Syllables
                || (cp >= 0xF900 && cp <= 0xFAFF)   // CJK Compatibility Ideographs
                || (cp >= 0xFE10 && cp <= 0xFE1F)   // Vertical forms
                || (cp >= 0xFE30 && cp <= 0xFE4F)   // CJK Compatibility Forms
                || (cp >= 0xFF00 && cp <= 0xFF60)   // Fullwidth Latin
                || (cp >= 0xFFE0 && cp <= 0xFFE6))  // Fullwidth Signs
            return 2;
        // Everything in the SMP (U+1F000+) including all emoji
        if (cp >= 0x1F000) return 2;
        return 1;
    }


    private static int displayWidth(String s) {
        if (s == null) return 0;
        int w = 0;
        for (int i = 0; i < s.length(); ) {
            int cp = s.codePointAt(i);
            w += cpWidth(cp);
            i += Character.charCount(cp);
        }
        return w;
    }


    public VirtualModel getModel() { return model; }

    public void setClient(Client client) { this.client = client; }

    private String buildCardLabel(CardDTO card) {
        String tipo = card.getType().toString();
        StringBuilder label = new StringBuilder("[ID: ").append(card.getId()).append("] ").append(tipo);
        CardData data = CardRegistry.getCard(card.getId());
        if (data != null) {
            switch (tipo.toUpperCase()) {
                case "BUILDING" -> {
                    int cost = data.getCost();
                    if (cost > 0) label.append(" (Cost: ").append(cost).append(")");
                }
                case "CRAFTER" -> {
                    CrafterSymbolEnum sym = data.getSymbol();
                    if (sym != null) label.append(" ").append(CrafterSymbolMapper.getSymbol(sym));
                }
                case "HUNTER" -> {
                    if (data.isMark()) label.append(" ⚑ ");
                }
                case "BUILDER" -> {
                    int foodDiscount = data.getFoodDiscount();
                    int pp = data.getPp();
                    if (pp > 0 || foodDiscount > 0) {
                        label.append(" (");
                        if (foodDiscount > 0) label.append("C: ").append(foodDiscount);
                        if (foodDiscount > 0 && pp > 0) label.append(" | ");
                        if (pp > 0) label.append("PP: ").append(pp);
                        label.append(")");
                    }
                }
            }
        }
        return label.toString();
    }

    private static class LogEntry {
        final String message;
        final AttributedStyle style;
        LogEntry(String message, AttributedStyle style) {
            this.message = message;
            this.style   = style;
        }
    }
}