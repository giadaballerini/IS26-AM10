package it.polimi.ingsw.client.ui.GUI;

import it.polimi.ingsw.client.data.CardRegistry;
import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.network.dto.*;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;
import java.util.stream.Collectors;
/**
 * JavaFX controller for the main in-game screen ({@code gameBoard.fxml}).
 *
 * <p>{@code GameController} is the single point of contact between the network
 * layer and the game UI. It receives update callbacks from {@link ViewGUI}
 * (which is itself notified by the {@link it.polimi.ingsw.client.VirtualModel})
 * and translates them into visual changes: card rows, tile board, totem
 * positions, player statistics, flag icons, event banners, and era curtain
 * animations.</p>
 *
 * <p>All public and private methods that touch JavaFX nodes are guarded by
 * {@link #assertFxThread()}, which throws {@link IllegalStateException} if
 * called from outside the FX Application Thread.</p>
 *
 * <h2>Lifecycle</h2>
 * <ol>
 *   <li>{@link #initialize()} — called by the FXMLLoader; wires button handlers
 *       and installs stat-icon tooltips.</li>
 *   <li>{@link #refreshBoard()} — called by {@code ViewGUI} on every model
 *       update; delegates to {@link #initStructure()} on the first call and to
 *       {@link #syncState()} on every subsequent call.</li>
 * </ol>
 *
 * <h2>Event-banner and era-animation sequencing</h2>
 * <p>The server may send two consecutive {@code EventMessage}s in the same
 * round (round events followed by end-game events). To prevent the second
 * banner from being lost, incoming events are enqueued in
 * {@link #pendingEventBanners} while a banner is already on screen. Era
 * curtain animations are similarly deferred in {@link #pendingEraAnimations}
 * and played once all event banners have been dismissed.</p>
 */

public class GameController {

    @FXML
    private HBox lowerRowChar;
    @FXML
    private HBox upperRowChar;
    @FXML
    private HBox lowerRowBuildsEv;
    @FXML
    private HBox upperRowBuildsEv;
    @FXML
    private HBox flagBox;
    @FXML
    private HBox myVillage;
    @FXML
    private VBox opponents;
    @FXML
    private Text txtBuildDisc;
    @FXML
    private Text txtFeastDisc;
    @FXML
    private Text txtNickname;
    @FXML
    private Text txtPP;
    @FXML
    private Text txtFood;
    @FXML
    private Text txtStars;
    @FXML
    private TextFlow txtCurrPlayer;
    @FXML
    private Text txtCurrPlayerNick;
    @FXML
    private Text txtAge;
    @FXML
    private Text txtRound;
    @FXML
    private Text txtPhase;
    @FXML
    private HBox tiles;
    @FXML
    private ImageView imgPP;
    @FXML
    private ImageView imgFood;
    @FXML
    private ImageView imgStars;
    @FXML
    private ImageView imgBuildDisc;
    @FXML
    private ImageView imgFeastDisc;

    @FXML
    private StackPane globalOverlay;

    @FXML
    private HBox overlayContent;

    @FXML
    private Button skipButton;

    @FXML
    private ImageView menuBtn;

    @FXML Button quitBtn;

    @FXML
    VBox notificationBox;
    /** The view that owns this controller and provides access to the client and the virtual model. */
    private final ViewGUI viewGUI;
    /** {@code true} after {@link #initStructure()} has been executed at least once. */
    private boolean initialized = false;
    /** Turn number recorded at the last {@link #refreshBoard()} call; used to detect round boundaries. */
    private int lastTurn = -1;
    /** Maps each {@link CardTypeEnum} to the {@link VBox} column that holds the player's own cards of that type. */
    private final Map<CardTypeEnum, VBox> cardVBoxMap = new HashMap<>();
    /** Live registry of every {@link CardGUI} currently managed by this controller, keyed by card ID. */
    private final Map<Integer, CardGUI> cardMap = new HashMap<>();
    /** Maps each board-tile ID to its {@link TileGUI} node. */
    private final Map<Integer, TileGUI> tileMap = new HashMap<>();
    /** Maps each player nickname to their {@link TotemGUI} pawn. */
    private final Map<String, TotemGUI> totemMap = new HashMap<>();
    /** Maps each opponent nickname to the {@link Text} node that displays their stats in the side panel. */
    private final Map<String, Text> opponentStatsNodes = new HashMap<>();
    /** Maps each player nickname to their assigned CSS hex color string. */
    private final Map<String, String> opponentColors = new HashMap<>();
    /** Tracks the {@link TileGUI} each player's totem currently occupies (absent when in the queue). */
    private final Map<String, TileGUI> totemPos = new HashMap<>();
    /** GUI node representing the queue tile (starting position for all totems). */
    private QTileGUI qTileGUI;
    /** GUI node representing the draw deck. */
    private DeckGUI deckGUI;
    /** Maps each opponent nickname to their village overlay, shown when the player clicks the "+" button. */
    private final Map<String, VillageOverlay> opponentOverlays = new HashMap<>();
    /** {@code true} while a move network request is in flight; prevents concurrent move submissions. */
    private boolean loadingMove = false;
    /** Floating panel at the bottom of the screen that renders the local player's cards. */
    private VillagePanel villagePanel;
    /** Floating panel on the right that renders all players' statistics. */
    private StatsPanel statsPanel;
    /** Animated dots timeline shown in the loading screen. */
    private javafx.animation.Timeline dotsAnimation;
    /** Preloaded flag icon images keyed by flag name (e.g. {@code "hunt"}, {@code "protection"}). */
    private final Map<String, Image> flagImages = new HashMap<>();
    /** Names of flags whose icons are already displayed in {@link #flagBox}; prevents duplicates. */
    private final Set<String> activeFlags = new HashSet<>();
    /** Pending event cards received via ping while an event banner is already on screen. */
    private final List<CardDTO> pingEvents = new ArrayList<>();

    private PauseTransition eventBannerDelay;
    /** {@code true} while an event banner is currently visible on screen. */
    private boolean eventBannerShowing = false;
    /**
     * Snapshot of every player's stats captured just before an event phase begins.
     * Used to compute the per-player delta displayed in the event banner.
     */
    private Optional<Map<String, PlayerStatsDTO>> statsBeforeEvent = Optional.empty();

    /**
     * Queue of event banners waiting to be displayed.
     * Necessary because the server may send two consecutive {@code EventMessage}s
     * in the same round (round events + end-game events); the second must not be
     * discarded but shown immediately after the first banner closes.
     */
    private final Deque<Map.Entry<EventDTO, List<PlayerStatsDTO>>> pendingEventBanners = new ArrayDeque<>();
    /**
     * Queue of era numbers for which the curtain animation has not yet been played.
     * Era animations are deferred while event banners are on screen and replayed
     * once all pending banners have been dismissed.
     */
    private final Deque<Integer> pendingEraAnimations = new ArrayDeque<>();

    /**
     * Creates a new {@code GameController} bound to the given view.
     *
     * @param viewGUI the owning {@link ViewGUI}; must not be {@code null}
     */
    public GameController(ViewGUI viewGUI) {
        this.viewGUI = viewGUI;
    }
    /**
     * Called automatically by the {@link javafx.fxml.FXMLLoader} after all
     * {@code @FXML} fields have been injected.
     *
     * <p>Wires the skip-button handler, enables hover cursor on the menu icon,
     * and installs custom tooltips on all stat icons.</p>
     */
    @FXML
    public void initialize(){
        txtNickname.setText(viewGUI.getNickname());
        skipButton.setOnAction(e -> handleSkipClick());
        CursorManager.makeNodesHoverable(menuBtn);

        creaTooltip(imgPP, "Punti Prestigio", "I tuoi punti totali accumulati fino ad ora, vince chi ne ha di più!");
        creaTooltip(imgFood, "Riserve di Cibo", "La quantità di cibo disponibile per sfamare la tua tribù.");
        creaTooltip(imgStars, "Stelle", "La quantità di stelle che i tuoi sciamani detengono magicamente.");
        creaTooltip(imgBuildDisc, "Sconto Costruzione", "Riduce il costo richiesto per acquistare edifici.");
        creaTooltip(imgFeastDisc, "Sconto Sostentamento", "Sconto applicato durante l'evento di sostentamento.");
    }

    @FXML
    private void onMenuBtnClick(MouseEvent event) {
        onMenuBtnClick(event, null);
    }
    /**
     * Opens the in-game pause menu overlay.
     *
     * <p>The menu exposes three actions: quit the application, abandon the
     * current match (triggers {@link it.polimi.ingsw.client.Client#quit()}),
     * or dismiss the menu. If {@code fallback} is non-null it is restored as
     * the overlay content when the user navigates back from a confirmation
     * dialog.</p>
     *
     * @param event    the mouse event that triggered the call, or {@code null}
     *                 when invoked programmatically
     * @param fallback optional node to restore when the user clicks "back"
     *                 inside the confirm dialog; pass {@code null} to close
     *                 the overlay entirely
     */
    private void onMenuBtnClick(MouseEvent event, Node fallback) {
        Label titleLabel = new Label("— MENU —");
        titleLabel.setPadding(new Insets(0, 0, 10, 0));
        titleLabel.setStyle("-fx-text-fill: rgb(254,242,210);-fx-font-family: \"Vagabundo Medium\";\n" + "-fx-font-size: 30px;");
        Button esciBtn = new Button("Esci dal gioco");
        esciBtn.getStyleClass().add("bottone-mesos");
        esciBtn.setPrefWidth(250);
        esciBtn.setPadding(Insets.EMPTY);
        esciBtn.setOnAction(e -> showConfirm(
                "Vuoi davvero uscire?",
                "L'applicazione verrà chiusa.",
                () -> Platform.exit()
        ));

        Button abbandonaBtn = new Button("Abbandona partita");
        abbandonaBtn.getStyleClass().add("bottone-mesos");
        abbandonaBtn.setPrefWidth(250);
        abbandonaBtn.setPadding(Insets.EMPTY);
        abbandonaBtn.setOnAction(e -> showConfirm(
                "Vuoi abbandonare la partita?",
                "Verrai disconnesso e la partita terminerà.",
                () -> viewGUI.getClient().quit()
        ));

        Button cancelBtn = new Button("Annulla");
        cancelBtn.getStyleClass().add("bottone-mesos");
        cancelBtn.setPrefWidth(250);
        cancelBtn.setOnAction(e -> hideOverlay(fallback));

        VBox menu = new VBox(14, titleLabel, esciBtn, abbandonaBtn, cancelBtn);
        menu.setAlignment(Pos.CENTER);
        menu.setStyle("-fx-background-color: #2c2c2c; -fx-border-color: #888; " +
                "-fx-border-width: 2; -fx-padding: 28 36 28 36; " +
                "-fx-background-radius: 10; -fx-border-radius: 10;");
        menu.setMaxWidth(500);
        menu.setMinWidth(500);
        menu.setMaxHeight(330);
        menu.setMinHeight(330);
        showOverlay(menu);
    }
    /**
     * Shows a two-button confirmation dialog (Confirm / Back) inside the global
     * overlay. Clicking <em>Confirm</em> hides the overlay and runs
     * {@code onConfirm}; clicking <em>Back</em> navigates to {@code fallback}
     * (or to the pause menu if {@code fallback} is {@code null}).
     *
     * @param title     bold headline text shown at the top of the dialog
     * @param caption   secondary descriptive text shown below the headline
     * @param onConfirm action executed when the user confirms
     */
    private void showConfirm(String title, String caption, Runnable onConfirm) {
        showConfirm(title, caption, onConfirm, null);
    }
    /**
     * Shows a two-button confirmation dialog (Confirm / Back) inside the global
     * overlay. Clicking <em>Confirm</em> hides the overlay and runs
     * {@code onConfirm}; clicking <em>Back</em> restores {@code fallback} as
     * the overlay content, or re-opens the pause menu if {@code fallback} is
     * {@code null}.
     *
     * @param title     bold headline text shown at the top of the dialog
     * @param caption   secondary descriptive text shown below the headline
     * @param onConfirm action executed when the user confirms
     * @param fallback  node to restore on "Back", or {@code null} to reopen the menu
     */
    private void showConfirm(String title, String caption, Runnable onConfirm, Node fallback) {
        Text t = new Text(title);
        t.setFill(Color.WHITE);
        t.setStyle("-fx-font-size: 32; -fx-font-weight: bold;");

        Text sub = new Text(caption);
        sub.setFill(Color.web("#aaaaaa"));
        sub.setStyle("-fx-font-size: 22;");

        Button confirmBtn = new Button("Conferma");
        confirmBtn.getStyleClass().add("bottone-mesos");
        confirmBtn.setPrefWidth(200);
        confirmBtn.setOnAction(e -> {
            hideOverlay();
            onConfirm.run();
        });

        Button backBtn = new Button("Indietro");
        backBtn.getStyleClass().add("bottone-mesos");
        backBtn.setPrefWidth(200);
        backBtn.setOnAction(e -> onMenuBtnClick(null, fallback)); // torna al menu

        HBox buttons = new HBox(22, confirmBtn, backBtn);
        buttons.setAlignment(Pos.CENTER);

        VBox panel = new VBox(25, t, sub, buttons);
        panel.setAlignment(Pos.CENTER);
        panel.setStyle("-fx-background-color: #2c2c2c; -fx-border-color: #888; " +
                "-fx-border-width: 2; -fx-padding: 28 36 28 36; " +
                "-fx-background-radius: 10; -fx-border-radius: 10;");
        panel.setMaxWidth(500);
        panel.setMaxHeight(300);
        panel.setMinHeight(300);
        showOverlay(panel);
    }
    /**
     * Installs a custom styled tooltip on the given {@link ImageView}.
     *
     * <p>The tooltip is composed of a bold title and a wrapping description
     * text, styled via the {@code card-tooltip} CSS class. It is installed
     * only once per node; repeated calls for the same target are silently
     * ignored.</p>
     *
     * @param target      the node on which the tooltip is installed
     * @param title       short label displayed in bold at the top
     * @param descrizione longer description shown below the title
     */
    private void creaTooltip(ImageView target, String title, String descrizione) {

        if (target.getProperties().containsKey("custom-tooltip-installed")) {
            return;
        }
        target.getProperties().put("custom-tooltip-installed", true);

        VBox content = new VBox(4);
        content.getStyleClass().add("card-tooltip-content");
        content.setMouseTransparent(true);

        Text titleLabel = new Text(title);
        titleLabel.getStyleClass().add("card-tooltip-title");

        Text descLabel = new Text(descrizione);
        descLabel.getStyleClass().add("card-tooltip-description");
        descLabel.setWrappingWidth(210);

        content.getChildren().addAll(titleLabel, descLabel);

        Tooltip tooltip = new Tooltip();
        tooltip.setGraphic(content);
        tooltip.getStyleClass().add("card-tooltip");

        tooltip.setShowDelay(Duration.millis(350));
        tooltip.setHideDelay(Duration.millis(100));

        tooltip.setOnShowing(e -> {
            tooltip.setAnchorY(tooltip.getAnchorY() + 18);
        });

        Tooltip.install(target, tooltip);
    }

    /**
     * Refreshes the board to reflect the current state of the
     * {@link it.polimi.ingsw.client.VirtualModel}.
     *
     * <p>On the first invocation ({@link #initialized} is {@code false}) the
     * full scene structure is built by {@link #initStructure()}. On every
     * subsequent call only the dynamic state (card rows, totem positions,
     * stats, phase labels) is re-synchronized via {@link #syncState()}.</p>
     *
     * <p>If the current turn number differs from {@link #lastTurn}, a
     * round-boundary animation is triggered via {@link #onEndRound()}.</p>
     *
     * <p>Must be called on the FX Application Thread.</p>
     */
    public void refreshBoard() {
        assertFxThread();
        if (!eventBannerShowing) hideOverlay();
        if (!initialized) {
            uploadFlags();
            initStructure();
            initialized = true;
        }

        int currentTurn = viewGUI.getModel().getCurrTurn();
        syncState();

        if(currentTurn != lastTurn) {
            lastTurn = currentTurn;
            onEndRound();
        }
    }
    /**
     * Moves the card identified by {@code cardId} from the draw rows into the
     * local player's village column for its type.
     *
     * <p>The card is explicitly removed from all four draw-row {@link HBox}es
     * before being appended to the appropriate {@link VBox} column. If no
     * column exists yet for the card's type, a new one is created and added to
     * {@link #myVillage}. Duplicate insertions are prevented by checking
     * whether a column already holds the card.</p>
     *
     * <p>Must be called on the FX Application Thread.</p>
     *
     * @param cardId the unique identifier of the card to insert; must be
     *               present in {@link #cardMap}
     */
    public void insertCard(int cardId) {
        assertFxThread();

        CardGUI card = cardMap.get(cardId);
        if (card == null) {
            System.err.println("[GameController] insertCard: cardId sconosciuto " + cardId);
            return;
        }
        card.setDisable(false);

        //Rimuove esplicitamente la carta dalle file di pesca
        upperRowChar.getChildren().remove(card);
        upperRowBuildsEv.getChildren().remove(card);
        lowerRowChar.getChildren().remove(card);
        lowerRowBuildsEv.getChildren().remove(card);

        //Se la carta era già dentro un vecchio VBox di una colonna rimasta in memoria,
        // la sradichiamo dal suo vecchio parent grafico. Questo previene i duplicati visivi.
        if (card.getParent() != null) {
            if (card.getParent() instanceof Pane) {
                ((Pane) card.getParent()).getChildren().remove(card);
            }
        }

        //Recupera o crea la colonna per il tipo di carta
        VBox column = cardVBoxMap.get(card.getType());
        if (column == null) {
            column = new VBox();
            column.setSpacing(-135);
            myVillage.getChildren().add(column);
            cardVBoxMap.put(card.getType(), column);
        }

        card.setDisable(false);
        card.setOnMouseClicked(null);

        column.getChildren().add(card);
    }
    /**
     * Displays a temporary notification message at the top of the
     * {@link #notificationBox}.
     *
     * <p>The message fades out after 2.5 seconds and is then removed from the
     * scene graph. Notifications are stacked in reverse insertion order
     * (newest on top).</p>
     *
     * <p>Must be called on the FX Application Thread.</p>
     *
     * @param message  the text to display
     * @param colorHex CSS hex color string applied to the message text
     *                 (e.g. {@code "#3498db"})
     */
    private void showNotification(String message, String colorHex) {
        assertFxThread();
        Text notif = new Text(message);
        notif.setFill(Color.web(colorHex));
        notif.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        notificationBox.getChildren().addFirst(notif);

        PauseTransition delay = new PauseTransition(Duration.millis(2500));
        delay.setOnFinished(e -> {
            FadeTransition fade = new FadeTransition(Duration.millis(400), notif);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);
            fade.setOnFinished(ev -> notificationBox.getChildren().remove(notif));
            fade.play();
        });
        delay.play();
    }
    /**
     * Handles a move confirmation from the server: relocates the moving
     * player's totem from the queue tile to the target board tile and
     * refreshes the drawable-card highlights.
     *
     * <p>Must be called on the FX Application Thread.</p>
     *
     * @param tile       DTO of the tile the player moved to
     * @param currPlayer nickname of the player who performed the move
     */
    public void onMoveUpdate(TileDTO tile, String currPlayer) {
        assertFxThread();
        tileMap.values().forEach(t -> t.setDisable(false));

        TotemGUI totem = totemMap.get(currPlayer);

        if (totem == null) {
            System.err.println("totem non trovato: " + currPlayer);
            return;
        }

        QTileGUI source = qTileGUI;
        TileGUI target = tileMap.get(tile.getId());
        if (source == null || target == null) {
            System.err.println("source o target null");
            return;
        }
        source.removePawn(totem);
        target.addPawn(totem);
        refreshDrawableHighlight();
        totemPos.put(currPlayer, target);
    }
    /**
     * Updates the "current player" label to reflect whose turn it is.
     *
     * <p>The nickname is rendered in the player's assigned color as returned
     * by {@link #opponentColors}.</p>
     *
     * <p>Must be called on the FX Application Thread.</p>
     *
     * @param nickname the nickname of the player whose turn has just started
     */
    public void onCurrPlayerUpdate(String nickname) {
        assertFxThread();
        txtCurrPlayer.setVisible(true);
        txtCurrPlayer.setManaged(true);
        txtCurrPlayerNick.setText(nickname.toLowerCase());
        String color = opponentColors.getOrDefault(nickname, "#ffffff");
        System.out.println("[DEBUG] currPlayer=" + nickname + " color=" + color + " opponentColors=" + opponentColors);
        txtCurrPlayerNick.setFill(Color.web(color));
    }
    /**
     * Updates the phase label and refreshes drawable-card highlights in
     * response to a phase-change message from the server.
     *
     * <p>Must be called on the FX Application Thread.</p>
     *
     * @param phaseDTO DTO carrying the new {@link GamePhaseEnum} value
     */
    public void onPhaseUpdate(PhaseDTO phaseDTO) {
        assertFxThread();
        txtPhase.setText("fase: " + phaseToLabel(phaseDTO.getPhase()));
        refreshDrawableHighlight();
    }
    /**
     * Handles a player returning to the queue: moves their totem back to the
     * queue tile and updates their stats display.
     *
     * <p>Must be called on the FX Application Thread.</p>
     *
     * @param t the queue tile DTO (currently unused beyond triggering the update)
     * @param s stats DTO of the player who returned to the queue
     */
    public void onReturnToQueue(TileDTO t, PlayerStatsDTO s) {
        String nick = s.getNickname();
        assertFxThread();
        TotemGUI totem = totemMap.get(nick);
        QTileGUI target = qTileGUI;
        TileGUI source = totemPos.get(s.getNickname());

        if(source == null || totem == null) return;

        source.removePawn(totem);
        target.addPawn(totem);
        totemPos.remove(nick);
        updateStats(List.of(s));
    }
    /**
     * Handles an era-change notification from the server.
     *
     * <p>Updates the era label and the deck display. If an event banner is
     * currently on screen (or pending banners are queued), the curtain
     * animation is deferred in {@link #pendingEraAnimations} and will play
     * once all banners have been dismissed; otherwise it is shown immediately
     * via {@link #showEraCurtainAnimation(int)}.</p>
     *
     * <p>Must be called on the FX Application Thread.</p>
     *
     * @param age the new era number (1-based)
     */
    public void onChangeAge(int age) {
        assertFxThread();
        txtAge.setText("era: " + age);
        if(deckGUI != null)
            deckGUI.update(viewGUI.getModel().getDeckSize(), age);
        // Se c'e' un banner eventi in corso, accoda l'animazione; altrimenti mostrala subito
        if (eventBannerShowing || !pendingEventBanners.isEmpty()) {
            pendingEraAnimations.addLast(age);
        } else {
            showEraCurtainAnimation(age);
        }
    }

    /**
     * Plays the era-change curtain animation.
     *
     * <p>Two dark panels slide in from the top and bottom of the screen,
     * meet in the center, reveal an era title (e.g. "ERA II") for roughly
     * 1.8 seconds, then slide back out. The animation is built entirely from
     * JavaFX {@link TranslateTransition}s and {@link FadeTransition}s
     * assembled in a {@link SequentialTransition}.</p>
     *
     * <p>An {@link javafx.animation.AnimationTimer} is used to wait two
     * rendering frames before reading the panel heights, ensuring that layout
     * has been performed and the measurements are accurate.</p>
     *
     * <p>Must be called on the FX Application Thread.</p>
     *
     * @param age the era number to display, converted to a Roman numeral
     *            by {@link #toRoman(int)}
     */
    private void showEraCurtainAnimation(int age) {
        assertFxThread();

        StackPane root = (StackPane) globalOverlay.getParent();

        Region topPane = new Region();
        topPane.setStyle("-fx-background-color: rgba(10,7,2,0.97);");
        topPane.prefWidthProperty().bind(root.widthProperty());
        topPane.prefHeightProperty().bind(root.heightProperty().divide(2));
        topPane.setTranslateY(-3000);

        Region botPane = new javafx.scene.layout.Region();
        botPane.setStyle("-fx-background-color: rgba(10,7,2,0.97);");
        botPane.prefWidthProperty().bind(root.widthProperty());
        botPane.prefHeightProperty().bind(root.heightProperty().divide(2));
        botPane.setTranslateY(3000);

        Region decorLine = new javafx.scene.layout.Region();
        decorLine.setStyle("-fx-background-color: #c8903a;");
        decorLine.setPrefHeight(2);
        decorLine.prefWidthProperty().bind(root.widthProperty().multiply(0.35));

        Font.loadFont(getClass().getResourceAsStream("/fonts/Mesos.ttf"), 14);

        Text eraText = new javafx.scene.text.Text("ERA " + toRoman(age));
        eraText.setStyle("-fx-font-family: 'Vagabundo Medium'; -fx-font-size: 70px; -fx-font-weight: bold;");
        eraText.setFill(javafx.scene.paint.Color.web("#f0d070"));

        Text subText = new javafx.scene.text.Text("inizia una nuova era");
        subText.setStyle("-fx-font-family: 'Vagabundo Medium'; -fx-font-size: 40px;");
        subText.setFill(javafx.scene.paint.Color.web("#a08040"));

        VBox centerContent = new javafx.scene.layout.VBox(12,
                decorLine, eraText, subText);
        centerContent.setAlignment(javafx.geometry.Pos.CENTER);
        centerContent.setMouseTransparent(true);
        centerContent.setOpacity(0);

        // Usiamo uno StackPane come overlay, con i due pannelli posizionati
        // tramite translateY relativo alla loro altezza (50% dello schermo).
        // topPane parte sopra lo schermo (translateY = -altezza), botPane sotto.
        StackPane curtainOverlay = new javafx.scene.layout.StackPane();
        curtainOverlay.setMouseTransparent(true);
        curtainOverlay.prefWidthProperty().bind(root.widthProperty());
        curtainOverlay.prefHeightProperty().bind(root.heightProperty());

        // I pannelli devono occupare meta' schermo e partire fuori dallo schermo.
        // Allineiamo topPane in alto e botPane in basso, poi li spostiamo con translateY.
        StackPane.setAlignment(topPane, javafx.geometry.Pos.TOP_CENTER);
        StackPane.setAlignment(botPane, javafx.geometry.Pos.BOTTOM_CENTER);

        curtainOverlay.getChildren().addAll(topPane, botPane, centerContent);
        root.getChildren().add(curtainOverlay);

        // Ora che i pannelli sono nella scena, colleghiamo translateY alla loro altezza.
        // topPane parte sopra: translateY = -prefHeight (negativo = fuori in alto)
        topPane.translateYProperty().bind(topPane.heightProperty().negate());
        botPane.translateYProperty().bind(botPane.heightProperty());

        // Dopo un frame la dimensione e' pronta: stacchiamo il bind e animiamo
        javafx.animation.AnimationTimer starter = new javafx.animation.AnimationTimer() {
            private int frames = 0;
            @Override public void handle(long now) {
                if (++frames < 3) return; // aspetta 2 frame per la misurazione
                stop();
                double halfH = topPane.getHeight();
                topPane.translateYProperty().unbind();
                botPane.translateYProperty().unbind();
                topPane.setTranslateY(-halfH);
                botPane.setTranslateY(halfH);

                TranslateTransition closeTop = new javafx.animation.TranslateTransition(Duration.millis(500), topPane);
                closeTop.setToY(0);
                closeTop.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
                TranslateTransition closeBot = new javafx.animation.TranslateTransition(Duration.millis(500), botPane);
                closeBot.setToY(0);
                closeBot.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
                ParallelTransition closeAll = new javafx.animation.ParallelTransition(closeTop, closeBot);

                // 2) Contenuto centrale appare (opacity sul VBox copre tutti i figli)
                FadeTransition showAll = new FadeTransition(Duration.millis(400), centerContent);
                showAll.setFromValue(0); showAll.setToValue(1);

                // 3) Pausa
                PauseTransition hold = new javafx.animation.PauseTransition(Duration.millis(1800));

                // 4) Contenuto sparisce
                FadeTransition hideContent = new FadeTransition(Duration.millis(300), centerContent);
                hideContent.setFromValue(1.0); hideContent.setToValue(0.0);

                // 5) Tendine si riaprono usando l'altezza reale misurata
                TranslateTransition openTop = new javafx.animation.TranslateTransition(Duration.millis(500), topPane);
                openTop.setToY(-halfH);
                openTop.setInterpolator(javafx.animation.Interpolator.EASE_IN);
                TranslateTransition openBot = new javafx.animation.TranslateTransition(Duration.millis(500), botPane);
                openBot.setToY(halfH);
                openBot.setInterpolator(javafx.animation.Interpolator.EASE_IN);
                ParallelTransition openAll = new javafx.animation.ParallelTransition(openTop, openBot);

                SequentialTransition seq = new SequentialTransition(
                        closeAll, showAll, hold, hideContent, openAll
                );
                seq.setOnFinished(ev -> {
                    root.getChildren().remove(curtainOverlay);
                    // Dopo la tendine, mostra eventuali altre animazioni era accodate
                    Integer nextAge = pendingEraAnimations.pollFirst();
                    if (nextAge != null) showEraCurtainAnimation(nextAge);
                });
                seq.play();
            }
        };
        starter.start();
    }

    /**
     * Converts a small positive integer to its Roman-numeral string.
     *
     * @param n the number to convert (expected values: 1, 2, 3)
     * @return the Roman-numeral string, or an empty string for unrecognized values
     */
    private String toRoman(int n) {
        return switch(n){
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            default -> "";
        };
    }
    /**
     * Handles a player-status update by refreshing the flag icons for the
     * local player.
     *
     * <p>Updates are ignored for other players' status messages.</p>
     *
     * <p>Must be called on the FX Application Thread.</p>
     *
     * @param status the updated status DTO; processed only when
     *               {@link PlayerStatusDTO#getNickname()} equals the local
     *               player's nickname
     */
    public void onStatusUpdate(PlayerStatusDTO status) {
        assertFxThread();
        if (!status.getNickname().equals(viewGUI.getNickname())) return;

        putFlags(status);
    }
    /**
     * Evaluates the active flags from a {@link PlayerStatusDTO} and adds the
     * corresponding icons to {@link #flagBox} for each flag that is set.
     *
     * @param status the status DTO from which flag values are read
     */
    private void putFlags(PlayerStatusDTO status) {
        List.of(
                Map.entry("hunt", status.isHuntBonus()),
                Map.entry("paint", status.isPaintFlag()),
                Map.entry("extra", status.isExtraFlag()),
                Map.entry("protection", status.hasProtection()),
                Map.entry("painter", status.hasDiscountFor(CardTypeEnum.PAINTER)),
                Map.entry("crafter", status.hasDiscountFor(CardTypeEnum.CRAFTER)),
                Map.entry("gatherer", status.hasDiscountFor(CardTypeEnum.GATHERER)),
                Map.entry("double", status.hasDoubleShamanIncome())
        ).forEach(e -> {
            if (e.getValue()) addFlagIcon(e.getKey());
        });
    }
    /**
     * Handles a draw notification for an opponent player: shows a notification
     * banner and refreshes both the card rows and all village displays.
     *
     * <p>Must be called on the FX Application Thread.</p>
     *
     * @param card     DTO of the card the opponent drew
     * @param nickname nickname of the player who drew the card
     */
    public void onOpponentDraw(CardDTO card, String nickname) {
        assertFxThread();

        String color = opponentColors.getOrDefault(nickname, "#ffffff");
        String typeName = card.getType().toString().toLowerCase();

        showNotification(nickname + " ha pescato: " + typeName, color);

        updateCardRows();

        updateVillages();
    }
    /**
     * Triggers a refresh of drawable-card highlight states, typically called
     * when the server signals that new cards are available to draw.
     *
     * <p>Must be called on the FX Application Thread.</p>
     */
    public void showDrawable() {
        assertFxThread();
        refreshDrawableHighlight();
    }
    /**
     * Displays a skip notification for the given player.
     *
     * <p>Must be called on the FX Application Thread.</p>
     *
     * @param nickname nickname of the player who skipped their draw turn
     */
    public void notifySkip(String nickname) {
        assertFxThread();

        String color = opponentColors.getOrDefault(nickname, "#ffffff");

        showNotification(nickname + " ha pescato saltato il turno.", color);
    }

    /**
     * Displays the event banner for the given {@link EventDTO}.
     *
     * <p>If a banner is already on screen, the new event is enqueued in
     * {@link #pendingEventBanners} and will be shown automatically once the
     * current banner is dismissed. Empty event DTOs are silently ignored.</p>
     *
     * <p>Must be called on the FX Application Thread.</p>
     *
     * @param events     DTO containing the event cards and post-event stats
     * @param statsBefore list of per-player stats captured <em>before</em>
     *                    the events were applied; used to compute the delta
     *                    line shown in the banner
     */
    public void onEvent(EventDTO events, List<PlayerStatsDTO> statsBefore) {
        assertFxThread();
        if (!events.isEmpty()) {
            if (eventBannerShowing) {
                pendingEventBanners.addLast(Map.entry(events, statsBefore));
                return;
            }
            showEventBanner(events, statsBefore);
        }
    }

    /**
     * Builds and displays the slide-in banner for a single {@link EventDTO}.
     *
     * <p>The banner contains the event card images, a per-player stat-delta
     * summary, and an auto-dismissing progress bar. It slides in from the
     * left and is automatically hidden after 8 seconds via
     * {@link #hideBanner()}.</p>
     *
     * <p>Must be called on the FX Application Thread.</p>
     *
     * @param events      the events to display
     * @param statsBefore pre-event stats used to compute per-player deltas
     */
    private void showEventBanner(EventDTO events, List<PlayerStatsDTO> statsBefore) {
        assertFxThread();
        eventBannerShowing = true;
        Map<String, PlayerStatsDTO> beforeMap = statsBefore.stream()
                .collect(Collectors.toMap(PlayerStatsDTO::getNickname, s -> s));

        Text label = new Text("Eventi del round");
        label.setFill(Color.WHITE);
        label.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");

        HBox cardsRow = new HBox(16);
        cardsRow.setAlignment(Pos.CENTER);
        for (CardDTO event : events.getEvents()) {
            CardGUI card = new CardGUI(event.getId());
            card.setOnMouseClicked(null);
            card.setHighlight(false);
            cardsRow.getChildren().add(card);
        }

        VBox deltaBox = new VBox(4);
        deltaBox.setAlignment(Pos.CENTER);
        for (PlayerStatsDTO after : events.getStats()) {
            PlayerStatsDTO before = beforeMap.get(after.getNickname());
            if (before == null) continue;
            String line = buildDeltaLine(before, after);
            if (line.isEmpty()) continue;
            String color = opponentColors.getOrDefault(after.getNickname(), "#ffffff");
            Text row = new Text(after.getNickname() + ":  " + line);
            row.setFill(Color.web(color));
            row.setStyle("-fx-font-size: 20;");
            deltaBox.getChildren().add(row);
        }

        javafx.scene.control.ProgressBar progressBar = new javafx.scene.control.ProgressBar(1.0);
        progressBar.setPrefWidth(300);

        VBox content = new VBox(12);
        content.setAlignment(Pos.CENTER);
        HBox.setHgrow(content, javafx.scene.layout.Priority.ALWAYS);
        content.getChildren().addAll(label, cardsRow, deltaBox, progressBar);

        HBox banner = new HBox(24);
        banner.setAlignment(Pos.CENTER);
        banner.setPadding(new javafx.geometry.Insets(16, 32, 16, 32));
        banner.setMaxWidth(Double.MAX_VALUE);
        banner.setMinHeight(500);
        banner.setMaxHeight(500);
        banner.setStyle(
                "-fx-background-color: rgba(0,0,0,0.82);" +
                        "-fx-background-radius: 0;"
        );
        banner.getChildren().add(content);

        showOverlay(banner);
        banner.prefWidthProperty().bind(
                ((javafx.scene.layout.Region) banner.getParent()).widthProperty()
        );
        banner.setTranslateX(-1920);

        javafx.animation.TranslateTransition slideIn = new javafx.animation.TranslateTransition(Duration.millis(400), banner);
        slideIn.setToX(0);
        slideIn.setInterpolator(javafx.animation.Interpolator.EASE_OUT);

        int displayMillis = 8000;
        javafx.animation.Timeline countdown = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(Duration.ZERO,
                        new javafx.animation.KeyValue(progressBar.progressProperty(), 1.0)),
                new javafx.animation.KeyFrame(Duration.millis(displayMillis),
                        new javafx.animation.KeyValue(progressBar.progressProperty(), 0.0))
        );
        countdown.setOnFinished(e -> hideBanner());

        slideIn.setOnFinished(e -> countdown.play());
        slideIn.play();
    }
    /**
     * Transitions to the end-game leaderboard screen.
     *
     * <p>Loads {@code leaderboardBoard.fxml}, passes the final ranking data to
     * {@link LeaderboardController}, and replaces the current scene with the
     * leaderboard in full-screen mode.</p>
     *
     * <p>Must be called on the FX Application Thread.</p>
     *
     * @param stats           list of final per-player stats
     * @param pos             the local player's finishing position (1-based)
     * @param globalRankingPos the local player's position in the persistent global leaderboard
     */
    public void onGameEnding(List<PlayerStatsDTO> stats, int pos, int globalRankingPos) {
        assertFxThread();
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/leaderboardBoard.fxml"));
            Parent root = loader.load();
            LeaderboardController ctrl = loader.getController();
            viewGUI.setLeaderboardController(ctrl);
            ctrl.setData(stats, pos, this.viewGUI, globalRankingPos);

            Scene scene = new Scene(root);
            if(tiles.getScene() == null){
                System.err.println("tiles.getScene è null");
                return;
            }

            Stage stage = (Stage) tiles.getScene().getWindow();
            stage.setScene(scene);
            Platform.runLater(() -> {
                stage.setFullScreenExitHint("");
                stage.setFullScreen(true);
            });

        } catch (Exception e){
            System.err.println("[GAME ENDING] Eccezione: " + e.getClass().getName() + " — " + e.getMessage() + "\n");
            e.printStackTrace();
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR, "Errore fine partita: " + e.getMessage());
            alert.show();
        }
    }


    public void showLeaderboard(Map<PlayerDTO, Integer> ranks) {
        assertFxThread();
    }

    /**
     * Performs the one-time structural setup of the scene: board tiles, queue
     * tile, deck widget, totem pawns, card rows, player stats, and opponent
     * village overlays.
     *
     * <p>This method is intentionally separated from {@link #syncState()} so
     * that the fixed scene structure is built only once, even when
     * {@link #refreshBoard()} is called repeatedly (e.g. after a
     * reconnection).</p>
     *
     * <p>Must be called on the FX Application Thread.</p>
     */
    private void initStructure() {
        assertFxThread();
        List<PlayerDTO> players = viewGUI.getModel().getPlayers();
        int numPlayers = viewGUI.getModel().getNumPlayers();
        List<TileDTO> tiles = viewGUI.getModel().getBoard();
        List<PlayerStatsDTO> stats = viewGUI.getModel().getPlayerStats();
        List<TileDTO> qtiles = viewGUI.getModel().getQueue();
        List<CardDTO> cardsUp = viewGUI.getModel().getUpperList();
        List<CardDTO> cardsDown = viewGUI.getModel().getLowerList();

        initVillagePanel();
        initStatsPanel();
        initCards(cardsUp,cardsDown);
        initPlayersColorsAndPawns(players);
        initBoard(numPlayers, tiles);
        initQueue(qtiles);
        initStats(stats);
        initOpponentOverlays(players);
        initFlags();
    }

    /**
     * Synchronises all dynamic GUI elements with the current state of the
     * {@link it.polimi.ingsw.client.VirtualModel}.
     *
     * <p>Covers: totem positions, card rows, village columns, player stats,
     * phase / turn / age labels, drawable highlights, and the deck widget.
     * Called on every {@link #refreshBoard()} invocation, including after a
     * reconnection.</p>
     *
     * <p>Must be called on the FX Application Thread.</p>
     */
    private void syncState() {
        assertFxThread();
        tileMap.values().forEach(TileGUI::resetArrowSize);
        cardMap.clear();
        updateTotemPositions();
        updateCardRows();
        updateVillages();
        updateStats(viewGUI.getModel().getPlayerStats());
        updateInfos(
                viewGUI.getModel().getCurrAge(),
                viewGUI.getModel().getCurrTurn(),
                viewGUI.getModel().getCurrPlayer(),
                viewGUI.getModel().getCurrentPhase()
        );
        refreshDrawableHighlight();
        if(deckGUI != null)
            deckGUI.update(viewGUI.getModel().getDeckSize(), viewGUI.getModel().currAge);

    }
    /**
     * Re-reads totem positions from the virtual model and places every totem
     * on either the queue tile or the appropriate board tile.
     *
     * <p>Clears all existing pawn placements before re-populating, so the
     * result always reflects the authoritative server state.</p>
     *
     * <p>Must be called on the FX Application Thread.</p>
     */
    private void updateTotemPositions() {
        assertFxThread();

        QTileGUI qTile = qTileGUI;
        qTile.clearPawns();
        tileMap.values().forEach(TileGUI::clearPawn);
        totemPos.clear();

        for (TileDTO qt : viewGUI.getModel().getQueue()) {
            if (qt.isOccupied() && qt.getPlayer() != null && !qt.getPlayer().isEmpty()) {
                TotemGUI totem = totemMap.get(qt.getPlayer());
                if (totem != null) qTile.addPawn(totem);
            }
        }

        for (TileDTO bt : viewGUI.getModel().getBoard()) {
            if (bt.isOccupied() && bt.getPlayer() != null && !bt.getPlayer().isEmpty()) {
                TileGUI tileGUI = tileMap.get(bt.getId());
                TotemGUI totem  = totemMap.get(bt.getPlayer());
                if (tileGUI != null && totem != null) {
                    tileGUI.addPawn(totem);
                    totemPos.put(bt.getPlayer(), tileGUI);
                }
            }
        }
    }

    /**
     * Creates and attaches the {@link VillagePanel} floating panel to the
     * scene root, then redirects {@link #myVillage} to the panel's inner
     * {@link HBox}.
     *
     * <p>The panel is inserted immediately below {@link #globalOverlay} in the
     * z-order so that it appears behind modal overlays. Idempotent: does
     * nothing if the panel has already been created.</p>
     *
     * <p>Must be called on the FX Application Thread.</p>
     */
    private void initVillagePanel() {
        assertFxThread();
        if (villagePanel != null) return;

        villagePanel = new VillagePanel();

        javafx.scene.layout.StackPane sceneRoot =
                (javafx.scene.layout.StackPane) globalOverlay.getParent();

        int overlayIdx = sceneRoot.getChildren().indexOf(globalOverlay);
        sceneRoot.getChildren().add(overlayIdx, villagePanel);
        javafx.scene.layout.StackPane.setAlignment(villagePanel, Pos.BOTTOM_CENTER);

        myVillage = villagePanel.getMyVillage();
    }

    /**
     * Creates and attaches the {@link StatsPanel} floating panel to the scene
     * root, then redirects {@link #opponents} to the panel's inner
     * {@link VBox}.
     *
     * <p>The panel is inserted immediately below {@link #globalOverlay} in the
     * z-order and aligned to the right edge of the screen. Idempotent: does
     * nothing if the panel has already been created.</p>
     *
     * <p>Must be called on the FX Application Thread.</p>
     */
    private void initStatsPanel() {
        assertFxThread();
        if (statsPanel != null) return;

        statsPanel = new StatsPanel();

        javafx.scene.layout.StackPane sceneRoot =
                (javafx.scene.layout.StackPane) globalOverlay.getParent();

        int overlayIdx = sceneRoot.getChildren().indexOf(globalOverlay);
        sceneRoot.getChildren().add(overlayIdx, statsPanel);
        StackPane.setAlignment(statsPanel, Pos.CENTER_RIGHT);

        opponents = statsPanel.getOpponents();
    }
    /**
     * Builds a {@link VillageOverlay} for each opponent player and stores it
     * in {@link #opponentOverlays}.
     *
     * <p>Each overlay contains one {@link VBox} column per {@link CardTypeEnum};
     * columns are hidden until populated. The overlay is shown when the local
     * player clicks the "+" button next to an opponent's name.</p>
     *
     * <p>Must be called on the FX Application Thread.</p>
     *
     * @param players the full player list; the local player is skipped
     */
    private void initOpponentOverlays(List<PlayerDTO> players){
        assertFxThread();
        for(PlayerDTO player : players){
            if(player.getNickname().equals(viewGUI.getNickname())) continue;

            Map<CardTypeEnum, VBox> cols = new HashMap<>();
            HBox content = new HBox(10);
            content.setAlignment(Pos.TOP_CENTER);

            for (CardTypeEnum type : CardTypeEnum.values()) {
                VBox col = new VBox(-135);
                col.setVisible(false);
                col.setManaged(false);
                cols.put(type, col);
                content.getChildren().add(col);
            }
            String color = opponentColors.getOrDefault(player.getNickname(), "#ffffff");
            Text title = new Text("Tribù di " + player.getNickname());
            title.setFill(Color.web(color));
            title.setStyle("-fx-font-size: 24;");

            Button closeBtn = new Button("Chiudi");
            closeBtn.setOnAction(e -> hideOverlay());

            VBox root = new VBox(10);
            root.setAlignment(Pos.CENTER);
            root.getChildren().addAll(title, content, closeBtn);

            opponentOverlays.put(player.getNickname(), new VillageOverlay(root,content, cols));
        }
    }
    /**
     * Rebuilds all village columns from the current virtual-model snapshot.
     *
     * <p>For the local player, cards are inserted into {@link #myVillage} via
     * {@link #insertCard(int)}. For opponents, cards are inserted into the
     * corresponding {@link VillageOverlay}. Duplicate card IDs within the same
     * player are filtered out using a local {@link Set}.</p>
     *
     * <p>Must be called on the FX Application Thread (indirectly via
     * {@link #syncState()}).</p>
     */
    private void updateVillages() {
        for (VBox col : cardVBoxMap.values()) {
            col.getChildren().clear();
        }

        myVillage.getChildren().clear();
        cardVBoxMap.clear();

        List<PlayerDTO> players = viewGUI.getModel().getPlayers();
        for (PlayerDTO player : players) {
            if (player.getNickname().equals(viewGUI.getNickname())) {

                // Set di controllo per evitare duplicati strutturali dallo stesso network model
                Set<Integer> addedCardIds = new HashSet<>();

                for (CardDTO card : player.getMyCharacters()) {
                    if (addedCardIds.add(card.getId())) {
                        CardGUI cardGUI = createDrawableCard(card.getId());
                        insertCard(cardGUI.getCardId());
                    }
                }
                for (CardDTO card : player.getMyBuildings()) {
                    if (addedCardIds.add(card.getId())) {
                        CardGUI cardGUI = createDrawableCard(card.getId());
                        insertCard(cardGUI.getCardId());
                    }
                }

            } else {
                VillageOverlay overlay = opponentOverlays.get(player.getNickname());
                if(overlay != null){
                    overlay.cols.values().forEach(col -> col.getChildren().clear());

                    Set<Integer> oppAddedIds = new HashSet<>();

                    for (CardDTO card : player.getMyCharacters()) {
                        if (oppAddedIds.add(card.getId())) {
                            VBox col = overlay.cols.get(card.getType());
                            if (col != null) {
                                CardGUI cardGUI = new CardGUI(card.getId());
                                cardGUI.setOnMouseClicked(null);
                                col.getChildren().add(cardGUI);
                            }
                        }
                    }
                    for (CardDTO card : player.getMyBuildings()) {
                        if (oppAddedIds.add(card.getId())) {
                            VBox col = overlay.cols.get(card.getType());
                            if (col != null) {
                                CardGUI cardGUI = new CardGUI(card.getId());
                                cardGUI.setOnMouseClicked(null);
                                col.getChildren().add(cardGUI);
                            }
                        }
                    }
                    overlay.content.getChildren().forEach(n -> {
                        boolean hasChildren = !(((VBox)n).getChildren().isEmpty());
                        n.setVisible(hasChildren);
                        n.setManaged(hasChildren);
                    });
                }
            }
        }
    }
    /**
     * Clears and rebuilds the upper and lower draw-card rows from the current
     * virtual-model snapshot, then refreshes drawable highlights.
     *
     * <p>Must be called on the FX Application Thread.</p>
     */
    private void updateCardRows() {
        lowerRowChar.getChildren().clear();
        upperRowChar.getChildren().clear();
        upperRowBuildsEv.getChildren().clear();
        lowerRowBuildsEv.getChildren().clear();

        for (CardDTO dto : viewGUI.getModel().getUpperList())
            addCardToRow(dto, upperRowChar, upperRowBuildsEv);

        for (CardDTO dto : viewGUI.getModel().getLowerList())
            addCardToRow(dto, lowerRowChar, lowerRowBuildsEv);

        refreshDrawableHighlight();
    }
    /**
     * Adds a single card DTO to the appropriate draw-row {@link HBox},
     * routing character cards to {@code charRow} and buildings/events to
     * {@code buildEvRow}. Character cards also have sliding behaviour enabled.
     *
     * @param dto        card data transfer object
     * @param charRow    target row for character-type cards
     * @param buildEvRow target row for building- and event-type cards
     */
    private void addCardToRow(CardDTO dto, HBox charRow, HBox buildEvRow) {
        CardGUI cardGUI = createDrawableCard(dto.getId());
        if (dto.getType().isCharacter()) {
            charRow.getChildren().add(cardGUI);
            cardGUI.setViewOrder(charRow.getChildren().size() - 1);
            cardGUI.showSliding(charRow, buildEvRow);
        } else {
            buildEvRow.getChildren().add(cardGUI);
        }
    }
    /**
     * Updates the age, turn, current-player, and phase labels in the HUD.
     *
     * @param age        current era number
     * @param turn       current round number
     * @param nickname   nickname of the player whose turn it is
     * @param phaseState current game phase, used to derive the display label
     */
    private void updateInfos(int age, int turn, String nickname, GamePhaseEnum phaseState) {
        txtAge.setText("era: " + age);
        txtRound.setText("round: " + turn);
        txtCurrPlayer.setVisible(true);
        txtCurrPlayer.setManaged(true);
        txtCurrPlayerNick.setText(nickname.toLowerCase());
        String color = opponentColors.getOrDefault(nickname, "#ffffff");
        txtCurrPlayerNick.setFill(Color.web(color));
        txtPhase.setText("fase: " + phaseToLabel(phaseState));
    }
    /**
     * Converts a {@link GamePhaseEnum} value to its Italian display label.
     *
     * @param currentPhase the phase to convert; {@code null} returns {@code "-"}
     * @return a short Italian label suitable for display in the phase HUD field
     */
    private String phaseToLabel(GamePhaseEnum currentPhase) {
        if (currentPhase == null)
            return "-";
        return switch (currentPhase) {
            case SETUP_PHASE -> "setup";
            case DRAW_PHASE -> "pesca";
            case OPTIONAL_DRAW_PHASE -> "pesca aggiuntiva";
            case END_TURN -> "fine turno";
            case END_ROUND -> "fine round";
            case PLAY_EVENT -> "evento in corso";
            case END_GAME -> "fine partita";
            case NONE -> "";
        };
    }
    /**
     * Performs the initial population of the draw-card rows, including the
     * deal animation played at game start.
     *
     * <p>Must be called on the FX Application Thread.</p>
     *
     * @param cardsUp   cards to place in the upper row
     * @param cardsDown cards to place in the lower row
     */
    private void initCards(List<CardDTO> cardsUp, List<CardDTO> cardsDown) {
        assertFxThread();
        upperRowChar.getChildren().clear();
        upperRowBuildsEv.getChildren().clear();
        lowerRowChar.getChildren().clear();
        lowerRowBuildsEv.getChildren().clear();

        List<CardGUI> toAnimate = new ArrayList<>();
        cardsUp.forEach(dto   -> addCardToRowWithAnimation(dto, upperRowChar, upperRowBuildsEv, toAnimate));
        cardsDown.forEach(dto -> addCardToRowWithAnimation(dto, lowerRowChar, lowerRowBuildsEv, toAnimate));

        animateDeal(toAnimate);
        refreshDrawableHighlight();
    }
    /**
     * Adds a single card to the appropriate draw row and registers it for the
     * deal animation.
     *
     * @param dto        card data transfer object
     * @param charRow    target row for character-type cards
     * @param buildEvRow target row for building- and event-type cards
     * @param toAnimate  accumulator list; the created {@link CardGUI} is appended here
     */
    private void addCardToRowWithAnimation(CardDTO dto, HBox charRow, HBox buildEvRow, List<CardGUI> toAnimate) {
        CardGUI cardGUI = createDrawableCard(dto.getId());
        if (dto.getType().isCharacter()) {
            charRow.getChildren().add(cardGUI);
            cardGUI.setViewOrder(charRow.getChildren().size() - 1);
            cardGUI.showSliding(charRow, buildEvRow);
        } else {
            buildEvRow.getChildren().add(cardGUI);
        }
        toAnimate.add(cardGUI);
    }
    /**
     * Returns the {@link CardGUI} for the given card ID, creating and
     * registering a new one if it does not yet exist in {@link #cardMap}.
     *
     * <p>Newly created cards have a click handler that delegates to
     * {@link #handleDrawClick(CardGUI)}.</p>
     *
     * <p>Must be called on the FX Application Thread.</p>
     *
     * @param cardId the card identifier
     * @return the existing or newly created {@link CardGUI}
     */
    private CardGUI createDrawableCard(int cardId) {
        assertFxThread();

        if (cardMap.containsKey(cardId)) return cardMap.get(cardId);

        CardGUI card = new CardGUI(cardId);
        cardMap.put(cardId, card);
        card.setOnMouseClicked(event -> handleDrawClick(card));
        return card;
    }
    /**
     * Handles a click on a drawable card: temporarily disables the card,
     * dispatches a draw request to the server on a background thread, and
     * re-enables the card when the request completes (success or failure).
     *
     * @param card the card the player intends to draw
     */
    private void handleDrawClick(CardGUI card) {
        assertFxThread();

        card.setDisable(true);

        Task<Void> drawTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                viewGUI.getClient().draw(card.getCardId());
                return null;
            }
        };

        drawTask.setOnFailed(event -> {
            card.setDisable(false);
            System.err.println("[GameController] Draw fallito: "
                    + drawTask.getException().getMessage());
        });

        drawTask.setOnSucceeded(event -> {
            card.setDisable(false);
        });

        new Thread(drawTask, "draw-action").start();
    }
    /**
     * Assigns a CSS hex color to each player and creates their {@link TotemGUI}
     * pawn node.
     *
     * <p>Colors are derived from the {@link it.polimi.ingsw.enumerations.ColorPawnEnum}
     * assigned to the player by the server during lobby setup.</p>
     *
     * <p>Must be called on the FX Application Thread.</p>
     *
     * @param players the full list of players in the match
     */
    private void initPlayersColorsAndPawns(List<PlayerDTO> players) {
        assertFxThread();

        for (PlayerDTO player : players) {
            String hex = switch (player.getColor()) {
                case BLUE -> "#3498db";
                case YELLOW -> "#f1c40f";
                case WHITE -> "#ffffff";
                case ORANGE -> "#e67e22";
                default -> "#9b59b6";
            };
            opponentColors.put(player.getNickname(), hex);
        }

        for (PlayerDTO player : players) {
            TotemGUI newTotem = new TotemGUI(player.getColor());
            totemMap.put(player.getNickname(), newTotem);
        }
    }
    /**
     * Handles a click on a board tile: disables all tiles to prevent concurrent
     * move requests, dispatches the move to the server on a daemon background
     * thread, and re-enables tiles when the request completes.
     *
     * <p>Calls are ignored while {@link #loadingMove} is {@code true}.</p>
     *
     * @param tileGUI the tile that was clicked
     * @param index   the 0-based position index of the tile, sent to the server
     */
    private void handleMoveClick(TileGUI tileGUI, int index) {
        assertFxThread();

        if (loadingMove) return;

        loadingMove = true;


        tileMap.values().forEach(t -> t.setDisable(true));

        Task<Void> moveTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                viewGUI.getClient().move(index);
                return null;
            }
        };

        moveTask.setOnSucceeded(ignored -> {
            loadingMove = false;
            tileMap.values().forEach(t -> t.setDisable(false));
        });

        moveTask.setOnFailed(event -> {
            loadingMove = false;
            tileMap.values().forEach(t -> t.setDisable(false));

            Throwable ex = moveTask.getException();
            System.err.println("[GameController] Move fallita lato rete: "
                    + (ex != null ? ex.getMessage() : "Errore sconosciuto"));
        });

        Thread thread = new Thread(moveTask, "move-action");
        thread.setDaemon(true);
        thread.start();
    }
    /**
     * Builds the board: creates and adds the {@link DeckGUI}, the
     * {@link QTileGUI} (queue), and one {@link TileGUI} per entry in
     * {@code tileList} to the {@link #tiles} {@link HBox}.
     *
     * <p>Each tile is wired to {@link #handleMoveClick(TileGUI, int)} and
     * registered in {@link #tileMap}.</p>
     *
     * <p>Must be called on the FX Application Thread.</p>
     *
     * @param numPlayers number of players in the match; forwarded to
     *                   {@link QTileGUI} to set the correct capacity display
     * @param tileList   ordered list of tile DTOs from the virtual model
     */
    private void initBoard(int numPlayers, List<TileDTO> tileList) {
        assertFxThread();
        tiles.getChildren().clear();

        deckGUI = new DeckGUI(
                viewGUI.getModel().getDeckSize(),
                viewGUI.getModel().currAge
        );
        deckGUI.setTranslateX(-50);
        CursorManager.makeNodesHoverable(deckGUI);
        tiles.getChildren().add(deckGUI);
        qTileGUI = new QTileGUI(numPlayers);

        tiles.getChildren().add(qTileGUI);

        for (int i = 0; i < tileList.size(); i++) {
            TileDTO tile = tileList.get(i);
            TileGUI nuovaTile = new TileGUI(tile.getId());
            final int index = i;
            nuovaTile.setOnMouseClicked(event -> handleMoveClick(nuovaTile, index));
            tileMap.put(tile.getId(), nuovaTile);
            tiles.getChildren().add(nuovaTile);
        }
    }
    /**
     * Places every player's totem on the queue tile at game start.
     *
     * <p>Must be called on the FX Application Thread, after
     * {@link #initPlayersColorsAndPawns(List)} and {@link #initBoard(int, List)}
     * have both completed.</p>
     *
     * @param tileList the queue tile list (currently used only for length reference)
     */
    private void initQueue(List<TileDTO> tileList) {
        assertFxThread();
        QTileGUI qTile = qTileGUI;
        for (int i = 0; i < viewGUI.getModel().getPlayers().size(); i++) {
            TotemGUI totem = totemMap.get(viewGUI.getModel().getPlayers().get(i).getNickname());
            if (totem != null) {
                qTile.addPawn(totem);
            }
        }
    }
    /**
     * Populates the stats display for all players from the initial stats
     * snapshot. The local player's stats are written to the HUD text fields;
     * each opponent gets a new stats row in the {@link StatsPanel}.
     *
     * <p>Must be called on the FX Application Thread.</p>
     *
     * @param playerStats initial stats list, one entry per player
     */
    private void initStats(List<PlayerStatsDTO> playerStats) {
        assertFxThread();

        String myNickname = viewGUI.getNickname();

        for (PlayerStatsDTO stats : playerStats) {
            String name = stats.getNickname();
            if (name.equals(myNickname)) {
                txtNickname.setText(name);
                String color = opponentColors.getOrDefault(name, "#ffffff");
                txtNickname.setFill(Color.web(color));
                txtNickname.setStyle("-fx-font-weight: bold;");
                updateMyStats(stats);
            } else {
                addOpponentRows(name, stats);
            }
        }
    }
    /**
     * Appends a stats row for a single opponent to the {@link #opponents}
     * {@link VBox}, including a "+" button that opens their village overlay.
     *
     * <p>Must be called on the FX Application Thread.</p>
     *
     * @param name  the opponent's nickname
     * @param stats their current stats DTO
     */
    private void addOpponentRows(String name, PlayerStatsDTO stats) {
        assertFxThread();

        String color = opponentColors.get(name);

        if (color == null) {
            System.err.println("[GameController] colore non trovato per: " + name);
            color = "#ffffff";
        }

        Text nameText = new Text(name);
        nameText.setFill(Color.web(color));
        nameText.setStyle("-fx-font-weight: bold;");

        Button plusButton = new Button("+");
        plusButton.setStyle(
                "-fx-text-fill: rgb(254,242,210);" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 24px;" +
                        "-fx-background-color: transparent;" +
                        "-fx-padding: 0 2 5 4;"
        );
        CursorManager.makeNodesHoverable(plusButton);
        plusButton.setOnAction(e -> viewVillage(name));
        Tooltip.install(plusButton, new Tooltip("Visualizza villaggio di " + name));

        HBox nameRow = new HBox(4, nameText, plusButton);
        nameRow.setAlignment(Pos.CENTER_LEFT);
        nameRow.setPickOnBounds(false);

        Text statsText = buildStatsText(stats);
        opponentStatsNodes.put(name, statsText);

        opponents.getChildren().addAll(nameRow, statsText);
    }

    /**
     * Updates the stats display for all players.
     *
     * <p>If the game is entering an event phase and no pre-event snapshot
     * exists yet, one is captured now from the virtual model so that the
     * event banner can later compute the per-player delta.</p>
     *
     * <p>The local player's stats are written to the HUD text fields; each
     * opponent's stats row is updated in place via
     * {@link #opponentStatsNodes}.</p>
     *
     * <p>Must be called on the FX Application Thread.</p>
     *
     * @param playerStats updated stats list, one entry per player
     */
    public void updateStats(List<PlayerStatsDTO> playerStats) {
        assertFxThread();
        GamePhaseEnum phase = viewGUI.getModel().getCurrentPhase();
        boolean isEventPhase = phase == GamePhaseEnum.END_ROUND || phase == GamePhaseEnum.PLAY_EVENT;
        if (!eventBannerShowing && statsBeforeEvent.isEmpty() && isEventPhase) {
            statsBeforeEvent = Optional.of(
                    viewGUI.getModel().getPlayerStats().stream()
                            .collect(Collectors.toMap(PlayerStatsDTO::getNickname, s -> s))
            );
        }
        String myNickname = viewGUI.getNickname();

        for (PlayerStatsDTO stats : playerStats) {
            if (stats.getNickname().equals(myNickname)) {
                updateMyStats(stats);
            } else {
                Text t = opponentStatsNodes.get(stats.getNickname());
                if (t != null) t.setText(formatStats(stats));
            }
        }
    }
    /**
     * Writes the local player's current stats into the HUD text fields
     * ({@link #txtPP}, {@link #txtFood}, {@link #txtStars},
     * {@link #txtBuildDisc}, {@link #txtFeastDisc}).
     *
     * <p>Must be called on the FX Application Thread.</p>
     *
     * @param stats the local player's current stats
     */
    private void updateMyStats(PlayerStatsDTO stats) {
        assertFxThread();

        txtPP.setText(String.valueOf(stats.getPPs()));
        txtFood.setText(String.valueOf(stats.getnFood()));
        txtStars.setText(String.valueOf(stats.getnStars()));
        txtBuildDisc.setText(String.valueOf(stats.getTotBuildDisc()));
        txtFeastDisc.setText(String.valueOf(stats.getFoodDiscount()));
    }
    /**
     * Opens the village overlay for the given opponent in the global overlay
     * panel.
     *
     * <p>Must be called on the FX Application Thread.</p>
     *
     * @param nickname the opponent whose village should be displayed
     */
    private void viewVillage(String nickname) {
        assertFxThread();

        VillageOverlay overlay = opponentOverlays.get(nickname);
        if(overlay == null){
            System.err.println("[GameController] Overlay non trovato per " + nickname);
            return;
        }
        showOverlay(overlay.root);
    }
    /**
     * Reads the active flags from the virtual model for the local player and
     * adds their icons to {@link #flagBox} via {@link #addFlagIcon(String)}.
     *
     * <p>Called once during {@link #initStructure()}.</p>
     */
    private void initFlags() {
        for(PlayerStatusDTO status : viewGUI.getModel().getPlayerStatuses()){
            if(status.getNickname().equals(viewGUI.getNickname())){
                putFlags(status);
            }
        }
    }
    /**
     * Pre-loads all flag icon {@link Image}s into {@link #flagImages}, keyed
     * by flag name. Called once at the start of {@link #refreshBoard()}, before
     * {@link #initStructure()}.
     */
    private void uploadFlags() {
        flagImages.put("protection", new Image("images/icons/Protection_flag.png", true));
        flagImages.put("double", new Image("images/icons/DoubleShamanIncome.png", true));
        flagImages.put("extra", new Image("images/icons/Extraflag.png", true));
        flagImages.put("paint", new Image("images/icons/Paint_flag.png", true));
        flagImages.put("painter", new Image("images/icons/Discount_painter.png", true));
        flagImages.put("crafter", new Image("images/icons/DiscountCrafter.png", true));
        flagImages.put("gatherer", new Image("images/icons/Discount_gatherer.png", true));
        flagImages.put("hunt", new Image("images/icons/Hunt_flag.png", true));
    }
    /**
     * Adds the icon for the named flag to {@link #flagBox}, if it is not
     * already present. The icon is sized to 25 px height (preserving aspect
     * ratio) and equipped with a custom tooltip loaded from
     * {@link FlagRegistry}.
     *
     * <p>Must be called on the FX Application Thread.</p>
     *
     * @param flag the flag name (e.g. {@code "hunt"}, {@code "protection"});
     *             must be a key in {@link #flagImages}
     */
    private void addFlagIcon(String flag) {
        assertFxThread();
        if (activeFlags.contains(flag)) return;
        activeFlags.add(flag);
        ImageView icon = new ImageView(flagImages.get(flag));
        icon.setFitHeight(25);
        icon.setPreserveRatio(true);
        flagBox.getChildren().add(icon);
        creaTooltip(icon, flag, FlagRegistry.getDescription(flag));
    }
    /**
     * Replaces the content of {@link #overlayContent} with {@code content} and
     * makes {@link #globalOverlay} visible. Clicking on the semi-transparent
     * backdrop (i.e. directly on {@code globalOverlay} rather than on
     * {@code content}) dismisses the overlay.
     *
     * <p>Must be called on the FX Application Thread.</p>
     *
     * @param content the node to display inside the overlay
     */
    private void showOverlay(Node content) {
        assertFxThread();
        overlayContent.getChildren().setAll(content);
        globalOverlay.setVisible(true);
        globalOverlay.setManaged(true);
        globalOverlay.setOnMouseClicked(e -> {
            if (e.getTarget().equals(globalOverlay)) hideOverlay();
        });
    }
    /** Hides the global overlay and clears its content. Equivalent to {@link #hideOverlay(Node)} with {@code null}. */
    private void hideOverlay() {
        hideOverlay(null);
    }
    /**
     * Hides the global overlay. If {@code fallback} is non-null, the overlay
     * is not closed but its content is replaced with {@code fallback} instead.
     *
     * <p>Also stops any running {@link #dotsAnimation} and resets
     * {@link #eventBannerShowing} to {@code false}.</p>
     *
     * <p>Must be called on the FX Application Thread.</p>
     *
     * @param fallback replacement content, or {@code null} to close the overlay
     */
    private void hideOverlay(Node fallback) {
        assertFxThread();
        eventBannerShowing = false;
        if (fallback != null) {
            showOverlay(fallback);
        } else {
            if (dotsAnimation != null) {
                dotsAnimation.stop();
                dotsAnimation = null;
            }
            globalOverlay.setVisible(false);
            globalOverlay.setManaged(false);
            overlayContent.getChildren().clear();
        }
    }
    /**
     * Recomputes and applies highlight states to all cards and tiles.
     *
     * <p>Highlights are shown only when it is the local player's turn:</p>
     * <ul>
     *   <li>During {@code DRAW_PHASE} / {@code OPTIONAL_DRAW_PHASE}: cards
     *       that the player can afford and are in the correct row are
     *       highlighted; arrow indicators on the player's tile show remaining
     *       up/down draw counts.</li>
     *   <li>During {@code SETUP_PHASE}: unoccupied tiles are highlighted as
     *       valid placement targets.</li>
     * </ul>
     * <p>The skip button is shown only when the current draw action is
     * optional (as indicated by {@link ActionsDTO#isOptionalFlag()}).</p>
     *
     * <p>Must be called on the FX Application Thread.</p>
     */
    private void refreshDrawableHighlight() {
        assertFxThread();

        cardMap.values().forEach(c -> c.setHighlight(false));
        tileMap.values().forEach(t -> t.setHighlight(false));
        tileMap.values().forEach(t -> t.setArrowHighlight(0, 0));

        boolean isMyTurn = viewGUI.getNickname().equals(viewGUI.getModel().getCurrPlayer());
        if (!isMyTurn){
            skipButton.setVisible(false);
            skipButton.setManaged(false);
            return;
        }

        GamePhaseEnum phase = viewGUI.getModel().getCurrentPhase();

        if (phase.equals(GamePhaseEnum.DRAW_PHASE) || phase.equals(GamePhaseEnum.OPTIONAL_DRAW_PHASE)) {
            ActionsDTO actions = viewGUI.getModel().getToDoActions();

            viewGUI.getModel().getUpperList().stream()
                    .filter(dto -> cardMap.containsKey(dto.getId()))
                    .forEach(dto -> cardMap.get(dto.getId())
                            .setHighlight(actions.getUpDraws() > 0 && isDrawable(dto)));

            viewGUI.getModel().getLowerList().stream()
                    .filter(dto -> cardMap.containsKey(dto.getId()))
                    .forEach(dto -> cardMap.get(dto.getId())
                            .setHighlight(actions.getDownDraws() > 0 && isDrawable(dto)));

            // Arrow highlight: indicatori separati per upDraw (oro) e downDraw (azzurro).
            TileGUI myTile = totemPos.get(viewGUI.getNickname());
            if (myTile != null) {
                myTile.setArrowHighlight(actions.getUpDraws(), actions.getDownDraws());
            }

        } else if (phase.equals(GamePhaseEnum.SETUP_PHASE)) {
            tileMap.values().stream()
                    .filter(t -> !t.isOccupied())
                    .forEach(t -> t.setHighlight(true));
        }
        boolean canSkip = (phase.equals(GamePhaseEnum.DRAW_PHASE) || phase.equals(GamePhaseEnum.OPTIONAL_DRAW_PHASE))
                && viewGUI.getModel().getToDoActions() != null
                && viewGUI.getModel().getToDoActions().isOptionalFlag();
        skipButton.setVisible(canSkip);
        skipButton.setManaged(canSkip);
    }
    /**
     * Returns {@code true} if the local player can draw the given card.
     *
     * <p>Buildings are drawable only when the player has enough food to cover
     * their cost after applying the build discount. Character cards are
     * always drawable; event cards (Feast, Hunt, Ritual, StonePainting) are
     * never directly drawable by the player.</p>
     *
     * @param c the card DTO to evaluate
     * @return {@code true} if the card can currently be drawn by the local player
     */
    private boolean isDrawable(CardDTO c) {
        CardTypeEnum type = c.getType();
        int id = c.getId();
        PlayerStatsDTO ownerStats = viewGUI.getModel().getPlayerStats().stream()
                .filter(s -> s.getNickname().equals(viewGUI.getNickname()))
                .findFirst()
                .orElse(null);

        if (ownerStats == null) return false;
        if (type.equals(CardTypeEnum.BUILDING)) {
            int actualCost = Math.max(0, CardRegistry.getCost(id) - ownerStats.getTotBuildDisc());
            return ownerStats.getnFood() >= actualCost;
        }

        return !type.equals(CardTypeEnum.FEAST) && !type.equals(CardTypeEnum.HUNT) && !type.equals(CardTypeEnum.RITUAL) && !type.equals(CardTypeEnum.STONE_PAINTING);
    }
    /**
     * Displays a full-screen "waiting for other players" overlay with an
     * animated ellipsis. A duplicate menu button is shown inside the overlay
     * so the player can quit even while waiting.
     *
     * <p>Must be called on the FX Application Thread.</p>
     */
    public void showReconnectionWaiting() {
        assertFxThread();
        Text msg = new Text("In attesa degli altri giocatori");
        msg.setFill(Color.WHITE);
        msg.setStyle("-fx-font-size: 28; -fx-font-weight: bold;");

        dotsAnimation = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(Duration.millis(500), e -> {
                    String current = msg.getText().replaceAll("\\.+$", "");
                    int dotCount = msg.getText().length() - current.length();
                    msg.setText(current + ".".repeat((dotCount % 3) + 1));
                })
        );
        dotsAnimation.setCycleCount(javafx.animation.Animation.INDEFINITE);
        dotsAnimation.play();

        ImageView overlayMenu = new ImageView(menuBtn.getImage());
        overlayMenu.setFitHeight(menuBtn.getFitHeight());
        overlayMenu.setFitWidth(menuBtn.getFitWidth());
        overlayMenu.setPreserveRatio(true);
        overlayMenu.setPickOnBounds(true);
        overlayMenu.getStyleClass().addAll(menuBtn.getStyleClass());
        CursorManager.makeNodesHoverable(overlayMenu);

        StackPane wrapper = new StackPane();
        wrapper.setAlignment(Pos.CENTER);
        wrapper.prefWidthProperty().bind(globalOverlay.widthProperty());
        wrapper.prefHeightProperty().bind(globalOverlay.heightProperty());

        StackPane.setAlignment(msg, Pos.CENTER);
        StackPane.setAlignment(overlayMenu, Pos.TOP_RIGHT);
        StackPane.setMargin(overlayMenu, new Insets(0, 10, 0, 0));
        wrapper.getChildren().addAll(msg, overlayMenu);

        overlayMenu.setOnMouseClicked(e -> onMenuBtnClick(e, wrapper));
        showOverlay(wrapper);
    }
    /**
     * Slides the current event banner out of view (upward), then hides the
     * overlay. After hiding, the next pending event banner is shown (if any),
     * or the next pending era animation is played.
     *
     * <p>Must be called on the FX Application Thread.</p>
     */
    private void hideBanner(){
        assertFxThread();
        if (overlayContent.getChildren().isEmpty()) {
            hideOverlay();
            return;
        }
        javafx.scene.Node banner = overlayContent.getChildren().get(0);
        javafx.animation.TranslateTransition slideOut =
                new javafx.animation.TranslateTransition(Duration.millis(300), banner);
        slideOut.setToY(-300);
        slideOut.setInterpolator(javafx.animation.Interpolator.EASE_IN);
        slideOut.setOnFinished(e -> {
            hideOverlay();
            // Se c'è un evento accodato (es. eventi finali dopo gli eventi del round),
            Map.Entry<EventDTO, List<PlayerStatsDTO>> next = pendingEventBanners.pollFirst();
            if (next != null) {
                showEventBanner(next.getKey(), next.getValue());
            } else {
                // Nessun altro banner eventi: mostra le animazioni era accodate
                Integer pendingAge = pendingEraAnimations.pollFirst();
                if (pendingAge != null) {
                    showEraCurtainAnimation(pendingAge);
                }
            }
        });
        slideOut.play();
    }
    /**
     * Handles a click on the skip button: hides the button immediately,
     * dispatches a skip request to the server on a background thread, and
     * restores the button if the request fails.
     */
    private void handleSkipClick(){
        skipButton.setVisible(false);
        skipButton.setManaged(false);
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                viewGUI.getClient().skip();
                return null;
            }
        };
        task.setOnFailed(ev -> Platform.runLater(()->{
            skipButton.setVisible(true);
            skipButton.setManaged(true);
        }));
        new Thread(task, "skip-action").start();
    }
    /**
     * Triggers the deal animation for all cards currently in the draw rows
     * at the start of a new round.
     *
     * <p>Must be called on the FX Application Thread.</p>
     */
    private void onEndRound() {
        assertFxThread();

        List<CardGUI> toAnimate = new ArrayList<>();
        for (CardDTO dto : viewGUI.getModel().getUpperList()) {
            CardGUI card = cardMap.get(dto.getId());
            if (card != null) toAnimate.add(card);
        }
        for (CardDTO dto : viewGUI.getModel().getLowerList()) {
            CardGUI card = cardMap.get(dto.getId());
            if (card != null) toAnimate.add(card);
        }

        animateDeal(toAnimate);
    }
    /**
     * Builds the stat-delta summary line for a single player, showing the
     * before/after values and the signed difference for food, prestige points,
     * and stars.
     *
     * @param before stats snapshot captured before the event
     * @param after  stats snapshot captured after the event
     * @return a formatted string such as
     *         {@code "cibo: 3 → 5  (+2)   |   pp: 10 → 12  (+2)"}, or an
     *         empty string if nothing changed
     */
    private String buildDeltaLine(PlayerStatsDTO before, PlayerStatsDTO after) {
        List<String> parts = new ArrayList<>();
        addDelta(parts, "cibo",   before.getnFood(),  after.getnFood());
        addDelta(parts, "pp",     before.getPPs(),     after.getPPs());
        addDelta(parts, "stelle", before.getnStars(),  after.getnStars());
        return String.join("   |   ", parts);
    }
    /**
     * Appends a formatted delta entry to {@code parts} if the value changed.
     *
     * @param parts  accumulator list of delta strings
     * @param label  display label for the stat (e.g. {@code "cibo"})
     * @param before value before the event
     * @param after  value after the event
     */
    private void addDelta(List<String> parts, String label, int before, int after) {
        int diff = after - before;
        if (diff == 0) return;
        String sign = diff > 0 ? "+" : "";
        parts.add(label + ": " + before + " → " + after + "  (" + sign + diff + ")");
    }
    /**
     * Creates a styled {@link Text} node displaying the given player stats.
     *
     * @param stats the stats to format
     * @return a white {@link Text} node ready to be added to the scene graph
     */
    private Text buildStatsText(PlayerStatsDTO stats) {
        Text t = new Text(formatStats(stats));
        t.setFill(Color.web("#FFFFFF"));
        t.setStyle("-fx-font-size: 20px;");
        return t;
    }
    /**
     * Formats a {@link PlayerStatsDTO} as a compact multi-line string for
     * display in the stats panel.
     *
     * @param stats the stats to format
     * @return a formatted string containing food, PP, stars, and discounts
     */
    private static String formatStats(PlayerStatsDTO stats) {
        return "food: " + stats.getnFood()
                + "  pp: " + stats.getPPs()
                + "  stars: " + stats.getnStars()
                + "\nsconto builder: " + stats.getTotBuildDisc()
                + " sconto gatherer: " + stats.getFoodDiscount();
    }
    /**
     * Asserts that the current thread is the JavaFX Application Thread.
     *
     * @throws IllegalStateException if called from any other thread
     */
    private static void assertFxThread() {
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException(
                    "Questo metodo deve girare sull'FX Application Thread, "
                            + "ma è stato chiamato da: " + Thread.currentThread().getName()
            );
        }
    }
    /**
     * Holds the JavaFX nodes that make up an opponent's village overlay:
     * the root container, the card-display {@link HBox}, and the per-type
     * column map.
     */
    private static class VillageOverlay {
        final VBox root;
        final HBox content;
        final Map<CardTypeEnum, VBox> cols;
        VillageOverlay(VBox root, HBox content, Map<CardTypeEnum, VBox> cols) {
            this.root = root;
            this.content = content;
            this.cols = cols;
        }
    }
    /**
     * Plays a staggered deal animation for a list of cards: each card fades
     * in showing its back, then flips to reveal its front via paired
     * {@link ScaleTransition}s. Cards are animated with a base delay of
     * {@code 80 ms × index} to produce a cascading effect.
     *
     * @param cards the cards to animate, in display order
     */
    private void animateDeal(List<CardGUI> cards) {
        int baseDelayMs = 80;

        for (int i = 0; i < cards.size(); i++) {
            CardGUI card = cards.get(i);
            card.showBack();
            card.setOpacity(0);

            int delayMs = i * baseDelayMs;

            PauseTransition wait = new PauseTransition(Duration.millis(delayMs));
            wait.setOnFinished(e -> dealSingleCard(card));
            wait.play();
        }
    }
    /**
     * Animates a single card's deal sequence: fade in → scale to zero
     * (showing back) → swap to front image → scale back to full size.
     *
     * @param card the card to animate
     */
    private void dealSingleCard(CardGUI card) {
        FadeTransition fadeIn = new FadeTransition(Duration.millis(120), card);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(130), card);
        scaleDown.setFromX(1);
        scaleDown.setToX(0);

        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(130), card);
        scaleUp.setFromX(0);
        scaleUp.setToX(1);

        scaleDown.setOnFinished(e -> {
            card.showFront();
            scaleUp.play();
        });

        SequentialTransition seq = new SequentialTransition(fadeIn, scaleDown);
        seq.play();
    }
    /**
     * Returns whether an event banner is currently being displayed.
     *
     * @return {@code true} if an event banner is on screen
     */
    public boolean isEventBannerShowing(){
        return eventBannerShowing;
    }
}