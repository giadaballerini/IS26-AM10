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
 * A character card representing a builder villager.
 *
 * <p>Each builder contributes a fixed number of prestige points at the end of
 * the game, calculated in {@link it.polimi.ingsw.model.player.Village#builderPoints()}
 * by summing the {@link #ppAmount} of all builders in the village.</p>
 */
public class Builder extends Character {

    /** The prestige points this builder contributes at the end of the game. */
    @JsonProperty
    private final int ppAmount;

    /**
     * Constructs a {@code Builder} from its JSON properties.
     *
     * @param id                 unique card identifier
     * @param trigger            the game phase that gates this card's instant effects
     * @param interactiveEffects interactive effects carried by this card
     * @param instantEffects     instant effects carried by this card
     * @param age                the age this card belongs to (1–3)
     * @param ppAmount           the PP value this builder contributes at end of game
     * @param type               the card type (always {@link CardTypeEnum#BUILDER})
     */
    @JsonCreator
    public Builder(@JsonProperty("id") int id,
                   @JsonProperty("trigger") GamePhaseEnum trigger,
                   @JsonProperty("interactiveEffects") List<CardEffectInteractive> interactiveEffects,
                   @JsonProperty("instantEffects") List<CardEffectInstant> instantEffects,
                   @JsonProperty("age") int age,
                   @JsonProperty("ppAmount") int ppAmount,
                   @JsonProperty("type") CardTypeEnum type) {
        super(id, trigger, interactiveEffects, instantEffects, age, type);
        this.ppAmount = ppAmount;
    }

    /**
     * Accepts a {@link GainFoodVisitor}, dispatching to
     * {@link GainFoodVisitor#visit(Builder, Player, GainFood)}.
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
     * {@link GainPPVisitor#visit(Builder, Player, GainPP)}.
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
     * {@link VillageVisitor#visit(Builder)}.
     *
     * @param visitor the visitor to dispatch to
     */
    @Override
    public void accept(VillageVisitor visitor) { visitor.visit(this); }

    /**
     * Returns the prestige points this builder contributes at the end of the
     * game.
     *
     * @return PP amount
     */
    public int getPpAmount() {
        return this.ppAmount;
    }
}