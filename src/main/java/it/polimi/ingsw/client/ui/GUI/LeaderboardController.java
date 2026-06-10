package it.polimi.ingsw.client.ui.GUI;

import it.polimi.ingsw.network.dto.PlayerStatsDTO;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * JavaFX controller for the end-game leaderboard screen
 * ({@code leaderboardBoard.fxml}).
 *
 * <p>The screen has two views that are swapped at runtime:</p>
 * <ul>
 *   <li><strong>Match ranking</strong> ({@link #rankingTable}) — shown
 *       immediately on load; lists all players sorted by prestige points
 *       (descending), with food as a tiebreaker. The local player's row is
 *       highlighted in gold.</li>
 *   <li><strong>Global leaderboard</strong> ({@link #globalTable}) — revealed
 *       when the player clicks the "Global Leaderboard" button; shows
 *       cumulative points across all recorded matches of the same player
 *       count, fetched from the server's PostgreSQL database. If the database
 *       is unreachable, a fallback message is shown in {@link #txtPlayerRank}
 *       instead.</li>
 * </ul>
 *
 * <p>All methods that touch JavaFX nodes are guarded by
 * {@link #assertFxThread()}. Network requests (quit, ranking fetch) are
 * dispatched on short-lived daemon threads to keep the FX Application Thread
 * free.</p>
 *
 * @see GameController#onGameEnding(List, int, int)
 */
public class LeaderboardController {


    /** Table showing the final match ranking, one row per player. */
    @FXML private TableView<PlayerStatsDTO> rankingTable;

    /** Column displaying each player's finishing position (1-based, computed dynamically). */
    @FXML private TableColumn<PlayerStatsDTO, Integer> colPos;

    /** Column displaying each player's nickname. */
    @FXML private TableColumn<PlayerStatsDTO, String> colNickname;

    /** Column displaying each player's final prestige-point total. */
    @FXML private TableColumn<PlayerStatsDTO, Integer> colPP;

    /** Column displaying each player's remaining food at game end. */
    @FXML private TableColumn<PlayerStatsDTO, Integer> colFood;

    /** The local player's 1-based finishing position in the match ranking. */
    private int rankingPos;

    /** Button that navigates back to the main menu by calling {@link it.polimi.ingsw.client.Client#quit()}. */
    @FXML private Button mainMenuBtn;

    /** Button that fetches and reveals the global leaderboard view. Hidden once the global table is shown. */
    @FXML private Button globalLeaderboardBtn;

    /** Reference to the owning {@link ViewGUI}, used to access the client and the virtual model. */
    private ViewGUI viewGUI;

    /** Table showing the persistent global ranking, one row per registered player. */
    @FXML private TableView<Map.Entry<String, Integer>> globalTable;

    /** Column displaying each entry's rank in the global leaderboard (dense ranking, ties share a position). */
    @FXML private TableColumn<Map.Entry<String, Integer>, Integer> globalColPos;

    /** Column displaying each entry's player nickname in the global leaderboard. */
    @FXML private TableColumn<Map.Entry<String, Integer>, String> globalColNickname;

    /** Column displaying each entry's cumulative point total in the global leaderboard. */
    @FXML private TableColumn<Map.Entry<String, Integer>, Integer> globalColPoints;

    /**
     * Label showing the local player's position in the global leaderboard,
     * or a "database unreachable" notice when the server cannot provide the data.
     */
    @FXML private Label txtPlayerRank;



    /**
     * Called automatically by the {@link javafx.fxml.FXMLLoader} after all
     * {@code @FXML} fields have been injected.
     *
     * <p>Configures both tables to use a constrained column-resize policy and
     * wires their cell-value factories:</p>
     * <ul>
     *   <li>{@link #colPos} — derived from the row's index in the sorted
     *       items list (1-based).</li>
     *   <li>{@link #colNickname}, {@link #colPP}, {@link #colFood} — mapped
     *       directly from the corresponding {@link PlayerStatsDTO} fields.</li>
     *   <li>{@link #globalColPos} — computed with dense ranking: iterates the
     *       global table items and assigns the same rank to entries with equal
     *       point totals.</li>
     *   <li>{@link #globalColNickname}, {@link #globalColPoints} — mapped from
     *       the {@code Map.Entry} key and value respectively.</li>
     * </ul>
     * <p>The global table is initially hidden and unmanaged; it is revealed
     * when the player clicks the global-leaderboard button.</p>
     */
    public void initialize() {
        rankingTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        globalTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        colPos.setCellValueFactory(c ->
                new SimpleIntegerProperty(
                        rankingTable.getItems().indexOf(c.getValue()) + 1).asObject());
        colNickname.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getNickname()));
        colPP.setCellValueFactory(c ->
                new SimpleIntegerProperty(c.getValue().getPPs()).asObject());
        colFood.setCellValueFactory(c ->
                new SimpleIntegerProperty(c.getValue().getnFood()).asObject());

        globalColPos.setCellValueFactory(c -> {
            int cnt = 0, rank = 1, prev = Integer.MIN_VALUE;
            for (Map.Entry<String, Integer> e : globalTable.getItems()) {
                cnt++;
                if (!e.getValue().equals(prev)) {
                    rank = cnt;
                    prev = e.getValue();
                }
                if (e.equals(c.getValue())) break;
            }
            return new SimpleIntegerProperty(rank).asObject();
        });
        globalColNickname.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getKey()));
        globalColPoints.setCellValueFactory(c ->
                new SimpleIntegerProperty(c.getValue().getValue()).asObject());

        globalTable.setVisible(false);
        globalTable.setManaged(false);
    }



    /**
     * Populates the match-ranking table and configures the global-ranking
     * status label.
     *
     * <p>Players are sorted by prestige points descending, with remaining
     * food as a tiebreaker. The table height is capped at five visible rows
     * (45 px each, plus a 30 px header). A custom {@link TableRow} factory
     * highlights the local player's row with a translucent gold background
     * ({@code rgba(255,215,0,0.35)}).</p>
     *
     * <p>Must be called on the FX Application Thread.</p>
     *
     * @param stats            final stats for all players in the match
     * @param myRankingPos     the local player's 1-based finishing position;
     *                         used to identify the row to highlight
     * @param viewGUI          the owning view, stored for later client access
     * @param globalRankingPos the local player's position in the persistent
     *                         global leaderboard for this player count, or
     *                         {@code -1} if the database was unreachable
     */
    public void setData(List<PlayerStatsDTO> stats, int myRankingPos,
                        ViewGUI viewGUI, int globalRankingPos) {
        this.viewGUI = viewGUI;
        assertFxThread();
        this.rankingPos = myRankingPos;

        List<PlayerStatsDTO> sorted = stats.stream()
                .sorted(Comparator.comparing(PlayerStatsDTO::getPPs, Comparator.reverseOrder())
                        .thenComparing(PlayerStatsDTO::getnFood, Comparator.reverseOrder()))
                .toList();

        ObservableList<PlayerStatsDTO> data = FXCollections.observableArrayList(sorted);
        rankingTable.setItems(data);
        rankingTable.setFixedCellSize(45.0);
        int visibleRows = Math.min(sorted.size(), 5);
        rankingTable.setPrefHeight(visibleRows * 45.0 + 30);
        rankingTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(PlayerStatsDTO item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty && item != null && getIndex() + 1 == myRankingPos) {
                    setStyle("-fx-background-color: rgba(255,215,0,0.35);");
                } else {
                    setStyle("");
                }
            }
        });

        String text = globalRankingPos == -1
                ? "Database non raggiungibile."
                : "Sei il numero " + globalRankingPos
                + " nella classifica globale di partite da "
                + viewGUI.getModel().getNumPlayers() + " giocatori";
        txtPlayerRank.setText(text);
        txtPlayerRank.setVisible(true);
        txtPlayerRank.setManaged(true);
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
     * Sets the {@link ViewGUI} reference used to access the client and the
     * virtual model.
     *
     * <p>Prefer passing {@code viewGUI} via {@link #setData} when the full
     * match data is available; this setter exists for cases where the
     * controller must be wired before data arrives.</p>
     *
     * @param viewGUI the owning view; must not be {@code null}
     */
    public void setViewGUI(ViewGUI viewGUI) {
        this.viewGUI = viewGUI;
    }



    /**
     * Handles the "Main Menu" button click: dispatches a
     * {@link it.polimi.ingsw.client.Client#quit()} call on a background
     * thread, which causes the server to clean up the session and the client
     * to navigate back to the main-menu scene.
     */
    public void onMainMenu() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                viewGUI.getClient().quit();
                return null;
            }
        };
        new Thread(task, "quit-action").start();
    }

    /**
     * Handles the "Global Leaderboard" button click: requests the persistent
     * ranking from the server on a background thread.
     *
     * <p>When the server responds, it calls
     * {@link #showGlobalRanking(Map)} on the FX Application Thread to
     * populate and reveal {@link #globalTable}.</p>
     */
    @FXML
    public void onGlobalLeaderboard() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                viewGUI.getClient().requestRanking();
                return null;
            }
        };
        new Thread(task, "ranking-leaderboard").start();
    }



    /**
     * Populates and reveals the global-leaderboard table with the ranking
     * data received from the server.
     *
     * <p>Entries are sorted by cumulative points descending, with the
     * nickname as an alphabetical tiebreaker. The table height is capped at
     * eight visible rows. A custom {@link TableRow} factory applies the CSS
     * class {@code my-row} to the entry that matches the local player's
     * nickname.</p>
     *
     * <p>Once the global table is shown, {@link #rankingTable} and
     * {@link #globalLeaderboardBtn} are hidden and unmanaged to keep the
     * layout clean.</p>
     *
     * <p>Must be called on the FX Application Thread.</p>
     *
     * @param ranks map from player nickname to their cumulative point total
     *              in the global leaderboard for this match's player count
     */
    public void showGlobalRanking(Map<String, Integer> ranks) {
        assertFxThread();
        List<Map.Entry<String, Integer>> sorted = ranks.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .toList();

        ObservableList<Map.Entry<String, Integer>> data =
                FXCollections.observableArrayList(sorted);
        globalTable.setItems(data);
        globalTable.setFixedCellSize(45.0);
        int visibleRows = Math.min(sorted.size(), 8);
        globalTable.setPrefHeight(visibleRows * 45.0 + 30);
        globalTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Map.Entry<String, Integer> item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty && item != null
                        && item.getKey().equals(viewGUI.getNickname())) {
                    getStyleClass().add("my-row");
                } else {
                    getStyleClass().remove("my-row");
                }
            }
        });

        rankingTable.setVisible(false);
        rankingTable.setManaged(false);
        globalTable.setVisible(true);
        globalTable.setManaged(true);
        globalLeaderboardBtn.setVisible(false);
        globalLeaderboardBtn.setManaged(false);
    }
}