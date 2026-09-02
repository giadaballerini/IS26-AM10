package it.polimi.ingsw.client.ui.gui;

import it.polimi.ingsw.client.Client;
import it.polimi.ingsw.client.VirtualModel;
import it.polimi.ingsw.client.rmi.ClientRmi;
import it.polimi.ingsw.client.socket.ClientSocket;
import it.polimi.ingsw.client.ui.UserInterface;
import it.polimi.ingsw.enumerations.NetworkType;
import it.polimi.ingsw.network.dto.LobbyDTO;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the main menu scene in the GUI.
 * Manages the sequential flow of pre-game screens: network configuration,
 * login, lobby creation, and lobby selection. Each pane is shown or hidden
 * depending on the current step, and the controller communicates with the
 * {@link Client} to perform network operations on background threads.
 *
 * <p>This controller requires {@link ViewGUI} and {@link SceneManager} to be
 * injected before the scene becomes active; failing to do so will cause a
 * {@link NullPointerException} at runtime.</p>
 */
public class MainMenuController {
    /** Pane displayed during the login step, where the player enters their username. */
    @FXML private VBox loginPane;

    /** Pane displayed during the network configuration step, where the player sets the server IP and connection type. */
    @FXML private VBox networkPane;

    /** Pane displayed after login, allowing the player to choose between creating a new lobby or joining an existing one. */
    @FXML private HBox choicePane;

    /** Pane displayed during lobby creation, where the player selects the number of players for the new game. */
    @FXML private VBox creationPane;

    /** Text field where the player types their username during the login step. */
    @FXML private TextField usernameField;

    /** Text field where the player types the server IP address during the network configuration step. */
    @FXML private TextField ipField;

    /** Label inside the error banner displaying the current error message text. */
    @FXML private Label errorLabel;

    /** Inner container of the error banner, holding the icon and the {@link #errorLabel}. */
    @FXML private HBox errorBanner;

    /** Outer wrapper of the error banner; animated in and out of view when an error must be shown or dismissed. */
    @FXML private HBox errorBannerWrapper;

    /** Toggle group for the player-count radio buttons shown during lobby creation (e.g. 2, 3, 4 players). */
    @FXML private ToggleGroup numPlayers;

    /** Toggle group for the connection-type radio buttons shown during network configuration (RMI or Socket). */
    @FXML private ToggleGroup connection;

    /** Button that confirms the server IP and connection type and attempts to establish the network connection. */
    @FXML private Button connectButton;

    /** Button that confirms the entered username and proceeds to the lobby selection or creation screen. */
    @FXML private Button loginButton;

    /** Image-based back button that navigates the player to the previous step in the menu flow. */
    @FXML private ImageView backButton;

    /** Button that closes the application from the main menu. */
    @FXML Button quitBtn;

    /** Stack pane that hosts and sizes the {@link #lobbyPane} table within the scene layout. */
    @FXML private javafx.scene.layout.StackPane lobbyPaneWrapper;

    /** Table displaying the list of available lobbies the player can join. */
    @FXML private TableView<LobbyDTO> lobbyPane;

    /** Table column showing the current and maximum player count for each lobby (e.g. {@code "2/4"}). */
    @FXML private TableColumn<LobbyDTO, String> playersColumn;

    /** Table column showing the username of the player who created each lobby. */
    @FXML private TableColumn<LobbyDTO, String> ownerColumn;

    /** Table column showing the numeric identifier of each lobby. */
    @FXML private TableColumn<LobbyDTO, Integer> colId;

    /** Table column containing the join button rendered inline for each lobby row. */
    @FXML private TableColumn<LobbyDTO, Void> colJoin;

    /** Observable list backing the lobby table. */
    private final ObservableList<LobbyDTO> lobbyData = FXCollections.observableArrayList();

    /** The nickname entered by the player at login. */
    private String username;

    /** The client instance created after a successful connection. */
    private Client client;

    /** The server IP address entered by the player. */
    private String ip;

    /**
     * The GUI-side implementation of {@link UserInterface} that receives server
     * notifications and dispatches them to the active JavaFX scene controllers.
     * Must be injected before the scene becomes active so that server-pushed
     * updates can be forwarded
     * to this controller.
     */
    private ViewGUI viewGUI;

    /**
     * Central registry for the application's JavaFX scenes, bound to the
     * primary {@link javafx.stage.Stage}. Used by this controller to trigger
     * scene transition without holding a direct reference to the stage.
     */
    private SceneManager sceneManager;

    /** Timer used to auto-dismiss the error banner after 8 seconds. */
    private Timeline hideTimer;

    /**
     * Whether the controller is expecting a reconnection attempt from the server.
     * Set to {@code true} when the player is sent back to the menu after a disconnection.
     */
    private boolean reconnectExpected = false;

    /** Timer that waits for a reconnection confirmation before proceeding to the lobby screen. */
    private PauseTransition reconnectTimer;

    /** Whether a reconnection has already been handled in this session, to avoid duplicate transitions. */
    private boolean reconnectHandled = false;

    /**
     * Logger used for logging messages and errors.
     */
    private static final Logger LOG = Logger.getLogger(MainMenuController.class.getName());

    /**
     * Initializes the controller after the FXML is loaded.
     * Sets up the lobby table columns, the join button cell factory,
     * and the initial visibility state of all panes.
     */
    @FXML
    public void initialize() {
        lobbyPane.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        colId.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getId()));
        playersColumn.setCellValueFactory(data -> {
            LobbyDTO l = data.getValue();
            return new ReadOnlyObjectWrapper<>(l.getCurrPlayers() + "/" + l.getCapacity());
        });
        ownerColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getNicknames().getFirst()));
        CursorManager.makeNodesHoverable(backButton, quitBtn);
        lobbyPane.prefWidthProperty().bind(lobbyPaneWrapper.widthProperty().multiply(0.3));
        lobbyPane.prefHeightProperty().bind(lobbyPaneWrapper.heightProperty().multiply(0.35));
        lobbyPane.maxWidthProperty().bind(lobbyPaneWrapper.widthProperty().multiply(0.3));
        lobbyPane.maxHeightProperty().bind(lobbyPaneWrapper.heightProperty().multiply(0.35));
        lobbyPane.translateYProperty().bind(lobbyPaneWrapper.heightProperty().multiply(-0.05));


        try {
            java.net.URL cssUrl = getClass().getResource("/css/tableStyle.css");
            if (cssUrl != null) {
                String cssPath = cssUrl.toExternalForm();
                lobbyPane.getStylesheets().clear();
                lobbyPane.getStylesheets().add(cssPath);

            } else {
                LOG.warning("File tableStyle.css non trovato.");
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Errore caricamento CSS", e);
        }

        colJoin.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Unisciti");
            {
                btn.getStyleClass().add("join-button");
            }

            {
                CursorManager.makeNodesHoverable(btn);
                btn.setOnAction(e -> {
                    LobbyDTO row = getTableView().getItems().get(getIndex());
                    client.joinGame(username, row.getId());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        lobbyPane.setItems(lobbyData);
        hideAll();
        networkPane.setVisible(true);
        networkPane.setManaged(true);
        quitBtn.setVisible(true);
        quitBtn.setManaged(true);
        connectButton.setDisable(true);
        connection.selectedToggleProperty().addListener((obs, oldVal, newVal) -> connectButton.setDisable(newVal == null));
    }

    /**
     * Called when a reconnection attempt succeeds. Stops the reconnection timer,
     * hides all panes, and transitions directly to the game board scene.
     */
    public void onReconnectSuccess() {
        reconnectExpected = false;
        reconnectHandled = true;
        if (reconnectTimer != null) { reconnectTimer.stop(); reconnectTimer = null; }
        hideAll();
        loadScene();
        viewGUI.reconnectWaiting();
    }

    /**
     * Populates the lobby table with the available lobbies and switches to the lobby list pane.
     * Must be called on the JavaFX application thread.
     *
     * @param lobbies a map from maximum player count to the list of lobbies with that capacity
     */
    public void updateLobbies(Map<Integer, List<LobbyDTO>> lobbies) {
        Platform.runLater(() -> {
            lobbyData.clear();
            for (List<LobbyDTO> sameCapacityLobbies : lobbies.values()) {
                lobbyData.addAll(sameCapacityLobbies);
            }
            hideAll();
            lobbyPaneWrapper.setVisible(true);
            lobbyPaneWrapper.setManaged(true);
            backButton.setVisible(true);
            backButton.setManaged(true);
            quitBtn.setVisible(true);
            quitBtn.setManaged(true);
        });
    }

    /**
     * Handles the connection button click. Reads the IP address and selected network type,
     * then attempts to create the client on a background thread to avoid blocking the UI.
     * On success, transitions to the login pane; on failure, shows an error banner.
     */
    @FXML
    public void onConnectClick() {
        if (connection.getSelectedToggle() == null) {
            showError("Selezionare un tipo di connessione");
            return;
        }
        ip = ipField.getText();
        if (ip.isEmpty()) ip = "127.0.0.1";

        connectButton.setDisable(true);
        Task<Void> connectTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                createClient();
                return null;
            }
        };

        connectTask.setOnSucceeded(e -> {
            connectButton.setDisable(false);
            if (client != null) {
                client.start();
                hideAll();
                loginPane.setVisible(true);
                loginPane.setManaged(true);
                quitBtn.setVisible(true);
                quitBtn.setManaged(true);
            }
        });

        connectTask.setOnFailed(e -> {
            connectButton.setDisable(false);
            Throwable ex = connectTask.getException();
            System.err.println("FAILED: " + ex);
            showError("Server non raggiungibile. Controllare l'indirizzo IP e riprovare.");
        });

        Thread t = new Thread(connectTask, "connect-task");
        t.setDaemon(true);
        t.start();
    }

    /**
     * Creates the appropriate {@link Client} implementation based on the selected network type,
     * and wires it to the {@link ViewGUI} and {@link VirtualModel}.
     * Called on a background thread by {@link #onConnectClick()}.
     *
     * @throws NotBoundException if the RMI registry cannot be reached
     * @throws IOException       if the socket connection cannot be established
     */
    private void createClient() throws NotBoundException, IOException {
        Toggle selected = connection.getSelectedToggle();
        if (selected == null) {
            Platform.runLater(() -> showError("Seleziona il tipo di rete"));
            return;
        }
        NetworkType chosenNetwork = NetworkType.valueOf((String) selected.getUserData());

        VirtualModel model = new VirtualModel();
        client = chosenNetwork == NetworkType.SOCKET
                ? new ClientSocket(ip, 1234, model)
                : new ClientRmi(ip, model);

        viewGUI.setModel(model);
        client.setUi(viewGUI);
        viewGUI.setClient(client);
    }

    /**
     * Handles the login button click. Reads the username from the text field
     * and attempts to log in on a background thread.
     * On failure, shows an appropriate error banner.
     */
    @FXML
    public void onLoginClick() {
        username = usernameField.getText();
        if (username.isEmpty()) {
            showError("Il nickname non può essere vuoto");
            return;
        }

        loginButton.setDisable(true);

        Task<Boolean> loginTask = new Task<>() {
            @Override
            protected Boolean call() {
                return client.login(username);
            }
        };

        loginTask.setOnSucceeded(e -> {
            loginButton.setDisable(false);
            boolean success = loginTask.getValue();
            if (!success) {
                showError("Il nickname è già in uso");
            }
        });

        loginTask.setOnFailed(e -> {
            loginButton.setDisable(false);
            showError("Errore di connessione durante il login");
        });

        Thread t = new Thread(loginTask, "login-task");
        t.setDaemon(true);
        t.start();
    }

    /**
     * Called by {@link ViewGUI} when the server confirms a successful login.
     * Transitions to the lobby choice pane, unless a reconnection flow is in progress,
     * in which case it waits briefly for a reconnection confirmation before proceeding.
     *
     * @param nickname the nickname that was accepted by the server
     */
    public void onLoginSuccess(String nickname) {
        hideAll();
        if (reconnectHandled) return;
        if (!reconnectExpected) {
            choicePane.setVisible(true);
            choicePane.setManaged(true);
            quitBtn.setVisible(true);
            quitBtn.setManaged(true);
            return;
        }
        if (reconnectTimer != null) reconnectTimer.stop();
        reconnectTimer = new PauseTransition(Duration.millis(2000));
        reconnectTimer.setOnFinished(e -> {
            if (reconnectExpected) {
                reconnectExpected = false;
                hideAll();
                choicePane.setVisible(true);
                choicePane.setManaged(true);
                quitBtn.setVisible(true);
                quitBtn.setManaged(true);
            }
        });
        reconnectTimer.play();
    }

    /**
     * Handles the "Create" button click. Transitions to the lobby creation pane.
     */
    @FXML
    public void onCreateClick() {
        hideAll();
        creationPane.setVisible(true);
        creationPane.setManaged(true);
        backButton.setVisible(true);
        backButton.setManaged(true);
        quitBtn.setVisible(true);
        quitBtn.setManaged(true);
    }

    /**
     * Handles the confirm button click in the lobby creation pane.
     * Reads the selected player count, requests game creation, and transitions
     * to the game board scene.
     */
    @FXML
    public void onConfirmClick() {
        Toggle selected = numPlayers.getSelectedToggle();
        if (selected == null) {
            showError("Selezionare quantità giocatori");
            return;
        }
        int numPlayers = Integer.parseInt((String) selected.getUserData());
        client.createGame(username, numPlayers);
        hideAll();
        loadScene();
    }

    /**
     * Handles the "Join" button click. Requests the list of available lobbies from the server.
     */
    @FXML
    public void onJoinClick() {
        client.requestJoin();
    }

    /**
     * Handles the back button click. Returns to the lobby choice pane.
     */
    @FXML
    public void onBackClick() {
        hideAll();
        choicePane.setVisible(true);
        choicePane.setManaged(true);
        quitBtn.setVisible(true);
        quitBtn.setManaged(true);
    }

    /**
     * Called by {@link ViewGUI} when the server confirms the player has joined a lobby.
     * Transitions to the game board scene.
     *
     * @param id the unique identifier of the lobby that was joined
     */
    public void onJoinSuccess(int id) {
        hideAll();
        loadScene();
    }

    /**
     * Called by {@link ViewGUI} when the server confirms the game was successfully created.
     * Hides all panes while waiting for the game board scene to be loaded.
     *
     * @param id the unique identifier of the created game
     */
    public void onCreateSuccess(int id) {
        hideAll();
    }

    /**
     * Hides all panes and clears the error banner.
     */
    private void hideAll() {
        loginPane.setVisible(false);
        loginPane.setManaged(false);
        networkPane.setVisible(false);
        networkPane.setManaged(false);
        choicePane.setVisible(false);
        choicePane.setManaged(false);
        creationPane.setVisible(false);
        creationPane.setManaged(false);
        lobbyPaneWrapper.setVisible(false);
        lobbyPaneWrapper.setManaged(false);
        backButton.setVisible(false);
        backButton.setManaged(false);
        quitBtn.setVisible(false);
        quitBtn.setManaged(false);

        if (hideTimer != null) hideTimer.stop();
        errorBanner.setVisible(false);
        errorBanner.setManaged(false);
        errorLabel.setText("");
    }

    /**
     * Injects the client instance into this controller.
     *
     * @param client the client to use for network operations
     */
    public void setClient(Client client) {
        this.client = client;
    }

    /**
     * Loads the game board FXML, wires a new {@link GameController} to it,
     * registers the resulting root under the {@code "game"} name in the
     * {@link SceneManager}, and switches the primary stage's existing
     * {@link javafx.scene.Scene} to that root.
     *
     * <p>The root is (re-)registered on every call, since a new
     * {@link GameController} is created each time a game starts. Swapping
     * only the scene root via {@link SceneManager#switchTo(String)}, instead
     * of building a brand-new {@code Scene} and assigning it with
     * {@code stage.setScene(...)}, keeps the stage's existing fullscreen
     * state intact and avoids the resize/fullscreen flicker that previously
     * occurred on this transition.</p>
     */
    private void loadScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/gameBoard.fxml"));
            GameController gameController = new GameController(viewGUI);
            loader.setController(gameController);
            Parent root = loader.load();
            viewGUI.setGameController(gameController);

            sceneManager.register("game", root);
            sceneManager.switchTo("game");
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Errore caricamento scena ", e);
        }
    }

    /**
     * Injects the {@link ViewGUI} instance into this controller.
     *
     * @param viewGUI the GUI view to associate with this controller
     */
    public void setViewGUI(ViewGUI viewGUI) {
        this.viewGUI = viewGUI;
    }

    /**
     * Injects the {@link SceneManager} instance into this controller.
     *
     * @param sceneManager the scene manager used to switch between application scenes
     */
    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    /**
     * Sends the player back to the network configuration pane, typically after
     * a disconnection from the server. Sets the reconnect flag so that the next
     * successful login triggers a reconnection attempt rather than a fresh lobby flow.
     * Shows the given reason in the error banner if non-empty.
     *
     * @param reason the reason for returning to the menu, displayed in the error banner;
     *               may be {@code null} or empty if no message should be shown
     */
    public void returnToMenu(String reason) {
        Platform.runLater(() -> {
            reconnectExpected = true;
            reconnectHandled = false;
            if (reconnectTimer != null) { reconnectTimer.stop(); reconnectTimer = null; }
            sceneManager.switchTo("menu");
            hideAll();
            networkPane.setVisible(true);
            networkPane.setManaged(true);
            quitBtn.setVisible(true);
            quitBtn.setManaged(true);
            if (reason != null && !reason.isEmpty()) {
                showError(reason);
            }
        });
    }

    /**
     * Sends the player back to the lobby choice pane, typically after a match ends normally.
     * Shows the given reason in the error banner if non-empty.
     *
     * @param reason the reason for returning to the lobby, displayed in the error banner;
     *               may be {@code null} or empty if no message should be shown
     */
    public void returnToLobby(String reason) {
        Platform.runLater(() -> {
            reconnectExpected = false;
            reconnectHandled = false;
            if (reconnectTimer != null) { reconnectTimer.stop(); reconnectTimer = null; }
            sceneManager.switchTo("menu");
            hideAll();
            choicePane.setVisible(true);
            choicePane.setManaged(true);
            quitBtn.setVisible(true);
            quitBtn.setManaged(true);
            if (reason != null && !reason.isEmpty())
                showError(reason);
        });
    }

    /**
     * Displays an animated error banner with the given message.
     * The banner slides in from the top and fades out automatically after 4 seconds.
     *
     * @param message the error message to display; should be non-null and non-empty
     */
    public void showError(String message) {
        errorLabel.setText(message);
        errorBanner.setVisible(true);
        errorBanner.setManaged(true);
        errorBannerWrapper.setVisible(true);
        errorBannerWrapper.setManaged(true);
        errorBanner.setOpacity(0);
        errorBanner.setTranslateY(-20);

        Timeline anim = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(errorBanner.opacityProperty(), 0),
                        new KeyValue(errorBanner.translateYProperty(), -20)
                ),
                new KeyFrame(Duration.millis(300),
                        new KeyValue(errorBanner.opacityProperty(), 1, Interpolator.EASE_OUT),
                        new KeyValue(errorBanner.translateYProperty(), 0, Interpolator.EASE_OUT)
                )
        );
        anim.play();

        Timeline dismiss = new Timeline(
                new KeyFrame(Duration.millis(4000), e -> {
                    Timeline fadeOut = new Timeline(
                            new KeyFrame(Duration.ZERO,
                                    new KeyValue(errorBanner.opacityProperty(), 1)
                            ),
                            new KeyFrame(Duration.millis(250),
                                    new KeyValue(errorBanner.opacityProperty(), 0, Interpolator.EASE_IN)
                            )
                    );
                    fadeOut.setOnFinished(ev -> {
                        errorBanner.setVisible(false);
                        errorBanner.setManaged(false);
                        errorBannerWrapper.setVisible(false);
                        errorBannerWrapper.setManaged(false);
                    });
                    fadeOut.play();
                })
        );
        dismiss.play();
    }

    /**
     * Handles the quit button click. Terminates the JavaFX application.
     */
    @FXML
    public void onExit() {
        Platform.exit();
    }
    /**
     * Creates a new {@code MainMenuController} instance.
     * Called by the JavaFX {@code FXMLLoader} via reflection.
     */
    public MainMenuController() {
    }
}