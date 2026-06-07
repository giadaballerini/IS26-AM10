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
 * Represents the controller that interacts with the main menu scene in the GUI.
 *
 * <p> This controller expects {@link ViewGUI} and {@link SceneManager} to be injected before making it active, otherwise it will throw a {@link NullPointerException} at runtime </p>
 */
public class MainMenuController {
    @FXML
    private VBox loginPane;
    @FXML
    private VBox retePane;
    @FXML
    private HBox sceltaPane;
    @FXML
    private VBox creaPane;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField ipField;

    @FXML
    private Label errorLabel;
    @FXML
    private HBox errorBanner;
    @FXML
    private HBox errorBannerWrapper;
    @FXML
    private ToggleGroup numPlayers;
    @FXML
    private ToggleGroup connection;

    @FXML
    private Button connectButton;

    @FXML
    private Button loginButton;

    @FXML
    private ImageView backButton;

    @FXML Button quitBtn;

    @FXML
    private TableView<LobbyRow> lobbyPane;
    @FXML
    private TableColumn<LobbyRow, String> colGiocatori;
    @FXML
    private TableColumn<LobbyRow, String> colProprietario;
    @FXML
    private TableColumn<LobbyRow, Integer> colId;
    @FXML
    private TableColumn<LobbyRow, Void> colJoin;
    private final ObservableList<LobbyRow> lobbyData = FXCollections.observableArrayList();
    private String username;
    private NetworkType chosenNetwork;
    private Client client;
    private String ip;
    private ViewGUI viewGUI;
    private SceneManager sceneManager;
    private Timeline hideTimer;
    private boolean reconnectExpected = false;
    private PauseTransition reconnectTimer;
    private boolean reconnectHandled = false;

    @FXML
    public void initialize() {
        lobbyPane.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colGiocatori.setCellValueFactory(new PropertyValueFactory<>("giocatori")); // chiama getGiocatori()
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
     * Fills the table with theavailable lobbies.
     * @param lobbies a map from max players to the list of available lobbies
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
     * This method is called after the connection choice is made to create the client
     * @throws NotBoundException if the RMI registry cannot be reached
     * @throws IOException if the socket connection cannot be established.
     */
    private void createClient() throws NotBoundException, IOException {
        Toggle selected = connection.getSelectedToggle();
        if (selected == null) {
            Platform.runLater(() ->showError("Seleziona il tipo di rete"));
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

    @FXML
    public void onCreaClick() {
        hideAll();
        creaPane.setVisible(true);
        creaPane.setManaged(true);
        backButton.setVisible(true);
        backButton.setManaged(true);
    }

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

    @FXML
    public void onJoinClick() {
        client.requestJoin();
    }

    @FXML
    public void onBackClick() {
        hideAll();
        sceltaPane.setVisible(true);
        sceltaPane.setManaged(true);
    }

    public void onJoinSuccess(int id) {
        hideAll();
        Stage stage = (Stage) loginPane.getScene().getWindow();
        loadScene("/fxml/gameBoard.fxml", stage);
    }

    public void onCreateSuccess(int id) {
        hideAll();
    }



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

    public void setClient(Client client) {
        this.client = client;
    }

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

    public void setViewGUI(ViewGUI viewGUI){
        this.viewGUI = viewGUI;
    }
    public void setSceneManager(SceneManager sceneManager){
        this.sceneManager = sceneManager;
    }

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
            if(reason != null && !reason.isEmpty())
                showError(reason);
        });
    }

    /**
     * Shows the banner with the error, it disappears after 4 seconds
     * @param message is the error, it should be not empty and not null
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

    @FXML
    public void onExit(){
        Platform.exit();
    }

}
