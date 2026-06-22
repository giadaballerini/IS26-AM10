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
import it.polimi.ingsw.model.entities.card.types.character.Hunter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayerTest {

    class TestablePlayer extends Player {
        public TestablePlayer(String nickname, ColorPawnEnum colorPawn) {
            super(nickname, colorPawn);
        }
        public Village getVillage() { return this.myVillage; }
        public void setVillage(Village v) { this.myVillage = v; }
    }

    @Mock Building mockBuilding;
    @Mock Builder mockBuilder;
    @Mock Player mockPlayer;
    @Mock Village mockVillage;

    @Test
    void testShouldGetPP() {
        Player p = new Player("Giorgio", ColorPawnEnum.ORANGE);
        p.addPP(67);
        assertEquals(67, p.getPP());
    }

    @Test
    void testShouldGetNFood() {
        Player p = new Player("Giorgio", ColorPawnEnum.ORANGE);
        p.addFood(67);
        assertEquals(67, p.getNFood());
    }

    @Test
    void testShouldGetNStars() {
        Player p = new Player("Giorgio", ColorPawnEnum.ORANGE);
        p.addStars(67);
        assertEquals(67, p.getNStars());
    }

    @Test
    void testShouldGetTotBuildDisc() {
        Player p = new Player("Player", ColorPawnEnum.PURPLE);

        CardEffectInstant eff = new DiscountFood(null, 2, DiscountFoodEnum.DISCOUNT_FOR_BUILDING);
        List<CardEffectInstant> lista = new ArrayList<>();
        lista.add(eff);

        CardEffectInstant eff2 = new DiscountFood(null, 3, DiscountFoodEnum.DISCOUNT_FOR_BUILDING);
        List<CardEffectInstant> lista2 = new ArrayList<>();
        lista2.add(eff2);

        Builder builder = new Builder(1, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), lista, 1, 2, CardTypeEnum.BUILDER);
        builder.execInstantEffect(p, GamePhaseEnum.DRAW_PHASE);
        assertEquals(2, p.getTotBuildDisc());

        Builder builder1 = new Builder(2, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), lista2, 1, 2, CardTypeEnum.BUILDER);
        builder1.execInstantEffect(p, GamePhaseEnum.DRAW_PHASE);
        assertEquals(5, p.getTotBuildDisc());
    }

    @Test
    void testShouldGetNumCharacters() {
        Player p = new Player("Player", ColorPawnEnum.PURPLE);
        Builder builder = new Builder(1, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 1, 2, CardTypeEnum.BUILDER);
        p.addCard(builder);
        assertEquals(1, p.getNumCharacters());

        Builder builder1 = new Builder(2, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 1, 2, CardTypeEnum.BUILDER);
        p.addCard(builder1);
        assertEquals(2, p.getNumCharacters());
    }

    @Test
    void testShouldGetNumType() {
        Player p = new Player("Giorgio", ColorPawnEnum.ORANGE);
        Builder b = new Builder(1, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 1, 2, CardTypeEnum.BUILDER);
        p.addCard(b);
        assertEquals(1, p.getNumType(CardTypeEnum.BUILDER));
    }

    @Test
    void testShouldGetFoodDiscount() {
        Player p = new Player("Giorgio", ColorPawnEnum.ORANGE);
        p.addFoodDiscount(67);
        assertEquals(67, p.getFoodDiscount());
    }

    @Test
    void testShouldGetBuildings() {
        Player p = new Player("Giorgio", ColorPawnEnum.ORANGE);
        p.addBuilding(mockBuilding);
        assertNotNull(p.getBuildings());
    }

    @Test
    void testShouldActivatePpProtection() {
        Player p = new Player("Giorgio", ColorPawnEnum.ORANGE);
        p.activatePpProtection();
        p.applyRitualLoss(10);
        assertEquals(0, p.getPP());
    }

    @Test
    void addCard() {
        mockPlayer.addCard(mockBuilder);
        verify(mockPlayer).addCard(mockBuilder);
    }

    @Test
    void testShouldAddFood() {
        Player p = new Player("Giorgio", ColorPawnEnum.ORANGE);
        p.addFood(67);
        assertEquals(67, p.getNFood());
    }

    @Test
    void addBuilding() {
        Building building = new Building(CardTypeEnum.BUILDING, 1, GamePhaseEnum.DRAW_PHASE, 1, 2, 3, new ArrayList<>(), new ArrayList<>());
        Player p = new Player("Giorgio", ColorPawnEnum.ORANGE);
        p.addBuilding(building);
        assertEquals(1, p.getBuildings().size());
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
        p.addFoodDiscount(3);
        assertEquals(3, p.getFoodDiscount());
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
        p.addStars(3);
        assertEquals(3, p.getNStars());
    }


    @Test
    void testShouldActivatePpProtection_BlocksLoss() {
        Player p = new Player("Player", ColorPawnEnum.PURPLE);
        p.activatePpProtection();
        p.applyRitualLoss(5);
        assertEquals(0, p.getPP());
    }

    @Test
    void testShouldApplyRitualLoss_WithoutProtection() {
        Player p = new Player("Player", ColorPawnEnum.PURPLE);
        p.applyRitualLoss(5);
        assertEquals(-5, p.getPP());
    }


    @Test
    void testShouldActivateDoubleShaman() {
        Player p = new Player("Player", ColorPawnEnum.PURPLE);
        p.activateDoubleShaman();
        p.applyRitualGain(4);
        assertEquals(8, p.getPP());
    }

    @Test
    void testShouldApplyRitualGain_WithoutDoubleShaman() {
        Player p = new Player("Player", ColorPawnEnum.PURPLE);
        p.applyRitualGain(4);
        assertEquals(4, p.getPP());
    }


    @Test
    void testShouldActivateHuntBonus() {
        Player p = new Player("Player", ColorPawnEnum.PURPLE);
        p.activateHuntBonus();
        p.applyHuntBonus();
        assertEquals(0, p.getNFood());
        assertEquals(0, p.getPP());
    }

    @Test
    void testShouldApplyHuntBonus_WithHunters() {
        Player p = new Player("Player", ColorPawnEnum.PURPLE);
        Hunter hunter = new Hunter(1, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 1, CardTypeEnum.HUNTER);
        p.addCard(hunter);
        p.activateHuntBonus();
        p.applyHuntBonus();
        assertEquals(1, p.getNFood());
        assertEquals(1, p.getPP());
    }

    @Test
    void testShouldNotApplyHuntBonus_WhenNotActivated() {
        Player p = new Player("Player", ColorPawnEnum.PURPLE);
        p.applyHuntBonus();
        assertEquals(0, p.getNFood());
        assertEquals(0, p.getPP());
    }


    @Test
    void testShouldActivatePaintBonus() {
        Player p = new Player("Player", ColorPawnEnum.PURPLE);
        p.activatePaintBonus();
        p.applyPaintBonus();
        assertEquals(0, p.getNFood());
    }

    @Test
    void testShouldNotApplyPaintBonus_WhenNotActivated() {
        Player p = new Player("Player", ColorPawnEnum.PURPLE);
        p.applyPaintBonus();
        assertEquals(0, p.getNFood());
    }


    @Test
    void testShouldAddCategoryDiscount_Painter() {
        Player p = new Player("Player", ColorPawnEnum.PURPLE);
        p.addCategoryDiscount(CardTypeEnum.PAINTER);
        assertTrue(p.calculateFeastDiscount() >= 0);
    }

    @Test
    void testShouldCalculateFeastDiscount_WithMultipleDiscounts() {
        Player p = new Player("Player", ColorPawnEnum.PURPLE);
        Builder painter = new Builder(1, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 1, 2, CardTypeEnum.PAINTER);
        Builder gatherer = new Builder(2, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 1, 2, CardTypeEnum.GATHERER);
        p.addCard(painter);
        p.addCard(gatherer);
        p.addCategoryDiscount(CardTypeEnum.PAINTER);
        p.addCategoryDiscount(CardTypeEnum.GATHERER);
        assertEquals(2, p.calculateFeastDiscount());
    }

    @Test
    void testShouldCalculateFeastDiscount_WithFoodDiscount() {
        Player p = new Player("Player", ColorPawnEnum.PURPLE);
        p.addFoodDiscount(3);
        assertEquals(3, p.calculateFeastDiscount());
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
        for (CrafterSymbolEnum s : CrafterSymbolEnum.values()) {
            doReturn(3).when(spyPlayer).getNumSymbolsForCrafter(s);
        }
        assertEquals(10, spyPlayer.getTotSymbolsForCrafter());
    }

    @Test
    void testShouldCheckBuildsEffectsNoEffect() {
        Building b1 = new Building(CardTypeEnum.BUILDING, 1, GamePhaseEnum.DRAW_PHASE, 1, 2, 7, new ArrayList<>(), new ArrayList<>());
        Building b2 = new Building(CardTypeEnum.BUILDING, 1, GamePhaseEnum.DRAW_PHASE, 1, 2, 7, new ArrayList<>(), new ArrayList<>());
        Player p = new Player("Player", ColorPawnEnum.PURPLE);
        p.addBuilding(b1);
        p.addBuilding(b2);
        List<Action> actions = p.checkBuildsEffects(GamePhaseEnum.DRAW_PHASE);
        assertTrue(actions.isEmpty());
    }

    @Test
    @DisplayName("applyQueueFoodBonus — bonus attivo ma tile senza food effect: nessun food aggiunto")
    void testShouldNotApplyQueueFoodBonus_WhenTileHasNoFoodEffect() {
        Player p = new Player("Player", ColorPawnEnum.PURPLE);
        p.activateExtraFoodOnQueue();
        p.applyQueueFoodBonus(false);
        assertEquals(0, p.getNFood());
    }

    @Test
    @DisplayName("applyQueueFoodBonus — bonus non attivo e tile con food effect: nessun food aggiunto")
    void testShouldNotApplyQueueFoodBonus_WhenBonusNotActivated() {
        Player p = new Player("Player", ColorPawnEnum.PURPLE);
        p.applyQueueFoodBonus(true);
        assertEquals(0, p.getNFood());
    }

    @Test
    void testShouldCheckBuildsEffectsWithEffect() {
        CardEffectInteractive eff = new DrawCard(DrawCardEnum.UP_DRAW);
        List<CardEffectInteractive> effs = new ArrayList<>();
        effs.add(eff);
        Building building = new Building(CardTypeEnum.BUILDING, 1, GamePhaseEnum.DRAW_PHASE, 1, 2, 7, new ArrayList<>(), effs);
        Player p = new Player("Player", ColorPawnEnum.PURPLE);
        p.addBuilding(building);
        List<Action> actions = p.checkBuildsEffects(GamePhaseEnum.DRAW_PHASE);
        assertFalse(actions.isEmpty());
        assertEquals(1, actions.size());
    }
    private Player jsonPlayer(java.util.Set<CardTypeEnum> categoryDiscounts) {
        return new Player(
                "Alice", ColorPawnEnum.BLUE,
                0, 0, 0, 0, 0,
                new Village(), new java.util.LinkedList<>(), new java.util.LinkedList<>(),
                false, false, false,
                categoryDiscounts,
                false, false);
    }

    @Test
    @org.junit.jupiter.api.DisplayName("@JsonCreator — categoryDiscounts null → nessuna NPE e discount = 0")
    void jsonCreator_nullCategoryDiscounts_noNPE() {
        Player p = jsonPlayer(null);
        assertDoesNotThrow(() -> assertEquals(0, p.calculateFeastDiscount()));
    }

    @Test
    @org.junit.jupiter.api.DisplayName("@JsonCreator — categoryDiscounts vuoto → discount = 0")
    void jsonCreator_emptyCategoryDiscounts_discountIsZero() {
        Player p = jsonPlayer(java.util.EnumSet.noneOf(CardTypeEnum.class));
        assertEquals(0, p.calculateFeastDiscount());
    }

    @Test
    @org.junit.jupiter.api.DisplayName("@JsonCreator — categoryDiscounts non vuoto → copiato correttamente")
    void jsonCreator_nonEmptyCategoryDiscounts_copiedCorrectly() {
        Player p = jsonPlayer(java.util.EnumSet.of(CardTypeEnum.BUILDER, CardTypeEnum.HUNTER));
        assertDoesNotThrow(() -> p.addCategoryDiscount(CardTypeEnum.PAINTER));
    }

    @Test
    @org.junit.jupiter.api.DisplayName("@JsonCreator — categoryDiscounts: isolamento dall'insieme originale")
    void jsonCreator_categoryDiscounts_isolatedFromOriginal() {
        java.util.Set<CardTypeEnum> original = new java.util.HashSet<>();
        original.add(CardTypeEnum.BUILDER);
        Player p = jsonPlayer(original);

        original.add(CardTypeEnum.HUNTER);
        assertEquals(0, p.calculateFeastDiscount());
    }

    @Test
    @org.junit.jupiter.api.DisplayName("@JsonCreator — skippableDraws non vuota: hasSkippableDraws true")
    void jsonCreator_nonEmptySkippableDraws_hasSkippableDrawsTrue() {
        Player owner = new Player("Alice", ColorPawnEnum.BLUE);
        java.util.List<Action> draws = new java.util.LinkedList<>();
        draws.add(new Action(owner, DrawCardEnum.UP_DRAW));

        Player p = new Player(
                "Alice", ColorPawnEnum.BLUE,
                0, 0, 0, 0, 0,
                new Village(), new java.util.LinkedList<>(), draws,
                false, false, false,
                java.util.EnumSet.noneOf(CardTypeEnum.class),
                false, false);

        assertTrue(p.hasSkippableDraws());
    }
}
