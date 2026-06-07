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

public class LeaderboardController {
    @FXML
    private TableView<PlayerStatsDTO> rankingTable;
    @FXML
    private TableColumn<PlayerStatsDTO, Integer> colPos;
    @FXML
    private TableColumn<PlayerStatsDTO, String> colNickname;
    @FXML
    private TableColumn<PlayerStatsDTO, Integer> colPP;
    @FXML
    private TableColumn<PlayerStatsDTO, Integer> colFood;

    private int rankingPos;
    @FXML
    private Button mainMenuBtn;
    @FXML
    private Button globalLeaderboardBtn;
    private ViewGUI viewGUI;
    @FXML private TableView<Map.Entry<String, Integer>>         globalTable;
    @FXML private TableColumn<Map.Entry<String, Integer>, Integer> globalColPos;
    @FXML private TableColumn<Map.Entry<String, Integer>, String>  globalColNickname;
    @FXML private TableColumn<Map.Entry<String, Integer>, Integer> globalColPoints;
    @FXML private Label txtPlayerRank;


    public void initialize() {
        rankingTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        globalTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        colPos.setCellValueFactory(c -> new SimpleIntegerProperty(rankingTable.getItems().indexOf(c.getValue()) + 1).asObject());
        colNickname.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNickname()));
        colPP.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getPPs()).asObject());
        colFood.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getnFood()).asObject());
        globalColPos.setCellValueFactory(c ->{
            int cnt = 0, rank = 1, prev = Integer.MIN_VALUE;
            for(Map.Entry<String, Integer> e : globalTable.getItems()){
                cnt ++;
                if(!e.getValue().equals(prev)){
                    rank = cnt;
                    prev = e.getValue();
                }
                if(e.equals(c.getValue())) break;
            }
            return new SimpleIntegerProperty(rank).asObject();
        });
        globalColNickname.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getKey()));
        globalColPoints.setCellValueFactory(c-> new SimpleIntegerProperty(c.getValue().getValue()).asObject());
        globalTable.setVisible(false);
        globalTable.setManaged(false);
    }

    public void setData(List<PlayerStatsDTO> stats, int myRankingPos, ViewGUI viewGUI, int globalRankingPos) {
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
        String text = globalRankingPos == -1  ? "Database non raggiungibile." : "Sei il numero " + globalRankingPos + " nella classifica globale di partite da " + viewGUI.getModel().getNumPlayers() + " giocatori";
        txtPlayerRank.setText(text);
        txtPlayerRank.setVisible(true);
        txtPlayerRank.setManaged(true);
    }


    private static void assertFxThread() {
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException(
                    "Questo metodo deve girare sull'FX Application Thread, "
                            + "ma è stato chiamato da: " + Thread.currentThread().getName()
            );
        }
    }

    public void setViewGUI(ViewGUI viewGUI) {
        this.viewGUI = viewGUI;
    }

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

    public void showGlobalRanking(Map<String, Integer> ranks) {
        assertFxThread();
        List<Map.Entry<String, Integer>> sorted = ranks.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .toList();
        ObservableList<Map.Entry<String, Integer>> data = FXCollections.observableArrayList(sorted);
        globalTable.setItems(data);
        globalTable.setFixedCellSize(45.0);
        int visibleRows = Math.min(sorted.size(), 8);
        globalTable.setPrefHeight(visibleRows * 45.0 + 30);
        globalTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Map.Entry<String, Integer> item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty && item != null && item.getKey().equals(viewGUI.getNickname())) {
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
