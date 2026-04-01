package it.polimi.ingsw.model.player;

import it.polimi.ingsw.enumerations.*;
import it.polimi.ingsw.model.action.Action;
import it.polimi.ingsw.model.entities.card.effects.instant.CardEffectInstant;
import it.polimi.ingsw.model.entities.card.effects.instant.DiscountFood;
import it.polimi.ingsw.model.entities.card.effects.interactive.CardEffectInteractive;
import it.polimi.ingsw.model.entities.card.effects.interactive.DrawCard;
import it.polimi.ingsw.model.entities.card.types.building.Building;
import it.polimi.ingsw.model.entities.card.types.character.Builder;
import it.polimi.ingsw.model.entities.card.types.character.Crafter;
import it.polimi.ingsw.model.gamemanager.GameManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.internal.matchers.Null;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayerTest {

    class TestablePlayer extends Player{
        private String nickname;
        private ColorPawnEnum colorPawn;

        public TestablePlayer(String nickname, ColorPawnEnum colorPawn) {
            super(nickname,colorPawn);
        }
        public Village getVillage(){return this.myVillage;}
        public void setVillage(Village v){this.myVillage = v;}
    }

    @Mock
    Building mockBuilding;

    @Mock
    Builder mockBuilder;

    @Mock
    Player mockPlayer;

    @Mock
    Village mockVillage;

    @Test
    void testShouldGetPP() {
        Player realPlayer = new Player("Giorgio", ColorPawnEnum.ORANGE);
        realPlayer.addPP(67);

        assertEquals(67, realPlayer.getPP());
    }

    @Test
    void testShouldGetNFood() {
        Player realPlayer = new Player("Giorgio", ColorPawnEnum.ORANGE);
        realPlayer.addFood(67);

        assertEquals(67, realPlayer.getNFood());
    }

    @Test
    void testShouldGetNStars() {
        Player realPlayer = new Player("Giorgio", ColorPawnEnum.ORANGE);
        realPlayer.addStars(67);

        assertEquals(67, realPlayer.getNStars());
    }


    @Test
    void testShouldGetTotBuildDisc() {
        Player p = new Player("Player", ColorPawnEnum.PURPLE);
        assertNotNull(p);

        CardEffectInstant eff = new DiscountFood(null,2, DiscountFoodEnum.DISCOUNT_FOR_BUILDING);
        List<CardEffectInstant> lista = new ArrayList<>();
        lista.add(eff);

        CardEffectInstant eff2 = new DiscountFood(null,3, DiscountFoodEnum.DISCOUNT_FOR_BUILDING);
        List<CardEffectInstant> lista2 = new ArrayList<>();
        lista2.add(eff2);

        Builder builder = new Builder(1, GamePhaseEnum.DRAW_PHASE,new ArrayList<>(),lista, 1,  CardTypeEnum.BUILDER);
        assertNotNull(builder);

        builder.execInstantEffect(p, GamePhaseEnum.DRAW_PHASE);
        assertEquals(2, p.getTotBuildDisc()); //Fail qui

        Builder builder1 = new Builder(2, GamePhaseEnum.DRAW_PHASE,new ArrayList<>(),lista2, 1, CardTypeEnum.BUILDER);
        assertNotNull(builder1);

        builder1.execInstantEffect(p, GamePhaseEnum.DRAW_PHASE);

        assertEquals(5, p.getTotBuildDisc());
    }

    @Test
    void testShouldGetNumCharacters() {
        Player p = new Player("Player", ColorPawnEnum.PURPLE);
        assertNotNull(p);
        Builder builder = new Builder(1, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 1,  CardTypeEnum.BUILDER);
        assertNotNull(builder);
        p.addCard(builder);
        assertEquals(1, p.getNumCharacters()); //Fail qui
        Builder builder1 = new Builder(2, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 1,  CardTypeEnum.BUILDER);
        assertNotNull(builder1);
        p.addCard(builder1);
        assertEquals(2, p.getNumCharacters());

    }



    @Test
    void testShouldGetNumType() {
        Player realPlayer = new Player("Giorgio", ColorPawnEnum.ORANGE);
        Builder b = new Builder(1, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 1,  CardTypeEnum.BUILDER);
        realPlayer.addCard(b);
        assertEquals(1, realPlayer.getNumType(CardTypeEnum.BUILDER));
    }

    @Test
    void testShouldGetFoodDiscount() {
        Player realPlayer = new Player("Giorgio", ColorPawnEnum.ORANGE);

        realPlayer.addFoodDiscount(67);

        assertEquals(67, realPlayer.getFoodDiscount());
    }

    @Test
    void testShouldGetBuildings() {
        Player realPlayer = new Player("Giorgio", ColorPawnEnum.ORANGE);

        realPlayer.addBuilding(mockBuilding);

        assertNotNull(realPlayer.getBuildings());
    }

    @Test
    void TestShouldGetHasProtection() {
        Player realPlayer = new Player("Giorgio", ColorPawnEnum.ORANGE);

        realPlayer.addProtection();

        assertTrue(realPlayer.getHasProtection());
    }

    @Test
    void addCard() {
        mockPlayer.addCard(mockBuilder);

        verify(mockPlayer).addCard(mockBuilder);
    }

    @Test
    void testShouldAddFood() {
        Player realPlayer = new Player("Giorgio", ColorPawnEnum.ORANGE);
        realPlayer.addFood(67);
        assertEquals(67, realPlayer.getNFood());

    }

    @Test
    void addBuilding() {
        Building building = new Building(1, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 1, 2, 3, CardTypeEnum.BUILDING);
        Player realPlayer = new Player("Giorgio", ColorPawnEnum.ORANGE);
        realPlayer.addBuilding(building);
        assertEquals(1, realPlayer.getBuildings().size());
    }

    @Test
    void addPP() {
        Player p = new Player("Player", ColorPawnEnum.PURPLE);
        p.addPP(2);
        assertEquals(2, p.getPP());

    }

    @Test
    void testShouldAddFoodDiscount() {
        Player p = new Player("Player", ColorPawnEnum.PURPLE);
        assertNotNull(p);
        p.addFoodDiscount(3);
        assertEquals(3,p.getFoodDiscount());
    }

    @Test
    void getNumSymbolsForCrafter() {
        TestablePlayer p = new TestablePlayer("Player", ColorPawnEnum.PURPLE);
        Crafter crafter1 = new Crafter(1, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, CrafterSymbolEnum.AMIGDALA, CardTypeEnum.CRAFTER);
        Village v = p.getVillage();
        v.add(crafter1);
        assertEquals(1, p.getNumSymbolsForCrafter(CrafterSymbolEnum.AMIGDALA));
    }

    @Test
    void testShouldAddStars() {
        Player p = new Player("Player", ColorPawnEnum.PURPLE);
        assertNotNull(p);
        p.addStars(3);
        assertEquals(3, p.getNStars());

    }

    @Test
    void testShouldAddProtection() {
        Player p = new Player("Player", ColorPawnEnum.PURPLE);
        assertFalse(p.getHasProtection());
        p.addProtection();

        assertTrue(p.getHasProtection());
    }

    @Test
    void testShouldAddDouble() {
        Player p = new Player("Player", ColorPawnEnum.PURPLE);
        assertFalse(p.getHasDoubleShamanIncome());
        p.addDouble();

        assertTrue(p.getHasDoubleShamanIncome());
    }

    @Test
    void testShouldGetBuilderPoints() {
        TestablePlayer p = new TestablePlayer("Player", ColorPawnEnum.PURPLE);

        p.setVillage(mockVillage);
        p.getBuilderPoints();

        verify(mockVillage).builderPoints();
    }

    @Test
    void testShouldGetTotSymbolsForCrafter() {
        Player realPlayer = new Player("Player", ColorPawnEnum.ORANGE);
        Player spyPlayer = spy(realPlayer);

        for(CrafterSymbolEnum s : CrafterSymbolEnum.values()) {
            doReturn(3).when(spyPlayer).getNumSymbolsForCrafter(s);
        }

        assertEquals(10, spyPlayer.getTotSymbolsForCrafter());
    }

    @Test
    void testShouldCheckBuildsEffectsNoEffect() {
        Building building = new Building(1, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 1, 2, 7, CardTypeEnum.BUILDING);

        Building building2 = new Building(1, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 1, 2, 7, CardTypeEnum.BUILDING);

        Player p = new Player("Player", ColorPawnEnum.PURPLE);

        p.addBuilding(building);
        p.addBuilding(building2);
        List<Action> actions = p.checkBuildsEffects(GamePhaseEnum.DRAW_PHASE);
        assertTrue(actions.isEmpty());
    }

    @Test
    void testShouldCheckBuildsEffectsWithEffect() {

        CardEffectInteractive eff = new DrawCard(DrawCardEnum.UP_DRAW);

        List<CardEffectInteractive> effs = new ArrayList<>();
        effs.add(eff);

        Building building = new Building(1, GamePhaseEnum.DRAW_PHASE, effs, new ArrayList<>(), 1, 2, 7, CardTypeEnum.BUILDING);

        Player p = new Player("Player", ColorPawnEnum.PURPLE);

        p.addBuilding(building);

        List<Action> actions = p.checkBuildsEffects(GamePhaseEnum.DRAW_PHASE);
        assertFalse(actions.isEmpty());
        assertEquals(1, actions.size());
    }

    @Test
    void testShouldSetHuntFlag() {
        Player p = new Player("Player", ColorPawnEnum.PURPLE);
        p.setHuntFlag(true);
        assertTrue(p.hasHuntFlag());
    }

    @Test
    void testShouldHasHuntFlag() {
        Player p = new Player("Player", ColorPawnEnum.PURPLE);
        assertFalse(p.hasHuntFlag());
    }

    @Test
    void testShouldGetHasDoubleShamanIncome() {
        Player p = new Player("Player", ColorPawnEnum.PURPLE);
        assertFalse(p.getHasDoubleShamanIncome());
    }
}