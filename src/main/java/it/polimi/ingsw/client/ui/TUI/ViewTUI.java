package it.polimi.ingsw.client.ui.TUI;

import it.polimi.ingsw.client.Client;
import it.polimi.ingsw.client.VirtualModel;
import it.polimi.ingsw.client.commands.Command;
import it.polimi.ingsw.client.data.CardData;
import it.polimi.ingsw.client.data.CardRegistry;
import it.polimi.ingsw.client.ui.TUI.screen.*;
import it.polimi.ingsw.client.ui.TUI.utility.CardInfoHelper;
import it.polimi.ingsw.client.ui.TUI.utility.CommandParser;
import it.polimi.ingsw.client.ui.TUI.utility.CrafterSymbolMapper;
import it.polimi.ingsw.client.ui.TUI.utility.TUICompleter;
import it.polimi.ingsw.client.ui.UserInterface;
import it.polimi.ingsw.enumerations.CrafterSymbolEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.exceptions.InvalidCardException;
import it.polimi.ingsw.network.dto.*;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.Terminal.Signal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.jline.utils.InfoCmp;

import java.io.IOException;
import java.util.*;

import static it.polimi.ingsw.client.ui.TUI.utility.CardColorMapper.getCardJlineColor;
import static it.polimi.ingsw.client.ui.TUI.utility.TUIColorMapper.getPlayerJlineColor;
/**
 * Text-based user interface for the Mesos game, implementing
 * {@link UserInterface}.
 *
 * <p>{@code ViewTUI} renders the entire game state into a raw terminal using
 * <a href="https://github.com/jline/jline3">JLine 3</a>. The screen is divided
 * into three side-by-side panels drawn inside Unicode box characters:</p>
 * <ul>
 *   <li><strong>Left panel</strong> — turn order queue, current phase, era,
 *       round, and active player ({@link #drawQueuePanel}).</li>
 *   <li><strong>Centre panel</strong> — board tiles with totem indicators,
 *       upper and lower drawable-card rows ({@link #drawCenterBoard}).</li>
 *   <li><strong>Right panel</strong> — per-player stats, discounts, and card
 *       inventories ({@link #drawPlayersPanel}).</li>
 * </ul>
 *
 * <p>Below the panels a separator line divides the game area from a scrolling
 * log area that holds up to {@value #MAX_LOGS} entries. All rendering goes
 * through a double-buffered approach ({@link #screenBuffer} /
 * {@link #colorBuffer}) that is flushed atomically via
 * {@link #flushBuffers()}.</p>
 *
 * <h2>Layout tiers</h2>
 * <p>The layout adapts to the terminal width through the {@link LayoutTier}
 * enum: {@code COMPACT} (< 120 cols), {@code STANDARD} (120–179 cols), and
 * {@code LARGE} (≥ 180 cols). Column proportions are recalculated on every
 * {@code WINCH} (window-resize) signal.</p>
 *
 * <h2>Input loop</h2>
 * <p>{@link #start()} runs a blocking readline loop on the calling thread.
 * Raw input is parsed by {@link CommandParser} into {@link Command} objects
 * that are executed directly; unrecognised input is reported in the log
 * area.</p>
 *
 * <h2>Thread safety</h2>
 * <p>All methods that write to the terminal or the internal buffers are
 * {@code synchronized} on {@code this}. Server-callback methods (e.g.
 * {@link #onMoveUpdate}, {@link #onEvent}) may be called from network threads
 * and are safe to invoke from any thread.</p>
 */
public class ViewTUI implements UserInterface {
    /**
     * Responsive breakpoints that control the column-width proportions of the
     * three-panel layout.
     *
     * <p>The tier is selected by {@link #computeLayout()} based on the current
     * terminal width and stored in {@link #currentTier}. A {@code WINCH} signal
     * triggers a full redraw which re-evaluates the tier automatically.</p>
     */
    private enum LayoutTier {
        /** Terminals narrower than 120 columns. Uses wider proportional panels. */
        COMPACT  (0, 120),
        /** Terminals between 120 and 179 columns (default). */
        STANDARD (120, 180),
        /** Terminals 180 columns wide or wider. Allocates more space to the centre panel. */
        LARGE    (180, 9999);
        /** Minimum terminal width (inclusive) for this tier. */
        final int minCols;
        /** Maximum terminal width (exclusive) for this tier. */
        final int maxCols;
        LayoutTier(int minCols, int maxCols) {
            this.minCols = minCols;
            this.maxCols = maxCols;
        }
        /**
         * Returns the tier that matches the given terminal column count.
         *
         * @param cols current terminal width in columns
         * @return the matching {@link LayoutTier}; falls back to
         *         {@code STANDARD} if no range matches
         */
        static LayoutTier from(int cols) {
            for (LayoutTier t : values())
                if (cols >= t.minCols && cols < t.maxCols) return t;
            return STANDARD;
        }
    }
    /** The layout tier currently in use; re-evaluated on every {@link #computeLayout()} call. */
    private LayoutTier currentTier = LayoutTier.STANDARD;
    /** Read-only view of the game state shared with the network layer. */
    VirtualModel model;
    /** JLine line reader that provides readline-style input with tab-completion. */
    private LineReader reader;
    /** Network client used to dispatch player actions to the server. */
    private Client client;
    /** JLine terminal handle; owns the raw-mode lifecycle and the output writer. */
    private Terminal terminal;
    /** Maximum number of log entries kept in {@link #logs} and displayed at the bottom of the screen. */
    private static final int MAX_LOGS = 8;
    /** Scrolling log buffer; newest entries are appended at the tail, oldest are evicted when full. */
    private final List<LogEntry> logs = new ArrayList<>();
    /**
     * Character buffer backing the current frame.
     * Dimensions are {@code [screenH][screenW]}, re-allocated on each
     * {@link #redrawScreen()} call.
     */
    private char[][] screenBuffer;

    /**
     * Colour/style buffer parallel to {@link #screenBuffer}.
     * Each cell holds the {@link AttributedStyle} for the corresponding
     * character, defaulting to {@link AttributedStyle#DEFAULT}.
     */
    private AttributedStyle[][] colorBuffer;

    /** Current terminal width in columns, updated by {@link #computeLayout()}. */
    private int screenW;

    /** Current terminal height in rows (minus one for the prompt), updated by {@link #computeLayout()}. */
    private int screenH;

    /** Row index where the three panel boxes start. */
    private int layoutStartRow;
    /** Height (in rows) of the three panel boxes. */
    private int layoutPanelHeight;
    /** Column width allocated to the left (queue) panel. */
    private int layoutLeftSize;
    /** Column width allocated to the centre (board) panel. */
    private int layoutCenterSize;
    /** Column width allocated to the right (players) panel. */
    private int layoutRightSize;
    /** Row index of the separator line between the panels and the log area. */
    private int layoutSepStart;
    /** Row index of the first log line. */
    private int layoutLogStart;

    /**
     * {@code true} when the last {@link #computeLayout()} call succeeded and
     * the cached layout values are valid for the current terminal dimensions.
     * Partial-redraw methods skip their work and fall back to a full
     * {@link #redrawScreen()} when this flag is {@code false}.
     */
    private boolean layoutValid = false;

    /**
     * Card types whose instances are collapsed into a count label
     * (e.g. {@code "3x PAINTER"}) in the players panel to save horizontal
     * space. Non-groupable types are always listed individually.
     */
    private static final Set<String> GROUPABLE_CHARACTERS = Set.of(
            "PAINTER", "GATHERER", "SHAMAN"
    );

    /**
     * Helper method to identify types of card that can be grouped, in order to
     * save space in the terminal
     * @param type is the type of the card that the method wants to know if it is groupable
     * @return {@code true} if it can be grouped or {@code false} if it can't
     */
    private boolean isGroupable(String type) {
        return type != null && GROUPABLE_CHARACTERS.contains(type.toUpperCase());
    }
    /**
     * Creates a new {@code ViewTUI}, initializes the JLine terminal, and
     * prints the ASCII splash logo.
     *
     * @param model  the shared virtual model populated by the network layer;
     *               must not be {@code null}
     * @param client the network client used to send player commands to the
     *               server; must not be {@code null}
     */
    public ViewTUI(VirtualModel model, Client client) {
        this.model = model;
        this.client = client;
        initJline();
        printLogo();
    }
    /**
     * Initialises the JLine {@link Terminal} and {@link LineReader}.
     *
     * <p>Sets up:</p>
     * <ul>
     *   <li>System properties to prefer Jansi over JNA for ANSI handling.</li>
     *   <li>Alternate-screen mode ({@code enter_ca_mode}) so the game UI does
     *       not contaminate the shell scroll-back buffer.</li>
     *   <li>An invisible cursor for cleaner rendering.</li>
     *   <li>A {@code WINCH} signal handler that calls {@link #redrawScreen()}
     *       whenever the terminal is resized.</li>
     *   <li>A {@link TUICompleter} that provides context-sensitive tab
     *       completion for game commands.</li>
     * </ul>
     * <p>Errors during terminal construction are printed to {@code stderr}; the
     * application can still run in degraded mode without a full terminal.</p>
     */
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
            TUICompleter completer = new TUICompleter(this);
            this.reader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .completer(completer)
                    .option(LineReader.Option.ERASE_LINE_ON_FINISH, true)
                    .build();
        } catch (IOException e) {
            System.err.println("Errore inizializzazione terminale: " + e.getMessage());
        }
    }
    /**
     * Runs the main input loop: prompts for a nickname until login succeeds,
     * then enters a blocking readline loop that parses and executes
     * {@link Command} objects.
     *
     * <p>The loop terminates on {@link UserInterruptException} (Ctrl-C) or
     * {@link EndOfFileException} (Ctrl-D) by calling {@link System#exit(int)}
     * with status {@code 0}. All other exceptions are caught and reported via
     * {@link #log(String)}.</p>
     *
     * <p>This method blocks the calling thread indefinitely and should be
     * invoked from the application's main thread.</p>
     */
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

    /**
     * Reads the current terminal dimensions, selects the appropriate
     * {@link LayoutTier}, and computes all layout metrics stored in the
     * {@code layout*} fields.
     *
     * <p>Returns {@code false} and sets {@link #layoutValid} to {@code false}
     * if the terminal is too small to render a meaningful UI (width &lt; 20 or
     * height &lt; 10). In that case callers should skip rendering.</p>
     *
     * @return {@code true} if the layout was computed successfully;
     *         {@code false} if the terminal is too small
     */
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

    /**
     * Performs a full re-render of the entire screen.
     *
     * <p>Steps performed:</p>
     * <ol>
     *   <li>Pauses the terminal to suppress partial output.</li>
     *   <li>Clears the scrollback buffer ({@code \033[3J}).</li>
     *   <li>Calls {@link #computeLayout()}; aborts if the terminal is too
     *       small.</li>
     *   <li>Allocates fresh {@link #screenBuffer} and {@link #colorBuffer}
     *       arrays filled with spaces and {@link AttributedStyle#DEFAULT}.</li>
     *   <li>Draws the banner, the three panel boxes, and their content.</li>
     *   <li>Renders the separator line and the log entries.</li>
     *   <li>Flushes the buffers to the terminal via {@link #flushBuffers()}
     *       and restores the readline prompt via {@link #redisplayPrompt()}.</li>
     * </ol>
     * <p>If the model is not yet ready (null queue or empty queue), a waiting
     * message is displayed instead of the game panels.</p>
     *
     * <p>This method is {@code synchronized} to prevent interleaved writes
     * from concurrent server-callback threads.</p>
     */
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

    /**
     * Clears a rectangular region of both the character buffer and the color
     * buffer, filling it with spaces and {@link AttributedStyle#DEFAULT}.
     *
     * @param startRow first row of the region (inclusive)
     * @param startCol first column of the region (inclusive)
     * @param rows     number of rows to clear
     * @param cols     number of columns to clear
     */
    private void clearRegion(int startRow, int startCol, int rows, int cols) {
        for (int r = startRow; r < startRow + rows && r < screenH; r++) {
            for (int c = startCol; c < startCol + cols && c < screenW; c++) {
                screenBuffer[r][c] = ' ';
                colorBuffer[r][c]  = AttributedStyle.DEFAULT;
            }
        }
    }

    /**
     * Partially redraws only the left (queue) panel without touching the other
     * two panels or the log area.
     *
     * <p>Falls back to a full {@link #redrawScreen()} if the cached layout is
     * invalid or the screen buffers have not been allocated yet.</p>
     */
    private synchronized void redrawQueuePanel() {
        if (!layoutValid || screenBuffer == null) { redrawScreen(); return; }
        clearRegion(layoutStartRow + 1, 1, layoutPanelHeight - 2, layoutLeftSize - 3);
        drawQueuePanel(layoutStartRow + 1, 1, layoutLeftSize - 3);
        flushBuffers();
        redisplayPrompt();
    }
    /**
     * Partially redraws only the center (board) panel without touching the
     * other two panels or the log area.
     *
     * <p>Falls back to a full {@link #redrawScreen()} if the cached layout is
     * invalid or the screen buffers have not been allocated yet.</p>
     */
    private synchronized void redrawCenterBoard() {
        if (!layoutValid || screenBuffer == null) { redrawScreen(); return; }
        clearRegion(layoutStartRow + 1, layoutLeftSize + 1, layoutPanelHeight - 2, layoutCenterSize - 3);
        drawCenterBoard(layoutStartRow + 1, layoutLeftSize + 1, layoutCenterSize - 3);
        flushBuffers();
        redisplayPrompt();
    }
    /**
     * Partially redraws only the right (players) panel without touching the
     * other two panels or the log area.
     *
     * <p>Falls back to a full {@link #redrawScreen()} if the cached layout is
     * invalid or the screen buffers have not been allocated yet.</p>
     */
    private synchronized void redrawPlayersPanel() {
        if (!layoutValid || screenBuffer == null) { redrawScreen(); return; }
        clearRegion(layoutStartRow + 1, layoutLeftSize + layoutCenterSize + 1, layoutPanelHeight - 2, layoutRightSize - 3);
        drawPlayersPanel(layoutStartRow + 1, layoutLeftSize + layoutCenterSize + 1, layoutRightSize - 3);
        flushBuffers();
        redisplayPrompt();
    }
    /**
     * Redraws only the separator line and the log area without touching the
     * three game panels.
     *
     * <p>Falls back to a full {@link #redrawScreen()} if the cached layout is
     * invalid or the screen buffers have not been allocated yet.</p>
     */
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

    /**
     * Serialises {@link #screenBuffer} and {@link #colorBuffer} into a single
     * ANSI-styled string and writes it to the terminal in one pass.
     *
     * <p>Surrogate pairs (characters outside the Basic Multilingual Plane) are
     * handled by detecting high-surrogate / low-surrogate pairs and emitting
     * them together with the style of the high-surrogate cell. After writing,
     * {@link InfoCmp.Capability#clr_eos} clears any stale content below the
     * last rendered row.</p>
     */
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

    /**
     * Moves the cursor to the last terminal row and asks the JLine
     * {@link LineReader} to repaint the readline prompt and any partial input
     * the user has typed.
     *
     * <p>The {@link IllegalStateException} thrown by JLine when the reader is
     * not in an active read operation is silently ignored.</p>
     */
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

    /**
     * Removes all entries from the log buffer and triggers a log-area redraw.
     */
    private synchronized void clearLogs() {
        logs.clear();
    }
    /**
     * Appends a plain-styled message to the log buffer and redraws the log
     * area. If the buffer is full (size > {@value #MAX_LOGS}), the oldest
     * entry is evicted.
     *
     * @param message the text to display in the log area
     */
    private synchronized void log(String message) {
        logs.add(new LogEntry(message, AttributedStyle.DEFAULT));
        if (logs.size() > MAX_LOGS) logs.removeFirst();
        redrawLogs();
    }
    /**
     * Appends a styled message to the log buffer and redraws the log area.
     * If the buffer is full (size > {@value #MAX_LOGS}), the oldest entry is
     * evicted.
     *
     * @param message the text to display
     * @param color   the {@link AttributedStyle} applied to the text
     */
    private synchronized void logColored(String message, AttributedStyle color) {
        logs.add(new LogEntry(message, color));
        if (logs.size() > MAX_LOGS) logs.removeFirst();
        redrawLogs();
    }

    /**
     * Renders the left panel: the local player's nickname, the turn-order
     * queue (one row per queue slot), the current game phase, round, era, and
     * active player.
     *
     * <p>Player nicknames are coloured with their assigned
     * {@link AttributedStyle} via {@link #getNicknameStyle(String)}.</p>
     *
     * @param startRow first usable row inside the panel box
     * @param startCol first usable column inside the panel box
     * @param maxWidth maximum number of columns available for text
     */
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
    /**
     * Converts a {@link GamePhaseEnum} value to its Italian display label for
     * use in the TUI queue panel.
     *
     * @param currentPhase the phase to convert; {@code null} returns {@code "-"}
     * @return a short Italian label (e.g. {@code "Pesca"}, {@code "Setup"})
     */
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
    /**
     * Renders the centre panel: the upper drawable-card row, the board tiles
     * with totem indicators and draw-arrow counts, and the lower drawable-card
     * row.
     *
     * <p>Each board tile is rendered as {@code [N:<player><arrows>]}.
     * When the local player is on a tile during {@code DRAW_PHASE}, the
     * remaining draw counts from {@link VirtualModel#getToDoActions()} are
     * substituted for the tile's static values.</p>
     *
     * <p>Cards are coloured with the type-specific style returned by
     * {@link it.polimi.ingsw.client.ui.TUI.utility.CardColorMapper#getCardJlineColor(String)}.</p>
     *
     * @param startRow first usable row inside the panel box
     * @param startCol first usable column inside the panel box
     * @param maxWidth maximum number of columns available for text
     */
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
    /**
     * Draws a Unicode box (using box-drawing characters ┌ ┐ └ ┘ ─ │) at the
     * specified position.
     *
     * @param startRow top-left row of the box
     * @param startCol top-left column of the box
     * @param width    outer width of the box in columns (must be ≥ 2)
     * @param height   outer height of the box in rows (must be ≥ 2)
     */
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
    /**
     * Writes a single character with the given style into the screen buffer at
     * the specified position. Out-of-bounds writes are silently ignored.
     *
     * @param row   target row
     * @param col   target column
     * @param c     character to write
     * @param style style to apply
     */
    private void printCharAt(int row, int col, char c, AttributedStyle style) {
        if (row < 0 || row >= screenH || col < 0 || col >= screenW) return;
        screenBuffer[row][col] = c;
        colorBuffer[row][col]  = style;
    }
    /**
     * Writes a single character with the default style into the screen buffer.
     * Equivalent to {@link #printCharAt(int, int, char, AttributedStyle)} with
     * {@link AttributedStyle#DEFAULT}.
     *
     * @param row target row
     * @param col target column
     * @param c   character to write
     */
    private void printCharAt(int row, int col, char c) {
        printCharAt(row, col, c, AttributedStyle.DEFAULT);
    }
    /**
     * Renders the right panel: per-player nickname (colored), stats line
     * (PP, food, stars), discount line, and the card inventory.
     *
     * <p>Cards belonging to {@link #GROUPABLE_CHARACTERS} types are collapsed
     * into a count label (e.g. {@code "3x GATHERER"}) to save space. All other
     * character cards and all building cards are listed individually via
     * {@link #buildCardLabel(CardDTO)}. Labels wrap to the next line when they
     * exceed the available width.</p>
     *
     * @param startRow first usable row inside the panel box
     * @param startCol first usable column inside the panel box
     * @param maxWidth maximum number of columns available for text
     */
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



    /**
     * Writes a string into the screen and color buffers at the given position,
     * truncating to fit within {@code maxWidth} columns.
     *
     * <p>Truncation rules:</p>
     * <ul>
     *   <li>If the display width of {@code text} fits within
     *       {@code maxWidth - 1}, it is written as-is.</li>
     *   <li>If {@code maxWidth ≤ 3}, as many code points as fit are written
     *       without an ellipsis.</li>
     *   <li>Otherwise the string is cut and {@code "..."} (3 columns) is
     *       appended.</li>
     * </ul>
     * <p>Wide characters (CJK, emoji, etc.) and surrogate pairs are handled
     * correctly: a wide character is never split across a boundary.</p>
     *
     * @param row      target row; ignored if out of bounds
     * @param col      starting column; ignored if out of bounds
     * @param text     the string to write; {@code null} or empty is a no-op
     * @param maxWidth maximum number of terminal columns to use (including the
     *                 one reserved as a guard column)
     * @param style    the {@link AttributedStyle} applied to every cell of the
     *                 output
     */
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



    /**
     * Writes a string with {@link AttributedStyle#DEFAULT} style.
     * Convenience overload of {@link #printAt(int, int, String, int, AttributedStyle)}.
     *
     * @param row      target row
     * @param col      starting column
     * @param text     the string to write
     * @param maxWidth maximum number of terminal columns to use
     */
    private void printAt(int row, int col, String text, int maxWidth) {
        printAt(row, col, text, maxWidth, AttributedStyle.DEFAULT);
    }

    /**
     * {@inheritDoc}
     * Triggers a full screen redraw.
     */
    @Override
    public void showBoard() {
        redrawScreen();
    }
    /**
     * {@inheritDoc}
     * Redraws the centre panel and logs a move-notification message. If the
     * moving player is the local player, the message is in the second person;
     * otherwise it names the player.
     *
     * @param tile       the tile the player moved to; the tile label is derived
     *                   from its ID ({@code 'A' + id})
     * @param currPlayer the nickname of the player who moved
     */
    @Override
    public void onMoveUpdate(TileDTO tile, String currPlayer) {
        redrawCenterBoard();
        if (model.getNickname().equals(currPlayer))
            log("Ti sei mossə alla tile: " + (char) ('A' + tile.getId()));
        else
            log(currPlayer + " si e' mossə alla tile: " + (char) ('A' + tile.getId()));
    }
    /**
     * {@inheritDoc}
     * Clears the log area and redraws the queue panel. If the new current
     * player is the local player, a turn-start prompt is logged.
     *
     * @param nickname the nickname of the player whose turn has just started
     */
    @Override
    public void onCurrPlayerUpdate(String nickname) {
        clearLogs();
        redrawQueuePanel();
        if (model.getNickname().equals(nickname)) {
            log(">>TOCCA A TE!<<");
            log("Digitare 'help' per ottenere la lista completa dei comandi utilizzabili.");
        }
    }

    /**
     * {@inheritDoc}
     * Redraws the queue panel to reflect the new phase label.
     *
     * @param phaseDTO DTO carrying the updated {@link GamePhaseEnum}
     */
    @Override
    public void onPhaseUpdate(PhaseDTO phaseDTO) {
        redrawQueuePanel();
    }
    /**
     * {@inheritDoc}
     * Redraws the centre panel. If the drawing player is not the local player,
     * a notification is logged with the card ID.
     *
     * @param c        the card that was drawn
     * @param nickname the nickname of the player who drew the card
     */
    @Override
    public void onDrawUpdate(CardDTO c, String nickname) {
        redrawCenterBoard();
        if (!model.getNickname().equals(nickname))
            log(nickname + " ha pescato la carta: " + c.getId());
    }
    /**
     * {@inheritDoc}
     * Delegates to {@link EventScreen} for a full-screen event display. If the
     * event DTO is empty the call is a no-op. After the screen is dismissed
     * (or on error), a full {@link #redrawScreen()} restores the main layout.
     *
     * @param events      DTO containing the event cards and post-event stats
     * @param statsBefore per-player stats captured before the events were applied
     */
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
    /**
     * {@inheritDoc}
     * Redraws all three panels and logs a draw-completion message via
     * {@link #showCompletedDraw()}.
     *
     * @param tileDTO        the queue tile the player returned to
     * @param playerStatsDTO the returning player's updated stats
     */
    @Override
    public void onReturnToQueue(TileDTO tileDTO, PlayerStatsDTO playerStatsDTO) {
        redrawQueuePanel();
        redrawCenterBoard();
        redrawPlayersPanel();
        showCompletedDraw();
    }
    /**
     * {@inheritDoc}
     * Redraws the queue and center panels and logs an era-change notification.
     *
     * @param age the new era number
     */
    @Override
    public void onChangeAge(int age) {
        redrawQueuePanel();
        redrawCenterBoard();
        log("E' cambiata l'era! Adesso siamo nell'era: " + age);
    }
    /**
     * {@inheritDoc}
     * Forwards the update to the virtual model and redraws the players panel.
     *
     * @param stats the updated stats for a single player
     */
    @Override
    public void onStatsUpdate(PlayerStatsDTO stats) {
        model.onStatsUpdate(stats);
        redrawPlayersPanel();
    }
    /**
     * {@inheritDoc}
     * Forwards the status update to the virtual model; no visual redraw is
     * performed here because status flags are not directly displayed in the
     * main TUI layout (they are shown on demand via {@link #showStatusScreen()}).
     *
     * @param status the updated status for a single player
     */
    @Override
    public void onStatusUpdate(PlayerStatusDTO status) {
        model.onStatusUpdate(status);
    }
    /**
     * {@inheritDoc}
     * Redraws the center panel and logs the number of remaining draw actions
     * available from each row. Also informs the player whether the draw phase
     * can be skipped.
     */
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
    /**
     * Logs a message confirming that the local player has completed their draw
     * turn.
     */
    public void showCompletedDraw(){
        log("Hai completato il turno di pesca!");
    }
    /**
     * {@inheritDoc}
     * Logs a skip notification; uses the second person for the local player
     * and the third person for opponents.
     *
     * @param nickname the player who skipped their draw turn
     */
    @Override
    public void notifySkip(String nickname) {
        if (model.getNickname().equals(nickname)) log("Hai saltato il turno.");
        else log(nickname + " ha saltato il turno.");
    }

    /**
     * {@inheritDoc}
     * Clears the terminal, resets the virtual model, invalidates the layout,
     * and triggers a full redraw. Returns the (now reset) virtual model so the
     * caller can wire it to a fresh {@link Client} instance.
     *
     * @return the reset {@link VirtualModel}
     */
    @Override
    public VirtualModel quit() {
        if (terminal != null) {
            terminal.puts(InfoCmp.Capability.clear_screen);
            terminal.puts(InfoCmp.Capability.cursor_home);
            terminal.flush();
        }
        synchronized (logs) { logs.clear(); }
        if (model != null)
            model.reset();
        layoutValid = false;
        redrawScreen();
        return model;
    }
    /**
     * {@inheritDoc}
     * Redraws the screen, logs the quit reason, and hints at the available
     * post-game commands.
     *
     * @param reason human-readable explanation of why the match ended
     */
    @Override
    public void onQuit(String reason) {
        redrawScreen();
        log(reason);
        log("Digitare create <numeroPersone> per creare una nuova partita o join per visualizzare le partite disponibili.");
    }
    /**
     * {@inheritDoc}
     * Logs a server-crash notification instructing the player to restart and
     * reconnect.
     */
    @Override
    public void onServerCrash() {
        log("Il server è crashato. Riavviare e riconnettersi ad esso per riprendere la partita.");
    }
    /**
     * {@inheritDoc}
     * Logs a disconnection message, waits 500 ms, exits alternate-screen mode,
     * restores cursor visibility, closes the terminal, and calls
     * {@link System#exit(int)} with status {@code 0}.
     */
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
    /**
     * Opens the {@link StatusScreen} full-screen overlay showing the active
     * status flags for all players, then restores the main layout.
     *
     * <p>Errors opening the screen are reported via {@link #log(String)}.</p>
     */
    public void showStatusScreen() {
        try {
            StatusScreen statusScreen = new StatusScreen(terminal, model.getPlayerStatuses(), model.getPlayers());
            statusScreen.display();
            redrawScreen();
        } catch (IOException e) {
            log("Errore nell'apertura della schermata status: " + e.getMessage());
        }
    }
    /**
     * {@inheritDoc}
     * Updates the virtual model with the final stats, redraws the main screen,
     * and opens the {@link EndGameScreen} full-screen overlay. If the overlay
     * cannot be rendered (e.g. terminal too small or I/O error), a compact
     * text summary is printed in the log area instead.
     *
     * @param stats      final per-player stats
     * @param rankingPos the local player's 1-based finishing position
     * @param globalPos  the local player's position in the persistent global
     *                   leaderboard, or {@code -1} if unavailable
     */
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
    /**
     * {@inheritDoc}
     * Opens the {@link RankingScreen} full-screen overlay showing the global
     * leaderboard for the current player count, then restores the main layout.
     *
     * @param ranks map from player nickname to cumulative global points
     */
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
    /**
     * {@inheritDoc}
     * Logs the list of joinable lobbies grouped by player capacity. If no
     * lobbies are available, a "none found" message is logged instead.
     *
     * @param lobbies map from player capacity to the list of open lobbies with
     * that capacity
     */
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
    /** {@inheritDoc} Not implemented in the TUI. */
    @Override
    public void showLeaderboard(Map<PlayerDTO, Integer> ranks) {}
    /**
     * {@inheritDoc}
     * Logs the exception message prefixed with {@code "ERRORE: "}.
     *
     * @param e the exception to report
     */
    @Override
    public void printError(Exception e) { log("ERRORE: " + e.getMessage()); }
    /**
     * {@inheritDoc}
     * Clears the log area and logs a login-confirmation message followed by
     * hints for the next steps.
     *
     * @param nickname the nickname under which the player logged in
     */
    @Override
    public void onLogin(String nickname) {
        clearLogs();
        log("Login effettuato come " + nickname);
        log("Digitare create <numeroPersone> per creare una nuova partita o join per visualizzare le partite disponibili.");
    }
    /**
     * {@inheritDoc}
     * Looks up the card in the virtual model, retrieves its formatted details
     * from {@link CardInfoHelper}, and logs each detail line with the card's
     * type colour.
     *
     * @param cardId the unique identifier of the card to inspect
     */
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
    /**
     * {@inheritDoc}
     * Clears the log area and logs a match-creation confirmation with the
     * assigned match ID.
     *
     * @param id the server-assigned ID of the newly created match
     */
    @Override
    public void onCreate(int id) {
        clearLogs();
        log("Partita creata con ID: " + id);
    }
    /**
     * {@inheritDoc}
     * Clears the log area and logs a join-confirmation message.
     *
     * @param id the ID of the match the player joined
     */
    @Override
    public void onJoin(int id) {
        clearLogs();
        log("Ti sei unito alla partita con ID: " + id);
    }
    /**
     * {@inheritDoc}
     * Clears the log area and logs a reconnection confirmation followed by a
     * waiting message.
     *
     * @param matchId the ID of the match the player reconnected to
     */
    @Override
    public void reconnect(int matchId) {
        clearLogs();
        log("Ti sei riunito alla partita con ID: " + matchId + "\nIn attesa degli altri giocatori.");
    }
    /**
     * Clears the screen and prints the large ASCII art Mesos logo directly to
     * the terminal writer, bypassing the double-buffer. Called once at
     * construction time.
     */
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
    /**
     * Opens the {@link HelpScreen} full-screen overlay listing all available
     * commands, then restores the main layout.
     *
     * <p>Errors opening the screen are reported via {@link #log(String)}.</p>
     */
    public void displayHelpMessage() {
        try {
            HelpScreen helpScreen = new HelpScreen(terminal);
            helpScreen.display();
            redrawScreen();
        } catch (IOException e) {
            log("Errore nell'apertura della schermata di help: " + e.getMessage());
        }
    }
    /**
     * Returns the {@link AttributedStyle} colour assigned to the player with
     * the given nickname, or {@link AttributedStyle#DEFAULT} if the player is
     * not found in the model.
     *
     * @param nickname the player whose colour style is requested
     * @return the JLine colour style for the player, or the default style
     */
    private AttributedStyle getNicknameStyle(String nickname) {
        for (PlayerDTO p : model.getPlayers())
            if (p.getNickname().equals(nickname))
                return getPlayerJlineColor(p.getColor());
        return AttributedStyle.DEFAULT;
    }
    /**
     * Returns the display width (in terminal columns) of the given Unicode
     * code point.
     *
     * <p>East-Asian wide characters (Hangul, CJK, fullwidth Latin, emoji, and
     * all Supplementary Multilingual Plane code points) return {@code 2};
     * all other code points return {@code 1}.</p>
     *
     * @param cp the Unicode code point to measure
     * @return {@code 1} for narrow characters, {@code 2} for wide characters
     */
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

    /**
     * Returns the total display width (in terminal columns) of a string,
     * correctly accounting for wide Unicode characters via {@link #cpWidth(int)}.
     *
     * @param s the string to measure; {@code null} returns {@code 0}
     * @return the number of terminal columns required to display the string
     */
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

    /**
     * Returns the {@link VirtualModel} associated with this view.
     *
     * @return the virtual model; never {@code null} after construction
     */
    public VirtualModel getModel() { return model; }
    /**
     * Replaces the {@link Client} reference used to send player commands.
     * Intended for reconnection scenarios where a new {@code Client} instance
     * is created after a disconnect.
     *
     * @param client the new client instance; must not be {@code null}
     */
    public void setClient(Client client) { this.client = client; }
    /**
     * Builds a compact display label for a card, including its ID, type, and
     * any type-specific attributes:
     * <ul>
     *   <li><strong>BUILDING</strong> — appends the food cost if &gt; 0.</li>
     *   <li><strong>CRAFTER</strong> — appends the crafter symbol via
     *       {@link CrafterSymbolMapper}.</li>
     *   <li><strong>HUNTER</strong> — appends a flag icon (⚑) if the card has
     *       the mark attribute set.</li>
     *   <li><strong>BUILDER</strong> — appends food discount and/or PP value
     *       if either is &gt; 0.</li>
     * </ul>
     *
     * @param card the card DTO to label
     * @return a formatted string such as {@code "[ID: 42] BUILDING (Cost: 3)"}
     */
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

    /**
     * Immutable container for a single log entry, pairing the display text
     * with its {@link AttributedStyle}.
     */
    private static class LogEntry {
        /** The text content of the log message. */
        final String message;
        /** The JLine style applied when rendering this entry. */
        final AttributedStyle style;

        /**
         * Creates a new log entry.
         *
         * @param message the text to display
         * @param style   the style to apply
         */
        LogEntry(String message, AttributedStyle style) {
            this.message = message;
            this.style   = style;
        }
    }
}