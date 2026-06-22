package it.polimi.ingsw.model.entities.card.types.character;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.entities.card.effects.instant.CardEffectInstant;
import it.polimi.ingsw.model.entities.card.effects.interactive.CardEffectInteractive;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Village;
import it.polimi.ingsw.visitors.CanDrawVisitor;
import it.polimi.ingsw.visitors.DrawCardVisitor;
import it.polimi.ingsw.visitors.PlayEventVisitor;
import it.polimi.ingsw.visitors.VillageVisitor;

import java.util.List;

/**
 * Abstract base class for all character cards.
 *
 * <p>Character cards are placed into a player's {@link Village} when drawn.
 * Each concrete subclass ({@link Builder}, {@link Crafter}, {@link Gatherer},
 * {@link Hunter}, {@link Painter}, {@link Shaman}) represents a distinct
 * villager role with its own effect and visitor dispatch logic.
 * Characters do not participate in event resolution; their
 * {@link #accept(PlayEventVisitor)} implementation is a no-op.</p>
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Builder.class,  name = "BUILDER"),
        @JsonSubTypes.Type(value = Crafter.class,  name = "CRAFTER"),
        @JsonSubTypes.Type(value = Gatherer.class, name = "GATHERER"),
        @JsonSubTypes.Type(value = Hunter.class,   name = "HUNTER"),
        @JsonSubTypes.Type(value = Painter.class,  name = "PAINTER"),
        @JsonSubTypes.Type(value = Shaman.class,   name = "SHAMAN")
})
public abstract class Character extends Card {

    /**
     * Constructs a {@code Character} card with the given properties.
     *
     * @param id                 unique card identifier
     * @param trigger            the game phase that gates this card's instant effects
     * @param interactiveEffects interactive effects carried by this card
     * @param instantEffects     instant effects carried by this card
     * @param age                the age this card belongs to (1–3)
     * @param type               the character type
     */
    public Character(int id, GamePhaseEnum trigger,
                     List<CardEffectInteractive> interactiveEffects,
                     List<CardEffectInstant> instantEffects,
                     int age, CardTypeEnum type) {
        super(id, trigger, interactiveEffects, instantEffects, age, type);
    }

    /**
     * Places this character into the given village by calling
     * {@link Village#add(Character)}.
     *
     * @param v the village to add this character to; must not be {@code null}
     */
    public void dispatch(Village v) { v.add(this); }

    /**
     * Accepts a {@link DrawCardVisitor}, dispatching to
     * {@link DrawCardVisitor#visit(Character)}.
     *
     * @param visitor the visitor to dispatch to
     */
    @Override
    public void accept(DrawCardVisitor visitor) { visitor.visit(this); }

    /**
     * Accepts a {@link CanDrawVisitor}, dispatching to
     * {@link CanDrawVisitor#visit(Character)}.
     *
     * @param visitor the visitor to dispatch to
     */
    @Override
    public void accept(CanDrawVisitor visitor) { visitor.visit(this); }

    /**
     * Characters do not participate in event resolution; this method is a
     * no-op.
     *
     * @param visitor the event visitor (unused)
     */
    @Override
    public void accept(PlayEventVisitor visitor) {}


    /**
     * Accepts a {@link VillageVisitor}, dispatching to the overload that
     * matches this character's concrete type.
     *
     * @param visitor the visitor to dispatch to
     */
    public abstract void accept(VillageVisitor visitor);
}