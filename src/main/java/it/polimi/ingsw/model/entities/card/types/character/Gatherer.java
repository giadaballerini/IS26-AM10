package it.polimi.ingsw.model.entities.card.types.character;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.entities.card.effects.instant.CardEffectInstant;
import it.polimi.ingsw.model.entities.card.effects.interactive.CardEffectInteractive;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.visitors.VillageVisitor;

import java.util.List;

/**
 * A character card representing a gatherer villager.
 *
 * <p>Gatherers provide food-related bonuses through their instant effects.
 * They carry no additional fields beyond those defined in {@link Character}.</p>
 */
public class Gatherer extends Character {

    /**
     * Constructs a {@code Gatherer} from its JSON properties.
     *
     * @param id                 unique card identifier
     * @param trigger            the game phase that gates this card's instant effects
     * @param interactiveEffects interactive effects carried by this card
     * @param instantEffects     instant effects carried by this card
     * @param age                the age this card belongs to (1–3)
     * @param type               the card type (always {@link CardTypeEnum#GATHERER})
     */
    @JsonCreator
    public Gatherer(@JsonProperty("id") int id,
                    @JsonProperty("trigger") GamePhaseEnum trigger,
                    @JsonProperty("interactiveEffects") List<CardEffectInteractive> interactiveEffects,
                    @JsonProperty("instantEffects") List<CardEffectInstant> instantEffects,
                    @JsonProperty("age") int age,
                    @JsonProperty("type") CardTypeEnum type) {
        super(id, trigger, interactiveEffects, instantEffects, age, type);
    }



    /**
     * Accepts a {@link VillageVisitor}, dispatching to
     * {@link VillageVisitor#visit(Gatherer)}.
     *
     * @param visitor the visitor to dispatch to
     */
    @Override
    public void accept(VillageVisitor visitor) { visitor.visit(this); }
}