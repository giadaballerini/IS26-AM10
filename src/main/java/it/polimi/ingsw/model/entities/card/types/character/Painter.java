package it.polimi.ingsw.model.entities.card.types.character;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.entities.card.effects.instant.CardEffectInstant;
import it.polimi.ingsw.model.entities.card.effects.instant.GainFood;
import it.polimi.ingsw.model.entities.card.effects.instant.GainPP;
import it.polimi.ingsw.model.entities.card.effects.interactive.CardEffectInteractive;
import it.polimi.ingsw.model.interfaces.GainFoodVisitor;
import it.polimi.ingsw.model.interfaces.GainPPVisitor;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.visitors.VillageVisitor;

import java.util.List;

/**
 * A character card representing a painter villager.
 *
 * <p>Painters contribute to two end-game bonuses: every pair of painters in
 * the village scores 10 prestige points, and when the paint bonus is active
 * (via {@link Player#activatePaintBonus()}), each painter grants one extra food
 * when the player enters a queue tile.</p>
 */
public class Painter extends Character {

    /**
     * Constructs a {@code Painter} from its JSON properties.
     *
     * @param id                 unique card identifier
     * @param trigger            the game phase that gates this card's instant effects
     * @param interactiveEffects interactive effects carried by this card
     * @param instantEffects     instant effects carried by this card
     * @param age                the age this card belongs to (1–3)
     * @param type               the card type (always {@link CardTypeEnum#PAINTER})
     */
    @JsonCreator
    public Painter(@JsonProperty("id") int id,
                   @JsonProperty("trigger") GamePhaseEnum trigger,
                   @JsonProperty("interactiveEffects") List<CardEffectInteractive> interactiveEffects,
                   @JsonProperty("instantEffects") List<CardEffectInstant> instantEffects,
                   @JsonProperty("age") int age,
                   @JsonProperty("type") CardTypeEnum type) {
        super(id, trigger, interactiveEffects, instantEffects, age, type);
    }

    /**
     * Accepts a {@link GainFoodVisitor}, dispatching to
     * {@link GainFoodVisitor#visit(Painter, Player, GainFood)}.
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
     * {@link GainPPVisitor#visit(Painter, Player, GainPP)}.
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
     * Accepts a {@link VillageVisitor}, dispatching to
     * {@link VillageVisitor#visit(Painter)}.
     *
     * @param visitor the visitor to dispatch to
     */
    @Override
    public void accept(VillageVisitor visitor) { visitor.visit(this); }
}