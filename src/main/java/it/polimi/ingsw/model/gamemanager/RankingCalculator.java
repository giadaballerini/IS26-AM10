package it.polimi.ingsw.model.gamemanager;

import it.polimi.ingsw.model.player.Player;

import java.util.*;

public class RankingCalculator {

    public Map<Integer, List<Player>> calculateFinalRanking(List<Player> players) {
        if (players == null || players.isEmpty()) return new TreeMap<>();

        List<Player> sorted = players.stream()
                .sorted(Comparator.comparing(Player::getPP, Comparator.reverseOrder())
                        .thenComparing(Player::getNFood, Comparator.reverseOrder()))
                .toList();

        Map<Integer, List<Player>> ranking = new TreeMap<>();
        int currentRank = 1;
        for (int i = 0; i < sorted.size(); i++) {
            Player curr = sorted.get(i);
            if (i > 0) {
                Player prev = sorted.get(i - 1);
                if (curr.getPP() != prev.getPP() || curr.getNFood() != prev.getNFood())
                    currentRank = i + 1;
            }
            ranking.computeIfAbsent(currentRank, k -> new ArrayList<>()).add(curr);
        }
        return ranking;
    }

    public Map<Player, Integer> calculateRankingPoints(List<Player> players, int numPlayers) {
        Map<Integer, List<Player>> ranking = calculateFinalRanking(players);
        Map<Player, Integer> points = new HashMap<>();
        for (Map.Entry<Integer, List<Player>> entry : ranking.entrySet()) {
            int pts = resolvePoints(entry.getKey(), numPlayers);
            entry.getValue().forEach(p -> points.put(p, pts));
        }
        return points;
    }

    private int resolvePoints(int rank, int numPlayers) {
        return switch (numPlayers) {
            case 2 -> rank == 1 ? 1 : 0;
            case 3 -> switch (rank) { case 1 -> 1; case 2 -> 0; default -> -1; };
            case 4 -> switch (rank) { case 1 -> 2; case 2 -> 1; case 3 -> 0; default -> -1; };
            case 5 -> switch (rank) {
                case 1 -> 2; case 2 -> 1; case 3 -> 0; case 4 -> -1; default -> -2;
            };
            default -> 0;
        };
    }
}