package it.polimi.ingsw.model.gamemanager;

import it.polimi.ingsw.model.player.Player;

import java.util.*;

/**
 * Computes the final ranking and global ranking point awards for a match.
 *
 * <p>Players are ranked by prestige points (descending), with food count as
 * a tiebreaker (descending). Players with equal PP and food share the same
 * rank. Each rank maps to a number of global ranking points that depends on
 * the total player count; lower-ranking players in larger matches may receive
 * a negative point award.</p>
 */
public class RankingCalculator {

    /**
     * Computes the final ranking for the given list of players.
     *
     * <p>Players are sorted by prestige points descending, with food count as
     * a tiebreaker. Players with identical PP and food share the same rank.
     * The result is a {@link TreeMap} keyed by rank (1-based), where each
     * entry holds the list of players at that rank.</p>
     *
     * @param players the list of players to rank; may be {@code null} or empty
     * @return an ordered map of rank → players; never {@code null},
     *         empty if {@code players} is {@code null} or empty
     */
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

    /**
     * Computes the global ranking points earned by each player in this match.
     *
     * <p>The point award for each rank is determined by {@link #resolvePoints(int, int)}
     * and varies with the number of players. Players sharing a rank receive the
     * same number of points.</p>
     *
     * @param players    the list of players to award points to
     * @param numPlayers the total number of players in the match (2–5)
     * @return a map from each player to their global ranking point award;
     *         never {@code null}
     */
    public Map<Player, Integer> calculateRankingPoints(List<Player> players, int numPlayers) {
        Map<Integer, List<Player>> ranking = calculateFinalRanking(players);
        Map<Player, Integer> points = new HashMap<>();
        for (Map.Entry<Integer, List<Player>> entry : ranking.entrySet()) {
            int pts = resolvePoints(entry.getKey(), numPlayers);
            entry.getValue().forEach(p -> points.put(p, pts));
        }
        return points;
    }

    /**
     * Returns the global ranking points awarded to a player finishing at the
     * given rank in a match with the given number of players.
     *
     * <p>Point values by player count:
     * <ul>
     *   <li>2 players: 1st → +1, others → 0</li>
     *   <li>3 players: 1st → +1, 2nd → 0, 3rd → −1</li>
     *   <li>4 players: 1st → +2, 2nd → +1, 3rd → 0, 4th → −1</li>
     *   <li>5 players: 1st → +2, 2nd → +1, 3rd → 0, 4th → −1, 5th → −2</li>
     * </ul>
     *
     * @param rank       the finishing rank (1-based)
     * @param numPlayers the total number of players in the match (2–5)
     * @return the number of global ranking points for that rank and player count
     */
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
    /**
     * Creates a new {@code RankingCalculator} instance.
     */
    public RankingCalculator() {
    }
}