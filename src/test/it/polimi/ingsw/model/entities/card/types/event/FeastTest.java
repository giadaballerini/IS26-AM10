package it.polimi.ingsw.model.entities.card.types.event;

import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.ColorPawnEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.entities.card.types.character.Builder;
import it.polimi.ingsw.model.entities.card.types.character.Gatherer;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Village;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FeastTest {


    static class TestPlayer extends Player {
        public TestPlayer(String name, ColorPawnEnum color) {
            super(name, color);
        }

        public Village getVillage() {
            return myVillage;
        }
    }

    private final Feast feast = new Feast(
            4, GamePhaseEnum.PLAY_EVENT,
            new ArrayList<>(), new ArrayList<>(),
            1, 2, 3, CardTypeEnum.FEAST);


    private Builder newBuilder(int id) {
        return new Builder(id, GamePhaseEnum.DRAW_PHASE,
                new ArrayList<>(), new ArrayList<>(), 2, 2, CardTypeEnum.BUILDER);
    }

    private Gatherer newGatherer(int id) {
        return new Gatherer(id, GamePhaseEnum.DRAW_PHASE,
                new ArrayList<>(), new ArrayList<>(), 2, CardTypeEnum.GATHERER);
    }

    @Test
    void execEvent_WithFlatFoodDiscount() {
        TestPlayer player1 = new TestPlayer("Player1", ColorPawnEnum.PURPLE);
        player1.addFood(0);
        player1.addPP(100);
        player1.addFoodDiscount(2);
        Village v1 = player1.getVillage();
        v1.add(newBuilder(1));
        v1.add(newBuilder(2));
        v1.add(newGatherer(3));

        TestPlayer player2 = new TestPlayer("Player2", ColorPawnEnum.ORANGE);
        player2.addFood(40);
        player2.addFoodDiscount(1);
        Village v2 = player2.getVillage();
        v2.add(newBuilder(4));
        v2.add(newBuilder(5));

        feast.execEvent(List.of(player1, player2), GamePhaseEnum.PLAY_EVENT);

        assertEquals(97, player1.getPP(), "player1 dovrebbe perdere 3 PP");
        assertEquals(0,  player1.getNFood(), "player1 non ha cibo, deve restare 0");

        assertEquals(38, player2.getNFood(), "player2 dovrebbe perdere 2 food");
    }


    @Test
    void execEvent_WithCategoryDiscount_ZeroCost() {
        TestPlayer player = new TestPlayer("Player", ColorPawnEnum.PURPLE);
        player.addFood(40);
        player.addCategoryDiscount(CardTypeEnum.GATHERER);
        Village v = player.getVillage();
        v.add(newGatherer(1));
        v.add(newGatherer(2));

        feast.execEvent(List.of(player), GamePhaseEnum.PLAY_EVENT);

        assertEquals(40, player.getNFood(),
                "lo sconto per categoria copre tutto il costo: food deve restare 40");
    }

    @Test
    void execEvent_NoFood_PaysEntirelyWithPP() {
        TestPlayer player = new TestPlayer("Player", ColorPawnEnum.PURPLE);
        Village v = player.getVillage();
        v.add(newBuilder(1));
        v.add(newBuilder(2));

        feast.execEvent(List.of(player), GamePhaseEnum.PLAY_EVENT);

        assertEquals(-6, player.getPP(),
                "2 personaggi × 3 PP/personaggio = -6 PP totali");
        assertEquals(0, player.getNFood(),
                "il cibo non deve cambiare se il giocatore non ne ha");
    }

    @Test
    void execEvent_EmptyVillage_NoCostApplied() {
        TestPlayer player = new TestPlayer("Player", ColorPawnEnum.PURPLE);
        player.addFood(10);
        player.addPP(50);

        feast.execEvent(List.of(player), GamePhaseEnum.PLAY_EVENT);

        assertEquals(50, player.getPP(),
                "villaggio vuoto: i PP non devono cambiare");
        assertEquals(10, player.getNFood(),
                "villaggio vuoto: il cibo non deve cambiare");
    }

    @Test
    void execEvent_EnoughFood_NoPPLost() {
        TestPlayer player = new TestPlayer("Player", ColorPawnEnum.PURPLE);
        player.addFood(20);
        player.addPP(50);
        Village v = player.getVillage();
        v.add(newBuilder(1));
        v.add(newBuilder(2));

        feast.execEvent(List.of(player), GamePhaseEnum.PLAY_EVENT);

        assertEquals(50, player.getPP(),
                "con cibo sufficiente non deve perdersi nessun PP");
        assertEquals(16, player.getNFood(),
                "2 × foodCost(2) = 4 food totali consumati");
    }
}