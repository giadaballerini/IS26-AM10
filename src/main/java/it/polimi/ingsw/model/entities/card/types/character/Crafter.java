package it.polimi.ingsw.model.entities.card.types.character;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.CrafterSymbolEnum;
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
 * A character card representing a crafter villager.
 *
 * <p>Each crafter carries a {@link CrafterSymbolEnum symbol}. At the end of
 * the game, a player scores PP equal to the number of crafters they own
 * multiplied by the number of distinct symbol types among those crafters.
 * Certain card effects also grant bonuses based on how many crafters with a
 * specific symbol are in the village.</p>
 */
public class Crafter extends Character {

    /** The symbol carried by this crafter, used for end-game scoring and effect calculations. */
    private final CrafterSymbolEnum symbol;

    /**
     * Constructs a {@code Crafter} from its JSON properties.
     *
     * @param id                 unique card identifier
     * @param trigger            the game phase that gates this card's instant effects
     * @param interactiveEffects interactive effects carried by this card
     * @param instantEffects     instant effects carried by this card
     * @param age                the age this card belongs to (1–3)
     * @param symbol             the crafter symbol carried by this card
     * @param type               the card type (always {@link CardTypeEnum#CRAFTER})
     */
    @JsonCreator
    public Crafter(@JsonProperty("id") int id,
                   @JsonProperty("trigger") GamePhaseEnum trigger,
                   @JsonProperty("interactiveEffects") List<CardEffectInteractive> interactiveEffects,
                   @JsonProperty("instantEffects") List<CardEffectInstant> instantEffects,
                   @JsonProperty("age") int age,
                   @JsonProperty("symbol") CrafterSymbolEnum symbol,
                   @JsonProperty("type") CardTypeEnum type) {
        super(id, trigger, interactiveEffects, instantEffects, age, type);
        this.symbol = symbol;
    }

    /**
     * Returns the symbol carried by this crafter.
     *
     * @return the {@link CrafterSymbolEnum} value; never {@code null}
     */
    public CrafterSymbolEnum getSymbol() {
        return this.symbol;
    }

    /**
     * Accepts a {@link GainFoodVisitor}, dispatching to
     * {@link GainFoodVisitor#visit(Crafter, Player, GainFood)}.
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
     * {@link GainPPVisitor#visit(Crafter, Player, GainPP)}.
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
     * {@link VillageVisitor#visit(Crafter)}.
     *
     * @param visitor the visitor to dispatch to
     */
    @Override
    public void accept(VillageVisitor visitor) { visitor.visit(this); }
}