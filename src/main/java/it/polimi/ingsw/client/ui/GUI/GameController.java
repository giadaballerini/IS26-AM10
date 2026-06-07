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

    private final ViewGUI viewGUI;

    private boolean initialized = false;

    private int lastTurn = -1;

    private final Map<CardTypeEnum, VBox> cardVBoxMap = new HashMap<>();

    private final Map<Integer, CardGUI> cardMap = new HashMap<>();


    private final Map<Integer, TileGUI> tileMap = new HashMap<>();

    private final Map<String, TotemGUI> totemMap = new HashMap<>();

    private final Map<String, Text> opponentStatsNodes = new HashMap<>();

    private final Map<String, String> opponentColors = new HashMap<>();

    private final Map<String, TileGUI> totemPos = new HashMap<>();

    private QTileGUI qTileGUI;

    private DeckGUI deckGUI;

    private final Map<String, VillageOverlay> opponentOverlays = new HashMap<>();

    private boolean loadingMove = false;

    private VillagePanel villagePanel;

    private StatsPanel statsPanel;

    private javafx.animation.Timeline dotsAnimation;
    private final Map<String, Image> flagImages = new HashMap<>();
    private final Set<String> activeFlags = new HashSet<>();
    private final List<CardDTO> pingEvents = new ArrayList<>();
    private PauseTransition eventBannerDelay;
    private boolean eventBannerShowing = false;
    private Optional<Map<String, PlayerStatsDTO>> statsBeforeEvent = Optional.empty();

    /**
     * Coda degli eventi in attesa di essere mostrati.
     * Serve perché al turno 10 il server manda DUE EventMessage consecutivi
     * (eventi del round + eventi finali): il secondo non va scartato ma accodato
     * e mostrato subito dopo che il primo banner si chiude.
     */
    private final Deque<Map.Entry<EventDTO, List<PlayerStatsDTO>>> pendingEventBanners = new ArrayDeque<>();
    /** Coda delle animazioni era da mostrare dopo che tutti i banner eventi sono stati consumati. */
    private final Deque<Integer> pendingEraAnimations = new ArrayDeque<>();


    public GameController(ViewGUI viewGUI) {
        this.viewGUI = viewGUI;
    }

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

    private void showConfirm(String title, String caption, Runnable onConfirm) {
        showConfirm(title, caption, onConfirm, null);
    }

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
     * Refreshes the board to reflect the state of {@link it.polimi.ingsw.client.VirtualModel}, the first time it is called it initialize the structure
     * from the second time on, it only synchronizes with the state of the game
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

    public void onCurrPlayerUpdate(String nickname) {
        assertFxThread();
        txtCurrPlayer.setVisible(true);
        txtCurrPlayer.setManaged(true);
        txtCurrPlayerNick.setText(nickname.toLowerCase());
        String color = opponentColors.getOrDefault(nickname, "#ffffff");
        System.out.println("[DEBUG] currPlayer=" + nickname + " color=" + color + " opponentColors=" + opponentColors);
        txtCurrPlayerNick.setFill(Color.web(color));
    }

    public void onPhaseUpdate(PhaseDTO phaseDTO) {
        assertFxThread();
        txtPhase.setText("fase: " + phaseToLabel(phaseDTO.getPhase()));
        refreshDrawableHighlight();
    }

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
     * Animazione "tendine" al cambio era: due pannelli scuri scendono dall'alto
     * e salgono dal basso, si incontrano al centro, appare il testo, poi si riaprono.
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


    private String toRoman(int n) {
        return switch(n){
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            default -> "";
        };
    }



    public void onStatusUpdate(PlayerStatusDTO status) {
        assertFxThread();
        if (!status.getNickname().equals(viewGUI.getNickname())) return;

        putFlags(status);
    }

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

    public void onOpponentDraw(CardDTO card, String nickname) {
        assertFxThread();

        String color = opponentColors.getOrDefault(nickname, "#ffffff");
        String typeName = card.getType().toString().toLowerCase();

        showNotification(nickname + " ha pescato: " + typeName, color);

        updateCardRows();

        updateVillages();
    }

    public void showDrawable() {
        assertFxThread();
        refreshDrawableHighlight();
    }

    public void notifySkip(String nickname) {
        assertFxThread();

        String color = opponentColors.getOrDefault(nickname, "#ffffff");

        showNotification(nickname + " ha pescato saltato il turno.", color);
    }

    /**
     * Displays the banner with the events, if the banner is already showing the event is queued
     * @param events DTO passed so that it can be displayed
     * @param statsBefore used for the difference between stats before and after the events.
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

    /** Builds and shows the banner for a single EventDTO. */
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
     * Setup strutturale della scena: layout fisso, board, coda, pawn, overlay avversari.
     * Non dipende dallo stato di gioco corrente (mani, fase, stats).
     * Chiamato una sola volta quando initialized=false.
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
     * Sincronizza la GUI con lo stato corrente del VirtualModel.
     * Chiamato sempre da  refreshBoard(), sia al primo avvio che alla riconnessione.
     * Gestisce: carte pescabili, mani giocatori, stats, etichette fase/turno.
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
     * Crea e collega il pannello Village flottante alla radice della scena.
     * Nasconde l'HBox FXML originale `myVillage` e reindirizza i riferimenti
     * all'HBox interna di VillagePanel.
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
     * Crea e collega il pannello Statistiche flottante alla radice della scena.
     * Nasconde l'HBox FXML originale `myVillage` e reindirizza i riferimenti
     * all'HBox interna di StatsPanel.
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

    private CardGUI createDrawableCard(int cardId) {
        assertFxThread();

        if (cardMap.containsKey(cardId)) return cardMap.get(cardId);

        CardGUI card = new CardGUI(cardId);
        cardMap.put(cardId, card);
        card.setOnMouseClicked(event -> handleDrawClick(card));
        return card;
    }

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
     *
     * @param playerStats
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

    private void updateMyStats(PlayerStatsDTO stats) {
        assertFxThread();

        txtPP.setText(String.valueOf(stats.getPPs()));
        txtFood.setText(String.valueOf(stats.getnFood()));
        txtStars.setText(String.valueOf(stats.getnStars()));
        txtBuildDisc.setText(String.valueOf(stats.getTotBuildDisc()));
        txtFeastDisc.setText(String.valueOf(stats.getFoodDiscount()));
    }

    private void viewVillage(String nickname) {
        assertFxThread();

        VillageOverlay overlay = opponentOverlays.get(nickname);
        if(overlay == null){
            System.err.println("[GameController] Overlay non trovato per " + nickname);
            return;
        }
        showOverlay(overlay.root);
    }

    private void initFlags() {
        for(PlayerStatusDTO status : viewGUI.getModel().getPlayerStatuses()){
            if(status.getNickname().equals(viewGUI.getNickname())){
                putFlags(status);
            }
        }
    }

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

    private void showOverlay(Node content) {
        assertFxThread();
        overlayContent.getChildren().setAll(content);
        globalOverlay.setVisible(true);
        globalOverlay.setManaged(true);
        globalOverlay.setOnMouseClicked(e -> {
            if (e.getTarget().equals(globalOverlay)) hideOverlay();
        });
    }

    private void hideOverlay() {
        hideOverlay(null);
    }

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

    private String buildDeltaLine(PlayerStatsDTO before, PlayerStatsDTO after) {
        List<String> parts = new ArrayList<>();
        addDelta(parts, "cibo",   before.getnFood(),  after.getnFood());
        addDelta(parts, "pp",     before.getPPs(),     after.getPPs());
        addDelta(parts, "stelle", before.getnStars(),  after.getnStars());
        return String.join("   |   ", parts);
    }

    private void addDelta(List<String> parts, String label, int before, int after) {
        int diff = after - before;
        if (diff == 0) return;
        String sign = diff > 0 ? "+" : "";
        parts.add(label + ": " + before + " → " + after + "  (" + sign + diff + ")");
    }

    private Text buildStatsText(PlayerStatsDTO stats) {
        Text t = new Text(formatStats(stats));
        t.setFill(Color.web("#FFFFFF"));
        t.setStyle("-fx-font-size: 20px;");
        return t;
    }

    private static String formatStats(PlayerStatsDTO stats) {
        return "food: " + stats.getnFood()
                + "  pp: " + stats.getPPs()
                + "  stars: " + stats.getnStars()
                + "\nsconto builder: " + stats.getTotBuildDisc()
                + " sconto gatherer: " + stats.getFoodDiscount();
    }

    private static void assertFxThread() {
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException(
                    "Questo metodo deve girare sull'FX Application Thread, "
                            + "ma è stato chiamato da: " + Thread.currentThread().getName()
            );
        }
    }

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

    public boolean isEventBannerShowing(){
        return eventBannerShowing;
    }
}