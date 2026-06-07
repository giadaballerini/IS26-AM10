package it.polimi.ingsw.client;

import it.polimi.ingsw.client.data.CardRegistry;
import it.polimi.ingsw.client.ui.UserInterface;
import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.exceptions.*;
import it.polimi.ingsw.network.client.ClientToServerActions;
import it.polimi.ingsw.network.dto.ActionsDTO;
import it.polimi.ingsw.network.dto.PlayerStatsDTO;
import it.polimi.ingsw.network.dto.TileDTO;

import java.util.List;

/**
 * Abstract base class for all client implementations.
 *
 * <p>Centralises shared state ({@code matchId}, {@code vm}, {@code ui}) and all
 * game-action validation logic ({@link #login}, {@link #createGame},
 * {@link #joinGame}, {@link #move}, {@link #draw}, {@link #skip},
 * {@link #requestRanking}), eliminating duplication between
 * {@code ClientRmi} and {@code ClientSocket}.
 *
 * <p>Subclasses implement only the transport layer via the protected
 * {@code doXxx} Template Method hooks:
 * {@link #doLogin}, {@link #doCreateGame}, {@link #doJoinGame},
 * {@link #doMove}, {@link #doDraw}, {@link #doSkip}, {@link #doRequestRanking}.
 */
public abstract class Client implements ClientToServerActions {

    protected int matchId = 0;
    protected VirtualModel vm;
    protected UserInterface ui;
    protected volatile boolean gameEnded = false;
    protected boolean lobbiesAvailable = false;

    protected Client(VirtualModel vm) {
        this.vm = vm;
    }

    public boolean isInGame() { return matchId != 0; }

    public VirtualModel getModel() { return vm; }

    public void setUi(UserInterface ui) {
        if (this.ui == null)
            this.ui = ui;
    }

    public String getNickname() { return vm.getNickname(); }

    public boolean hasAvailableLobbies() { return lobbiesAvailable; }

    public void showStatus() {
        if (isInGame())
            ui.showStatusScreen();
        else
            ui.printError(new InvalidTimingException(
                    "Non è possibile richiedere informazioni prima che la partita sia iniziata."));
    }

    public void help() {
        ui.displayHelpMessage();
    }

    public void info(int cardId) {
        if (isInGame())
            ui.info(cardId);
        else
            ui.printError(new InvalidTimingException(
                    "Non è consentito richiedere informazioni prima che la partita sia iniziata."));
    }

    public void start() { ui.start(); }

    @Override
    public boolean login(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            ui.printError(new AlreadyExistingUsernameException("Il nickname non può essere vuoto."));
            return false;
        }
        return doLogin(nickname);
    }

    @Override
    public void createGame(String nickname, int numPlayers) {
        if (numPlayers < 2 || numPlayers > 5) {
            ui.printError(new InvalidLobbySizeException("Il numero di giocatori deve essere tra 2 e 5."));
            return;
        }
        if (isInGame()) {
            ui.printError(new InvalidTimingException("Sei già in una partita."));
            return;
        }
        gameEnded = false;
        doCreateGame(nickname, numPlayers);
    }

    @Override
    public void joinGame(String nickname, int id) {
        if (isInGame()) {
            ui.printError(new InvalidTimingException("Sei già in una partita."));
            return;
        }
        if (id <= 0) {
            ui.printError(new InvalidLobbyException("Codice lobby non valido."));
            return;
        }
        gameEnded = false;
        doJoinGame(nickname, id);
    }

    @Override
    public void move(int tileId) {
        if (!isInGame()) {
            ui.printError(new InvalidTimingException("Non è possibile muoversi prima che la partita sia iniziata."));
            return;
        }
        if (vm.getCurrentPhase() != GamePhaseEnum.SETUP_PHASE) {
            ui.printError(new InvalidPhaseException("FASE INVALIDA"));
            return;
        }
        if (!vm.getNickname().equals(vm.getCurrPlayer())) {
            ui.printError(new InvalidPlayerException("NON E' IL TUO TURNO"));
            return;
        }
        List<TileDTO> board = vm.getBoard();
        if (tileId < 0 || tileId >= board.size()) {
            ui.printError(new InvalidMoveException("Tile non esistente."));
            return;
        }
        if (board.get(tileId).isOccupied()) {
            ui.printError(new OccupiedTileException("Tile già occupata."));
            return;
        }
        doMove(tileId);
    }

    @Override
    public void draw(int card) {
        if (!isInGame()) {
            ui.printError(new InvalidTimingException("Non è consentito pescare prima che la partita sia iniziata."));
            return;
        }
        GamePhaseEnum phase = vm.getCurrentPhase();
        if (phase != GamePhaseEnum.DRAW_PHASE && phase != GamePhaseEnum.OPTIONAL_DRAW_PHASE) {
            ui.printError(new InvalidPhaseException("FASE INVALIDA"));
            return;
        }
        if (!vm.getNickname().equals(vm.getCurrPlayer())) {
            ui.printError(new InvalidPlayerException("NON E' IL TUO TURNO"));
            return;
        }
        boolean inUpper = vm.getUpperList().stream().anyMatch(c -> c.getId() == card);
        boolean inLower = vm.getLowerList().stream().anyMatch(c -> c.getId() == card);
        if (!inUpper && !inLower) {
            ui.printError(new InvalidDrawException("Carta non presente."));
            return;
        }
        ActionsDTO actions = vm.getToDoActions();
        if (inUpper && actions.getUpDraws() <= 0) {
            ui.printError(new InvalidDrawException("Nessuna pescata disponibile dalla fila superiore."));
            notifyDrawableIfPossible();
            return;
        }
        if (inLower && actions.getDownDraws() <= 0) {
            ui.printError(new InvalidDrawException("Nessuna pescata disponibile dalla fila inferiore."));
            notifyDrawableIfPossible();
            return;
        }
        if (CardRegistry.getType(card) == CardTypeEnum.BUILDING) {
            PlayerStatsDTO myStats = vm.getPlayerStats().stream()
                    .filter(s -> s.getNickname().equals(vm.getNickname()))
                    .findFirst().orElse(null);
            if (myStats != null) {
                int actualCost = Math.max(0, CardRegistry.getCost(card) - myStats.getTotBuildDisc());
                if (myStats.getnFood() < actualCost) {
                    ui.printError(new InvalidDrawException("Cibo insufficiente per acquistare questo edificio."));
                    return;
                }
            }
        }
        doDraw(card);
    }

    @Override
    public void skip() {
        if (!isInGame()) {
            ui.printError(new InvalidTimingException("Non è consentito saltare la fase di pesca prima che la partita sia iniziata."));
            return;
        }
        GamePhaseEnum phase = vm.getCurrentPhase();
        if (phase != GamePhaseEnum.DRAW_PHASE && phase != GamePhaseEnum.OPTIONAL_DRAW_PHASE) {
            ui.printError(new InvalidPhaseException("FASE INVALIDA"));
            return;
        }
        if (!vm.getNickname().equals(vm.getCurrPlayer())) {
            ui.printError(new InvalidPlayerException("NON E' IL TUO TURNO"));
            return;
        }
        if (!vm.getToDoActions().isOptionalFlag()) {
            ui.printError(new InvalidSkipException("Non è possibile saltare la pesca adesso."));
            return;
        }
        doSkip();
    }

    @Override
    public void requestRanking() {
        if (!gameEnded) {
            ui.printError(new InvalidTimingException("Il ranking è disponibile solo a fine partita."));
            return;
        }
        doRequestRanking();
    }

    /**
     * Notifies the UI that drawable cards are available, if any draws remain.
     */
    private void notifyDrawableIfPossible() {
        ActionsDTO actions = vm.getToDoActions();
        if (actions.getUpDraws() + actions.getDownDraws() > 0)
            ui.showDrawable();
    }

    /**
     * Resets match state after a quit or server crash.
     * Sets {@code matchId} to 0 and refreshes the {@code VirtualModel} via the UI.
     */
    protected void resetMatch() {
        matchId = 0;
        vm = ui.quit();
    }

    /**
     * Performs the transport-specific login handshake.
     *
     * @param nickname the validated, non-blank nickname
     * @return {@code true} if login succeeded
     */
    protected abstract boolean doLogin(String nickname);

    /**
     * Sends a create-game request over the transport layer.
     *
     * @param nickname   the player's nickname
     * @param numPlayers the desired number of players (already validated: 2–5)
     */
    protected abstract void doCreateGame(String nickname, int numPlayers);

    /**
     * Sends a join-game request over the transport layer.
     *
     * @param nickname the player's nickname
     * @param id       the lobby ID (already validated: &gt; 0)
     */
    protected abstract void doJoinGame(String nickname, int id);

    /**
     * Sends a move request over the transport layer.
     *
     * @param tileId the target tile index (already validated)
     */
    protected abstract void doMove(int tileId);

    /**
     * Sends a draw request over the transport layer.
     *
     * @param card the card ID (already validated)
     */
    protected abstract void doDraw(int card);

    /**
     * Sends a skip-draw request over the transport layer.
     */
    protected abstract void doSkip();

    /**
     * Sends a ranking request over the transport layer.
     */
    protected abstract void doRequestRanking();
}