package it.polimi.ingsw.client.ui.GUI;

import it.polimi.ingsw.client.Client;
import it.polimi.ingsw.client.VirtualModel;
import it.polimi.ingsw.enumerations.NetworkType;
import it.polimi.ingsw.network.client.rmi.ClientRmi;
import it.polimi.ingsw.network.client.socket.ClientSocket;
import it.polimi.ingsw.network.dto.LobbyDTO;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.util.List;
import java.util.Map;

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

    @FXML private VBox loginPane;
    @FXML private VBox retePane;
    @FXML private HBox sceltaPane;
    @FXML private VBox creaPane;
    @FXML private TextField usernameField;
    @FXML private TextField ipField;
    @FXML private Label errorLabel;
    @FXML private HBox errorBanner;
    @FXML private HBox errorBannerWrapper;
    @FXML private ToggleGroup numPlayers;
    @FXML private ToggleGroup connection;
    @FXML private Button connectButton;
    @FXML private Button loginButton;
    @FXML private ImageView backButton;
    @FXML Button quitBtn;
    @FXML private TableView<LobbyRow> lobbyPane;
    @FXML private TableColumn<LobbyRow, String> colGiocatori;
    @FXML private TableColumn<LobbyRow, String> colProprietario;
    @FXML private TableColumn<LobbyRow, Integer> colId;
    @FXML private TableColumn<LobbyRow, Void> colJoin;

    /** Observable list backing the lobby table. */
    private final ObservableList<LobbyRow> lobbyData = FXCollections.observableArrayList();

    /** The nickname entered by the player at login. */
    private String username;

    /** The network protocol chosen by the player (RMI or Socket). */
    private NetworkType chosenNetwork;

    /** The client instance created after a successful connection. */
    private Client client;

    /** The server IP address entered by the player. */
    private String ip;

    private ViewGUI viewGUI;
    private SceneManager sceneManager;

    /** Timer used to auto-dismiss the error banner after 4 seconds. */
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
     * Initializes the controller after the FXML is loaded.
     * Sets up the lobby table columns, the join button cell factory,
     * and the initial visibility state of all panes.
     */
    @FXML
    public void initialize() {
        lobbyPane.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colGiocatori.setCellValueFactory(new PropertyValueFactory<>("giocatori"));
        colProprietario.setCellValueFactory(new PropertyValueFactory<>("proprietario"));
        CursorManager.makeNodesHoverable(backButton, quitBtn);

        System.out.println(getClass().getResource("/images/back_button.png"));
        try {
            java.net.URL cssUrl = getClass().getResource("/css/tableStyle.css");
            if (cssUrl != null) {
                String cssPath = cssUrl.toExternalForm();
                lobbyPane.getStylesheets().clear();
                lobbyPane.getStylesheets().add(cssPath);
                System.out.println("STILE CARICATO CON SUCCESSO DA: " + cssPath);
            } else {
                System.out.println("ERRORE: File tableStyle.css non trovato.");
            }
        } catch (Exception e) {
            System.out.println("ECCEZIONE CSS: " + e.getMessage());
        }

        colJoin.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Unisciti");

            {
                btn.getStyleClass().add("bottone-join");
            }

            {
                CursorManager.makeNodesHoverable(btn);
                btn.setOnAction(e -> {
                    LobbyRow row = getTableView().getItems().get(getIndex());
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
        retePane.setVisible(true);
        retePane.setManaged(true);
        quitBtn.setVisible(true);
        quitBtn.setManaged(true);
        connectButton.setDisable(true);
        connection.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            connectButton.setDisable(newVal == null);
        });
    }

    /**
     * Called when a reconnection attempt succeeds. Stops the reconnect timer,
     * hides all panes, and transitions directly to the game board scene.
     */
    public void onReconnectSuccess() {
        reconnectExpected = false;
        reconnectHandled = true;
        if (reconnectTimer != null) { reconnectTimer.stop(); reconnectTimer = null; }
        hideAll();
        Stage stage = (Stage) loginPane.getScene().getWindow();
        loadScene("/fxml/gameBoard.fxml", stage);
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
            for (Map.Entry<Integer, List<LobbyDTO>> entry : lobbies.entrySet()) {
                int max = entry.getKey();
                for (LobbyDTO l : entry.getValue()) {
                    lobbyData.add(new LobbyRow(
                            l.getId(),
                            l.getNicknames().size(),
                            max,
                            l.getNicknames().isEmpty() ? "-" : l.getNicknames().get(0)
                    ));
                }
            }
            hideAll();
            lobbyPane.setVisible(true);
            lobbyPane.setManaged(true);
            backButton.setVisible(true);
            backButton.setManaged(true);
        });
    }

    /**
     * Handles the connect button click. Reads the IP address and selected network type,
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
        this.chosenNetwork = NetworkType.valueOf((String) selected.getUserData());

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
            protected Boolean call() throws Exception {
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
            sceltaPane.setVisible(true);
            sceltaPane.setManaged(true);
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
                sceltaPane.setVisible(true);
                sceltaPane.setManaged(true);
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
    public void onCreaClick() {
        hideAll();
        creaPane.setVisible(true);
        creaPane.setManaged(true);
        backButton.setVisible(true);
        backButton.setManaged(true);
    }

    /**
     * Handles the confirm button click in the lobby creation pane.
     * Reads the selected player count, requests game creation, and transitions
     * to the game board scene.
     */
    @FXML
    public void onConfermaClick() {
        Toggle selected = numPlayers.getSelectedToggle();
        if (selected == null) {
            showError("Selezionare quantità giocatori");
            return;
        }
        int numPlayers = Integer.parseInt((String) selected.getUserData());
        client.createGame(username, numPlayers);
        hideAll();
        Stage stage = (Stage) loginPane.getScene().getWindow();
        loadScene("/fxml/gameBoard.fxml", stage);
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
        sceltaPane.setVisible(true);
        sceltaPane.setManaged(true);
    }

    /**
     * Called by {@link ViewGUI} when the server confirms the player has joined a lobby.
     * Transitions to the game board scene.
     *
     * @param id the unique identifier of the lobby that was joined
     */
    public void onJoinSuccess(int id) {
        hideAll();
        Stage stage = (Stage) loginPane.getScene().getWindow();
        loadScene("/fxml/gameBoard.fxml", stage);
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
        retePane.setVisible(false);
        retePane.setManaged(false);
        sceltaPane.setVisible(false);
        sceltaPane.setManaged(false);
        creaPane.setVisible(false);
        creaPane.setManaged(false);
        lobbyPane.setVisible(false);
        lobbyPane.setManaged(false);
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
     * Loads the given FXML scene, wires a new {@link GameController} to it,
     * applies the application stylesheet and custom cursors, and sets it as
     * the current scene on the given stage in fullscreen mode.
     *
     * @param fxmlPath the classpath path of the FXML file to load
     * @param stage    the stage on which to set the new scene
     */
    private void loadScene(String fxmlPath, Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            GameController gameController = new GameController(viewGUI);
            loader.setController(gameController);
            Parent root = loader.load();
            viewGUI.setGameController(gameController);
            Scene newScene = new Scene(root);

            java.net.URL cssURL = getClass().getResource("/css/style.css");
            if (cssURL != null) {
                newScene.getStylesheets().add(cssURL.toExternalForm());
            } else {
                System.err.println("[WARNING] Impossibile caricare il CSS: file non trovato nel percorso specificato.");
            }

            CursorManager.applyBaseCursor(newScene);
            CursorManager.applyHoverToScene(newScene);
            stage.setScene(newScene);
            stage.setFullScreenExitHint("");
            stage.setFullScreen(true);
        } catch (IOException e) {
            System.err.println("Errore caricamento scena: " + e.getMessage());
            e.printStackTrace();
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
            retePane.setVisible(true);
            retePane.setManaged(true);
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
            sceltaPane.setVisible(true);
            sceltaPane.setManaged(true);
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
}