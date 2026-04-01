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

import static org.junit.jupiter.api.Assertions.*;

class FeastTest {
    public class PlayerTest extends Player{
        public PlayerTest(String name, ColorPawnEnum color) {
            super(name, color);
        }

        public Village getVillage(){
            return myVillage;
        }
    }
    @Test
    void execEvent() {
        Feast feast = new Feast(4 , GamePhaseEnum.PLAY_EVENT, new ArrayList<>(), new ArrayList<>(), 1, 2, 3, CardTypeEnum.FEAST);
        PlayerTest player1 = new PlayerTest("Player1", ColorPawnEnum.PURPLE);
        PlayerTest player2 = new PlayerTest("Player2", ColorPawnEnum.ORANGE);
        player1.addFood(0);
        player2.addFood(40);
        player1.addFoodDiscount(2);
        player2.addFoodDiscount(1);
        player1.addPP(100);
        Village v1 = player1.getVillage();
        Village v2 = player2.getVillage();
        List players = new ArrayList<>();
        players.add(player1);
        players.add(player2);
        Builder builder1 = new Builder(1, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, CardTypeEnum.BUILDER);
        Builder builder2 = new Builder(2, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, CardTypeEnum.BUILDER);
        Gatherer gatherer1 = new Gatherer(3, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, CardTypeEnum.GATHERER);
        v1.add(builder1);
        v1.add(builder2);
        v1.add(gatherer1);
        v2.add(builder1);
        v2.add(builder2);
        feast.execEvent(players, GamePhaseEnum.PLAY_EVENT);
        assertEquals(38, player2.getNFood());
        assertEquals(0, player1.getNFood());
        assertEquals(97, player1.getPP());


    }
}//uiiaiuuiiiai