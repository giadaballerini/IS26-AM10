package it.polimi.ingsw.model.entities.card.types.event;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.entities.card.effects.instant.CardEffectInstant;
import it.polimi.ingsw.model.entities.card.effects.interactive.CardEffectInteractive;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.visitors.CanDrawVisitor;
import it.polimi.ingsw.visitors.DrawCardVisitor;
import it.polimi.ingsw.visitors.PlayEventVisitor;
import it.polimi.ingsw.visitors.VillageVisitor;

import java.util.List;

/**
 * Abstract base class for event cards.
 *
 * <p>Event cards are not drawn into a player's hand; instead, they are resolved
 * collectively at the end of a round or age, affecting all players at once.
 * The four concrete event types are {@link Feast}, {@link Hunt},
 * {@link Ritual}, and {@link StonePainting}, each implementing
 * {@link #execEvent(List, GamePhaseEnum)} with their own resolution logic.</p>
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Feast.class,         name = "FEAST"),
        @JsonSubTypes.Type(value = Hunt.class,          name = "HUNT"),
        @JsonSubTypes.Type(value = Ritual.class,        name = "RITUAL"),
        @JsonSubTypes.Type(value = StonePainting.class, name = "STONE_PAINTING")
})
public abstract class Event extends Card {

    /**
     * Constructs an {@code Event} card with the given properties.
     *
     * @param id                 unique card identifier
     * @param trigger            the game phase during which this event is resolved
     * @param interactiveEffects interactive effects carried by this card
     * @param instantEffects     instant effects carried by this card
     * @param age                the age this card belongs to (1–3)
     * @param type               the event type
     */
    public Event(int id, GamePhaseEnum trigger,
                 List<CardEffectInteractive> interactiveEffects,
                 List<CardEffectInstant> instantEffects,
                 int age, CardTypeEnum type) {
        super(id, trigger, interactiveEffects, instantEffects, age, type);
    }

    /**
     * Resolves this event for all players in the match.
     *
     * @param players the list of all players; must not be {@code null}
     * @param phase   the current game phase at the time of resolution
     */
    public abstract void execEvent(List<Player> players, GamePhaseEnum phase);


    /**
     * Accepts a {@link PlayEventVisitor}, dispatching to
     * {@link PlayEventVisitor#visit(Event)}.
     *
     * @param visitor the visitor to dispatch to
     */
    @Override
    public void accept(PlayEventVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Accepts a {@link DrawCardVisitor}, dispatching to
     * {@link DrawCardVisitor#visit(Event)}.
     *
     * @param visitor the visitor to dispatch to
     */
    @Override
    public void accept(DrawCardVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Accepts a {@link CanDrawVisitor}, dispatching to
     * {@link CanDrawVisitor#visit(Event)}.
     *
     * @param visitor the visitor to dispatch to
     */
    @Override
    public void accept(CanDrawVisitor visitor) { visitor.visit(this); }

    @Override
    public void accept(VillageVisitor visitor){visitor.visit(this);}
}