package it.polimi.ingsw.client.TUI;

import it.polimi.ingsw.client.Client;
import it.polimi.ingsw.client.UserInterface;
import it.polimi.ingsw.client.VirtualModel;
import it.polimi.ingsw.client.CommandParser;
import it.polimi.ingsw.client.commands.Command;
import it.polimi.ingsw.exceptions.InvalidCardException;
import it.polimi.ingsw.exceptions.InvalidTimingException;
import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.network.dto.*;
import org.jline.reader.*;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.jline.utils.InfoCmp;

import java.io.IOException;
import java.util.*;

import static it.polimi.ingsw.client.TUI.CardColorMapper.getCardJlineColor;
import static it.polimi.ingsw.client.TUI.TUIColorMapper.getPlayerJlineColor;

public class ViewTUI implements UserInterface {

    VirtualModel model;
    private LineReader reader;
    private final Client client;
    private Terminal terminal;

    private static final int MAX_LOGS = 8;
    private final List<String> logs = new ArrayList<>();

    private char[][] screenBuffer;
    private AttributedStyle[][] colorBuffer;
    private int screenW, screenH;
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
        redrawScreen();

        while (true) {
            try {
                String input = reader.readLine("Comando> ").trim();

                if (input.isEmpty()){
                    redrawScreen();
                    continue;
                }
                
                Command command = CommandParser.parse(input, client);
                if (command != null) {
                    if(command.shouldClearLogs())
                        clearLogs();
                    command.execute(client);
                } else {
                    log("Comando non riconosciuto. Usa: move <n>, create <n>, join, choose <id>, draw <id>, info<id>, skip, help, exit");
                }
            } catch (UserInterruptException | EndOfFileException e) {
                System.exit(0);
            } catch (Exception e) {
                log("Errore: " + e.getMessage());
            }

        }
    }

    private synchronized void clearLogs() {
        logs.clear();
    }

    private synchronized void log(String message) {
        logs.add(message);
        if (logs.size() > MAX_LOGS) logs.removeFirst();
        redrawScreen();
    }

    private synchronized void redrawScreen() {
        terminal.writer().print("\033[3J");
        if (terminal == null || reader == null)
            return;

        screenW = terminal.getWidth();
        screenH = terminal.getHeight() - 1;

        int reservedBottom = MAX_LOGS + 2;
        int logStart = screenH - MAX_LOGS;
        int sepStart = logStart - 1;

        int startRow = 3;
        int panelHeight = sepStart - startRow;

        if (screenW < 20 || screenH < 10) return;


        screenBuffer = new char[screenH][screenW];
        for (int i = 0; i < screenH; i++) {
            Arrays.fill(screenBuffer[i], ' ');
        }
        colorBuffer = new AttributedStyle[screenH][screenW];
            for (int r = 0; r < screenH; r++) {
                for (int c = 0; c < screenW; c++) {
                    colorBuffer[r][c] = AttributedStyle.DEFAULT;
                }
        }


        String banner = "*** MESOS ***";
        int bannerCol = Math.max(0, (screenW - banner.length()) / 2);
        printAt(1, bannerCol, banner, screenW);
        if (model != null && model.getQueue() != null && !model.getQueue().isEmpty()) {
            int leftSize   = screenW / 6;
            int centerSize = screenW / 2;
            int rightSize  = screenW - leftSize - centerSize;
            try {
                drawBox(startRow, 0, leftSize - 1, panelHeight);
                drawBox(startRow, leftSize, centerSize - 1, panelHeight);
                drawBox(startRow, leftSize + centerSize, rightSize - 1, panelHeight);

                drawQueuePanel  (startRow + 1, 1, leftSize - 3);
                drawCenterBoard (startRow + 1, leftSize + 1, centerSize - 3);
                drawPlayersPanel(startRow + 1, leftSize + centerSize + 1, rightSize - 3);
            } catch (Exception ignored) {}
        } else {
            String waitMsg = "In attesa dell'inizio della partita...";
            printAt(screenH / 2, (screenW - waitMsg.length()) / 2, waitMsg, screenW);
        }


        printAt(sepStart, 0, "─".repeat(screenW), screenW);
        for (int i = 0; i < logs.size(); i++) {
            if(logStart + i < screenH)
                printAt(logStart + i, 0, logs.get(i), screenW);
        }
        flushBuffers();

        terminal.puts(InfoCmp.Capability.cursor_address, terminal.getHeight() - 1, 0);
        terminal.puts(InfoCmp.Capability.clr_eol);
        terminal.flush();

        try {
            if(reader != null) {
                reader.callWidget(LineReader.REDRAW_LINE);
                reader.callWidget(LineReader.REDISPLAY);
            }
        } catch (IllegalStateException ignored) {}

        terminal.writer().flush();
    }
    private void flushBuffers(){
        terminal.puts(InfoCmp.Capability.cursor_home);
        AttributedStringBuilder asb = new AttributedStringBuilder();
        for(int i = 0; i < screenH; i++){
            for(int j = 0; j < screenW - 1; j++){
                asb.style(colorBuffer[i][j]);
                asb.append(screenBuffer[i][j]);
            }
            if(i < screenH - 1)
                asb.append("\n");
        }
        asb.style(AttributedStyle.DEFAULT);
        terminal.writer().print(asb.toAnsi());
        terminal.puts(InfoCmp.Capability.clr_eos);
        terminal.writer().flush();
    }

    private void printAt(int row, int col, String text, int maxWidth, AttributedStyle style) {
        if (maxWidth <= 0 || text == null || text.isEmpty()) return;
        if (row < 0 || row >= screenH || col < 0 || col >= screenW) return;

        int allowed = maxWidth - 1;
        String out;
        if (allowed <= 3) {
            out = text.substring(0, Math.min(text.length(), allowed));
        } else if (text.length() > allowed) {
            out = text.substring(0, allowed - 3) + "...";
        } else {
            out = text;
        }

        for (int i = 0; i < out.length(); i++) {
            if (col + i < screenW) {
                screenBuffer[row][col + i] = out.charAt(i);
                colorBuffer[row][col + i] = style;
            }
        }
    }
    private void printAt(int row, int col, String text, int maxWidth) {
        printAt(row, col, text, maxWidth, AttributedStyle.DEFAULT);
    }

    private void drawQueuePanel(int startRow, int startCol, int maxWidth) {
        int row = startRow;
        printAt(row++, startCol, "ORDINE TURNO", maxWidth);
        printAt(row++, startCol, "---------", maxWidth);
        List<TileDTO> queue = model.getQueue();
        for (int j = 0; j < queue.size(); j++) {
            TileDTO tile = queue.get(j);
            String label = (j + 1) + ". " + (tile.isOccupied() ? tile.getPlayer() : "-");
            printAt(row++, startCol, label, maxWidth);
        }
        printAt(row++, startCol, "---------", maxWidth);
        row++;
        printAt(row++, startCol, "Turno corrente: " + model.getCurrTurn(), maxWidth);
        printAt(row++, startCol, "Giocatore corrente:", maxWidth);
        printAt(row++, startCol, model.getCurrPlayer(), maxWidth);
    }

    private void drawCenterBoard(int startRow, int startCol, int maxWidth) {
        int row = startRow;

        printAt(row++, startCol, "*** LISTA SUPERIORE ***", maxWidth);
        int half = maxWidth / 2;
        for (int i = 0; i < model.getUpperList().size(); i += 2) {
            printAt(row, startCol, "[ID: " + model.getUpperList().get(i).getId() + "] " + model.getUpperList().get(i).getType(), half, getCardJlineColor(model.getUpperList().get(i).getType().toString()));
            if(i + 1 < model.getUpperList().size()){
                printAt(row, startCol + half, "[ID: " + model.getUpperList().get(i+1).getId() + "] " + model.getUpperList().get(i+1).getType(), half, getCardJlineColor(model.getUpperList().get(i+1).getType().toString()));
            }
            row++;
        }
        row++;

        printAt(row++, startCol, "*** TRACCIATO DELLE OFFERTE ***", maxWidth);
        List<TileDTO> board = model.getBoard();
        for (int i = 0; i < board.size(); i++) {
            TileDTO t = board.get(i);
            String label = (char)('1'+ i) + ": " + (t.isOccupied() ? t.getPlayer() : "Vuota");
            printAt(row++, startCol, label, maxWidth);
        }
        row++;

        printAt(row++, startCol, "*** LISTA INFERIORE ***", maxWidth);
        for (int i = 0; i < model.getLowerList().size(); i += 2) {
            printAt(row, startCol, "[ID: " + model.getLowerList().get(i).getId() + "] " + model.getLowerList().get(i).getType(), half, getCardJlineColor(model.getLowerList().get(i).getType().toString()));
            if(i + 1 < model.getLowerList().size()){
                printAt(row, startCol + half, "[ID: " + model.getLowerList().get(i+1).getId() + "] " + model.getLowerList().get(i+1).getType(), half, getCardJlineColor(model.getLowerList().get(i+1).getType().toString()));
            }
            row++;
        }
    }

    private void drawBox(int startRow, int startCol, int width, int height) {
        if (width < 2 || height < 2) return;
        int endRow = startRow + height - 1;
        int endCol = startCol + width - 1;
        printCharAt(startRow, startCol, '\u250C');
        printCharAt(startRow, endCol, '\u2510');
        printCharAt(endRow, startCol, '\u2514');
        printCharAt(endRow, endCol, '\u2518');
        for(int i = startCol + 1; i < endCol; i++){
            printCharAt(startRow, i, '\u2500');
            printCharAt(endRow, i, '\u2500');
        }
        for(int i = startRow + 1; i < endRow; i++){
            printCharAt(i, startCol, '\u2502');
            printCharAt(i, endCol, '\u2502');
        }
    }

    private void printCharAt(int row, int col, char c, AttributedStyle style) {
        if(row < 0 || row >= screenH || col < 0 || col >= screenW) return;
        screenBuffer[row][col] = c;
        colorBuffer[row][col] = style;
    }

    private void printCharAt(int row, int col, char c) {
        printCharAt(row, col, c, AttributedStyle.DEFAULT);
    }
    private void drawPlayersPanel(int startRow, int startCol, int maxWidth) {
        class Etichetta {
            String testo;
            String tipo;

            Etichetta(String testo, String tipo) {
                this.testo = testo;
                this.tipo = tipo;
            }
        }
        int row = startRow;
        printAt(row++, startCol, "*** STATO DEI GIOCATORI ***", maxWidth);

        List<PlayerStatsDTO> stats = model.getPlayerStats();
        List<PlayerDTO> players = model.getPlayers();

        for (int i = 0; i < stats.size(); i++) {
            Map<String, Integer> grouped = new LinkedHashMap<>();
            List<CardDTO> singles = new ArrayList<>();
            PlayerStatsDTO s = stats.get(i);
            AttributedStyle style = getPlayerJlineColor(players.get(i).getColor());
            String statsLine = String.format("%s: Punti: %d | Cibo: %d | Stelle: %d", s.getNickname(), s.getPPs(), s.getnFood(), s.getnStars());
            printAt(row++, startCol, statsLine, maxWidth, style);
            if (i < players.size()) {
                PlayerDTO p = players.get(i);
                for (int j = 0; j < model.getPlayers().get(i).getMyCharacters().size(); j++) {
                    String tipo = model.getPlayers().get(i).getMyCharacters().get(j).getType().toString();
                    if (isGroupable(tipo)) {
                        grouped.put(tipo, grouped.getOrDefault(tipo, 0) + 1);
                    } else {
                        singles.add(model.getPlayers().get(i).getMyCharacters().get(j));
                    }
                }
                singles.addAll(model.getPlayers().get(i).getMyBuildings());
            }

            List<Etichetta> labels = new ArrayList<>();
            List<String> groupKeys = new ArrayList<>(grouped.keySet());
            for (int j = 0; j < groupKeys.size(); j++) {
                String tipo = groupKeys.get(j);
                int amount = grouped.get(tipo);
                labels.add(new Etichetta(amount + "x " + tipo, tipo));
            }
            for (int j = 0; j < singles.size(); j++) {
                CardDTO card = singles.get(j);
                String tipo = card.getType().toString();
                labels.add(new Etichetta("[" + card.getId() + "] " + tipo, tipo));
            }
            if (labels.isEmpty()) {
                printAt(row++, startCol, "Carte: Nessuna", maxWidth);
            } else {
                printAt(row, startCol, "Carte: ", maxWidth);
                int currentCol = startCol + 7;
                for (int j = 0; j < labels.size(); j++) {
                    Etichetta e = labels.get(j);
                    boolean isLast = j == labels.size() - 1;
                    String separatore = isLast ? "" : ", ";
                    int space = e.testo.length() + separatore.length();
                    if ((currentCol - startCol) + space > maxWidth - 2) {
                        row++;
                        currentCol = startCol + 7;
                    }
                    AttributedStyle colour = CardColorMapper.getCardJlineColor((e.tipo));
                    printAt(row, currentCol, e.testo, maxWidth, colour);
                    currentCol += e.testo.length();
                    if (!separatore.isEmpty()) {
                        printAt(row, currentCol, separatore, maxWidth);
                        currentCol += separatore.length();

                    }
                }
                row++;
            }
            row++;
        }
    }

    @Override
    public void showBoard() { redrawScreen(); }

    @Override
    public void onMoveUpdate(TileDTO tile, String currPlayer) {
        if (model.getNickname().equals(currPlayer))
            log("Ti sei mossə alla tile: " + (char)('A' + tile.getId()));
        else
            log(currPlayer + " si e' mossə alla tile: " + (char)('A' + tile.getId()));
    }

    @Override
    public void onCurrPlayerUpdate(String nickname) {
        if (model.getNickname().equals(nickname)) log(">>TOCCA A TE!<<");
    }

    @Override
    public void onPhaseUpdate(PhaseDTO phaseDTO) { log("La fase corrente e': " + phaseDTO.getPhase()); }

    @Override
    public void onDrawUpdate(CardDTO c, String nickname) {
        if (model.getNickname().equals(nickname)) log("Hai pescato la carta: " + c.getId());
        else log(nickname + " ha pescato la carta: " + c.getId());
    }

    @Override
    public void onEvent(String event){
        log("E' stato eseguito un evento di " + event);
    }

    @Override
    public VirtualModel quit() {
        if (terminal != null) {
            terminal.puts(InfoCmp.Capability.clear_screen);
            terminal.puts(InfoCmp.Capability.cursor_home);
            terminal.flush();
        }

        synchronized (logs) {
            logs.clear();
        }

        if (model != null) {
            model = new VirtualModel(); // il metodo mi ricrea il modello e lo passa alla classe che chiama la quit così non dovrei avere più problemi.
        }   // IN ALTERNATIVA ANDREBBE RESETTATO TUTTO A LISTA VUOTA NEL VM

        redrawScreen();

        return model;
    }

    @Override
    public void exit() {
        log("Disconnessione in corso...");
        try {
            if(terminal != null) {
                terminal.puts(InfoCmp.Capability.exit_ca_mode);
                terminal.puts(InfoCmp.Capability.cursor_visible);
                terminal.flush();
            }
            Thread.sleep(500);
        } catch (Exception e) {
            System.err.println("Errore durante la disconnessione: " + e.getMessage());
        } finally {
            System.exit(0);
        }
    }

    @Override
    public void onReturnToQueue(TileDTO tileDTO, PlayerStatsDTO playerStatsDTO) {
        log(playerStatsDTO.getNickname() + " e' tornatə in posizione " + tileDTO.getId());
        showBoard();
    }

    @Override
    public void onChangeAge(int age) { log("E' cambiata l'era! Adesso siamo nell'era: " + age); }

    @Override
    public void onStatsUpdate(PlayerStatsDTO stats) {
        log("Nickname: " + stats.getNickname() + "  Food: " + stats.getnFood() + "  PP: " + stats.getPPs() + "  Stelle: " + stats.getnStars());
    }

    @Override
    public void onStatusUpdate(PlayerStatusDTO status) {
        log("Stato del giocatore: huntFlag=" + status.isHuntFlag() + " discountPainter=" + status.isDiscountPainter()
                + " discountCrafter=" + status.isDiscountCrafter() + " discountGatherer=" + status.isDiscountGatherer()
                + " paintFlag=" + status.isPaintFlag() + " extraFlag=" + status.isExtraFlag()
                + " hasProtection=" + status.hasProtection() + " doubleShamanIncome=" + status.hasDoubleShamanIncome());
    }

    public void notifySkip(String nickname) {
        if (model.getNickname().equals(nickname)) log("Hai saltato il turno.");
        else log(nickname + " ha saltato il turno.");
    }

    @Override
    public void showDrawable() {
        ActionsDTO a = model.getToDoActions();
        if (a.getUpDraws() > 0) log(a.getUpDraws() + " pescate rimanenti dalla fila superiore.");
        if (a.getDownDraws() > 0) log(a.getDownDraws() + " pescate rimanenti dalla fila inferiore.");
        if (a.isOptionalFlag()) log("Hai la possibilita' di saltare la fase di pesca.");
        else log("Non hai la possibilita' di saltare la fase di pesca.");
    }

    @Override
    public void onGameEnding(List<PlayerStatsDTO> stats, int rankingPos) {
        log("==============================");
        log("      PARTITA TERMINATA!      ");
        log("==============================");
        log("Ti sei classificato al posto numero: " + rankingPos);
        log("Classifica finale:");
        stats.stream()
                .sorted(Comparator.comparingInt(PlayerStatsDTO::getPPs).reversed().thenComparingInt(PlayerStatsDTO::getnFood).reversed())
                .forEach(s -> log("- " + s.getNickname() + ": " + s.getPPs() + " Punti, " + s.getnFood() + " Cibo"));
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
    public void showLeaderboard(Map<PlayerDTO, Integer> ranks) { log("Classifica da DB"); } // TODO non è stato implementato a fondo

    @Override
    public void printError(Exception e) { log("ERRORE: " + e.getMessage()); }

    @Override
    public void onLogin(String nickname) {
        log("Login effettuato come " + nickname);
        log("Digitare create <numeroPersone> per creare una nuova partita o join per visualizzare le partite disponibili.");
    }

    //possibilità di fare stampe a colori per le info
    @Override
    public void info(int cardId){
        try{
            CardDTO card = model.findCardById(cardId);
            List<String> details = CardInfoHelper.getFormattedDetails(card);
            for(String s : details){
                log(s);
            }
        }catch (InvalidCardException e) {
            printError(e);
        }
    }
    @Override
    public void onCreate(int id){
        log("Partita creata con ID: " + id);
    }

    @Override
    public void onJoin(int id){
        log("Ti sei unito alla partita con ID: " + id);
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

    public void displayHelpMessage(){
        try {
            HelpScreen helpScreen = new HelpScreen(terminal, reader);
            helpScreen.display();
            redrawScreen();
        } catch (IOException e) {
            log("Errore nell'apertura della schermata di help: " + e.getMessage());
        }
    }


    public VirtualModel getModel() {
        return model;
    }
}