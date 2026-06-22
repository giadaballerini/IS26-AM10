package it.polimi.ingsw.model.gamemanager;

import it.polimi.ingsw.enumerations.ColorPawnEnum;
import it.polimi.ingsw.model.player.Player;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RankingCalculatorTest {

    private final RankingCalculator calculator = new RankingCalculator();

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 5})
    void testCalculateRankingPointsAssignsCorrectPointsByRank(int numPlayers) {
        List<Player> players = buildPlayersWithDistinctPP(numPlayers);

        Map<Player, Integer> ranking = calculator.calculateRankingPoints(players, numPlayers);

        assertEquals(numPlayers, ranking.size());

        for (int rank = 1; rank <= numPlayers; rank++) {
            Player p = players.get(rank - 1);
            int expected = expectedPoints(rank, numPlayers);
            assertEquals(expected, ranking.get(p),
                    "Rank " + rank + " con " + numPlayers + " giocatori dovrebbe valere " + expected);
        }
    }

    private List<Player> buildPlayersWithDistinctPP(int numPlayers) {
        ColorPawnEnum[] colors = ColorPawnEnum.values();
        return java.util.stream.IntStream.range(0, numPlayers)
                .mapToObj(i -> {
                    Player p = new Player("p" + i, colors[i]);
                    p.addPP((numPlayers - i) * 10);
                    return p;
                })
                .toList();
    }


    private int expectedPoints(int rank, int numPlayers) {
        return switch (numPlayers) {
            case 2 -> rank == 1 ? 1 : 0;
            case 3 -> switch (rank) {
                case 1 -> 1;
                case 2 -> 0;
                default -> -1;
            };
            case 4 -> switch (rank) {
                case 1 -> 2;
                case 2 -> 1;
                case 3 -> 0;
                default -> -1;
            };
            case 5 -> switch (rank) {
                case 1 -> 2;
                case 2 -> 1;
                case 3 -> 0;
                case 4 -> -1;
                default -> -2;
            };
            default -> throw new IllegalArgumentException("numPlayers non gestito: " + numPlayers);
        };
    }
}