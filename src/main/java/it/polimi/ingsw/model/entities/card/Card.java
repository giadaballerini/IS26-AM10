package it.polimi.ingsw.model.entities.card;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.action.Action;
import it.polimi.ingsw.model.entities.card.effects.instant.CardEffectInstant;
import it.polimi.ingsw.model.entities.card.effects.interactive.CardEffectInteractive;
import it.polimi.ingsw.model.entities.card.types.building.Building;
import it.polimi.ingsw.model.entities.card.types.character.*;
import it.polimi.ingsw.model.entities.card.types.event.Feast;
import it.polimi.ingsw.model.entities.card.types.event.Hunt;
import it.polimi.ingsw.model.entities.card.types.event.Ritual;
import it.polimi.ingsw.model.entities.card.types.event.StonePainting;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.network.dto.CardDTO;
import it.polimi.ingsw.visitors.CanDrawVisitor;
import it.polimi.ingsw.visitors.DrawCardVisitor;
import it.polimi.ingsw.visitors.PlayEventVisitor;
import it.polimi.ingsw.visitors.VillageVisitor;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Abstract base class for all cards in the game.
 *
 * <p>A card carries a set of {@link CardEffectInstant instant effects} applied
 * when the card is drawn or a phase trigger fires, and optionally a set of
 * {@link CardEffectInteractive interactive effects} that generate pending
 * {@link Action} instances to be resolved by the owner. Each card belongs to
 * an age (1–3), has a {@link CardTypeEnum type}, and is associated with a
 * {@link GamePhaseEnum trigger} that gates when its instant effects may fire.</p>
 *
 * <p>Concrete subclasses cover three categories: characters ({@link Builder},
 * {@link Crafter}, {@link Gatherer}, {@link Hunter}, {@link Painter},
 * {@link Shaman}), events ({@link Feast}, {@link Hunt}, {@link Ritual},
 * {@link StonePainting}), and buildings ({@link Building}). Jackson uses the
 * {@code type} property to deserialize the correct subclass.</p>
 *
 * <p>The Visitor pattern is used for type-specific dispatch: each concrete
 * subclass implements the {@code accept} overloads to call the matching
 * {@code visit} method on the visitor.</p>
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Building.class,      name = "BUILDING"),
        @JsonSubTypes.Type(value = Feast.class,         name = "FEAST"),
        @JsonSubTypes.Type(value = Hunt.class,          name = "HUNT"),
        @JsonSubTypes.Type(value = Ritual.class,        name = "RITUAL"),
        @JsonSubTypes.Type(value = StonePainting.class, name = "STONE_PAINTING"),
        @JsonSubTypes.Type(value = Builder.class,       name = "BUILDER"),
        @JsonSubTypes.Type(value = Crafter.class,       name = "CRAFTER"),
        @JsonSubTypes.Type(value = Gatherer.class,      name = "GATHERER"),
        @JsonSubTypes.Type(value = Hunter.class,        name = "HUNTER"),
        @JsonSubTypes.Type(value = Painter.class,       name = "PAINTER"),
        @JsonSubTypes.Type(value = Shaman.class,        name = "SHAMAN")
})
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public abstract class Card {

    /** Unique identifier of this card. */
    private final int id;

    /**
     * The game phase that must be active for this card's instant effects to
     * fire, or that determines when the card's special behaviour is relevant.
     */
    protected GamePhaseEnum trigger;

    /** Interactive effects that generate pending actions for the card's owner. */
    protected List<CardEffectInteractive> interactiveEffects;

    /** Instant effects applied when this card is drawn or its trigger phase fires. */
    protected List<CardEffectInstant> instantEffects;

    /** The age (1–3) this card belongs to. */
    private final int age;

    /** The category of this card (character type, building, or event variant). */
    private final CardTypeEnum type;

    /**
     * Constructs a {@code Card} with the given properties.
     *
     * @param id                 unique card identifier
     * @param trigger            the game phase that gates this card's instant effects
     * @param interactiveEffects interactive effects carried by this card
     * @param instantEffects     instant effects carried by this card
     * @param age                the age this card belongs to (1–3)
     * @param type               the category of this card
     */
    public Card(int id, GamePhaseEnum trigger, List<CardEffectInteractive> interactiveEffects,
                List<CardEffectInstant> instantEffects, int age, CardTypeEnum type) {
        this.id = id;
        this.trigger = trigger;
        this.interactiveEffects = interactiveEffects;
        this.instantEffects = instantEffects;
        this.age = age;
        this.type = type;
    }

    /**
     * Returns the age this card belongs to.
     *
     * @return age (1–3)
     */
    public int getAge() {
        return this.age;
    }

    /**
     * Returns the interactive effects carried by this card.
     *
     * @return the interactive effect list; never {@code null}
     */
    public List<CardEffectInteractive> getInteractiveEffects() {
        return this.interactiveEffects;
    }

    /**
     * Returns the instant effects carried by this card.
     *
     * @return the instant effect list; never {@code null}
     */
    public List<CardEffectInstant> getInstantEffects() {
        return this.instantEffects;
    }

    /**
     * Returns whether this effect may fire given the card's trigger phase and
     * the current game phase. The default implementation requires an exact
     * match between the two phases.
     *
     * @param trigger   the phase associated with the card carrying this effect
     * @param currPhase the current game phase
     * @return {@code true} if the effect may fire
     */
    public boolean canApply(GamePhaseEnum trigger, GamePhaseEnum currPhase) {
        return trigger == currPhase;
    }

    /**
     * Applies all eligible instant effects of this card to the given player
     * for the current game phase, removing one-time effects after they fire.
     *
     * <p>An effect fires only if {@link Card#canApply(GamePhaseEnum, GamePhaseEnum)}
     * returns {@code true} for this card's trigger and the current phase.</p>
     *
     * @param p         the player to apply the effects to
     * @param currPhase the current game phase
     */
    public void execInstantEffect(Player p, GamePhaseEnum currPhase) {
        Iterator<CardEffectInstant> it = instantEffects.iterator();
        while (it.hasNext()) {
            CardEffectInstant e = it.next();
            if (canApply(trigger, currPhase)) {
                e.apply(p, this);
                if (e.isOneTime())
                    it.remove();
            }
        }
    }

    /**
     * Returns the game phase that gates this card's instant effects.
     *
     * @return the trigger phase; never {@code null}
     */
    public GamePhaseEnum getTrigger() {
        return trigger;
    }

    /**
     * Returns the unique identifier of this card.
     *
     * @return card ID
     */
    public int getId() {
        return this.id;
    }

    /**
     * Applies the interactive effects of this card to the given player,
     * returning any resulting pending actions.
     *
     * <p>The default implementation returns an empty list; subclasses that
     * carry interactive effects should override this method.</p>
     *
     * @param p the player to apply the effects to
     * @return list of pending actions generated; never {@code null}
     */
    public List<Action> execInteractiveEffect(Player p) {
        return Collections.emptyList();
    }

    /**
     * Returns the category of this card.
     *
     * @return the {@link CardTypeEnum} value; never {@code null}
     */
    public CardTypeEnum getType() {
        return this.type;
    }


    /**
     * Accepts a {@link PlayEventVisitor}, dispatching to the overload that
     * matches this card's concrete type.
     *
     * @param visitor the visitor to dispatch to
     */
    public abstract void accept(PlayEventVisitor visitor);

    /**
     * Accepts a {@link DrawCardVisitor}, dispatching to the overload that
     * matches this card's concrete type.
     *
     * @param visitor the visitor to dispatch to
     */
    public abstract void accept(DrawCardVisitor visitor);

    /**
     * Accepts a {@link CanDrawVisitor}, dispatching to the overload that
     * matches this card's concrete type.
     *
     * @param visitor the visitor to dispatch to
     */
    public abstract void accept(CanDrawVisitor visitor);

    /**
     * Accepts a {@link VillageVisitor}, dispatching to the overload that
     * matches this card's concrete type.
     * @param visitor the visitor to dispatch to
     */
    public abstract void accept(VillageVisitor visitor);

    /**
     * Converts this card to a {@link CardDTO} for network transfer.
     *
     * @return a DTO carrying the card's ID, age, and type; never {@code null}
     */
    public CardDTO toDTO() {
        return new CardDTO(id, age, type);
    }
}