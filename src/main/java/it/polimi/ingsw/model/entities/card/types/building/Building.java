package it.polimi.ingsw.model.entities.card.types.building;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.action.Action;
import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.entities.card.effects.instant.CardEffectInstant;
import it.polimi.ingsw.model.entities.card.effects.instant.GainFood;
import it.polimi.ingsw.model.entities.card.effects.instant.GainPP;
import it.polimi.ingsw.model.entities.card.effects.interactive.CardEffectInteractive;
import it.polimi.ingsw.model.interfaces.GainFoodVisitor;
import it.polimi.ingsw.model.interfaces.GainPPVisitor;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.visitors.CanDrawVisitor;
import it.polimi.ingsw.visitors.DrawCardVisitor;
import it.polimi.ingsw.visitors.PlayEventVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a building card that a player can acquire during the game.
 *
 * <p>Buildings are purchased by paying a food cost and grant a fixed PP value
 * at the end of the game. They may also carry instant and interactive effects
 * that fire when their trigger phase matches the current game phase, making
 * them a source of ongoing bonuses throughout the match.</p>
 */
public class Building extends Card {

    /** The prestige point value this building contributes at the end of the game. */
    private final int ppValue;

    /** The food cost required to acquire this building. */
    private final int foodCost;

    /**
     * Constructs a {@code Building} from its JSON properties.
     *
     * @param type               the card type (always {@link CardTypeEnum#BUILDING})
     * @param id                 unique card identifier
     * @param trigger            the game phase that gates this building's instant effects
     * @param age                the age this building belongs to (1–3)
     * @param ppValue            the end-game PP value of this building
     * @param foodCost           the food cost to acquire this building
     * @param instantEffects     instant effects carried by this building;
     *                           may be {@code null}
     * @param interactiveEffects interactive effects carried by this building;
     *                           may be {@code null}
     */
    @JsonCreator
    public Building(@JsonProperty("type") CardTypeEnum type,
                    @JsonProperty("id") int id,
                    @JsonProperty("trigger") GamePhaseEnum trigger,
                    @JsonProperty("age") int age,
                    @JsonProperty("ppValue") int ppValue,
                    @JsonProperty("foodCost") int foodCost,
                    @JsonProperty("instantEffects") List<CardEffectInstant> instantEffects,
                    @JsonProperty("interactiveEffects") List<CardEffectInteractive> interactiveEffects) {
        super(id, trigger, interactiveEffects, instantEffects, age, type);
        this.ppValue = ppValue;
        this.foodCost = foodCost;
    }

    /**
     * Returns the prestige point value this building contributes at the end of
     * the game.
     *
     * @return end-game PP value
     */
    public int getPpValue() {
        return ppValue;
    }

    /**
     * Returns the food cost required to acquire this building.
     *
     * @return food cost
     */
    public int getFoodCost() {
        return foodCost;
    }

    /**
     * Accepts a {@link GainFoodVisitor}, dispatching to
     * {@link GainFoodVisitor#visit(it.polimi.ingsw.model.entities.card.types.building.Building, Player, GainFood)}.
     *
     * @param visitor the visitor to dispatch to
     * @param p       the player receiving the food gain
     * @param e       the food gain effect being applied
     */
    @Override
    public void accept(GainFoodVisitor visitor, Player p, GainFood e) {
        visitor.visit(this, p, e);
    }

    /**
     * Accepts a {@link GainPPVisitor}, dispatching to
     * {@link GainPPVisitor#visit(it.polimi.ingsw.model.entities.card.types.building.Building, Player, GainPP)}.
     *
     * @param visitor the visitor to dispatch to
     * @param p       the player receiving the PP gain
     * @param e       the PP gain effect being applied
     */
    @Override
    public void accept(GainPPVisitor visitor, Player p, GainPP e) {
        visitor.visit(this, p, e);
    }

    /**
     * Buildings do not participate in event resolution; this method is a no-op.
     *
     * @param visitor the event visitor (unused)
     */
    @Override
    public void accept(PlayEventVisitor visitor) {}

    /**
     * Accepts a {@link CanDrawVisitor}, delegating to
     * {@link CanDrawVisitor#visit(Building)}.
     *
     * @param visitor the visitor to dispatch to
     */
    @Override
    public void accept(CanDrawVisitor visitor) { visitor.visit(this); }

    /**
     * Accepts a {@link DrawCardVisitor}, delegating to
     * {@link DrawCardVisitor#visit(Building)}.
     *
     * @param visitor the visitor to dispatch to
     */
    @Override
    public void accept(DrawCardVisitor visitor) { visitor.visit(this); }

    /**
     * Applies all interactive effects of this building to the given player,
     * collecting the resulting pending actions.
     *
     * @param p the player to apply the effects to; must not be {@code null}
     * @return list of {@link Action} instances generated; never {@code null}
     */
    @Override
    public List<Action> execInteractiveEffect(Player p) {
        List<Action> actions = new ArrayList<>();
        for (CardEffectInteractive e : interactiveEffects) {
            Action a = e.apply(p);
            actions.add(a);
        }
        return actions;
    }
}