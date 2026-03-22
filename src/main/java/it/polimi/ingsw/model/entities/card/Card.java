package it.polimi.ingsw.model.entities.card;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.entities.card.effects.instant.CardEffectInstant;
import it.polimi.ingsw.model.entities.card.effects.instant.GainFood;
import it.polimi.ingsw.model.entities.card.effects.instant.GainPP;
import it.polimi.ingsw.model.entities.card.effects.interactive.CardEffectInteractive;
import it.polimi.ingsw.model.interfaces.GainFoodVisitor;
import it.polimi.ingsw.model.interfaces.GainPPVisitor;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.entities.card.types.event.*;
import it.polimi.ingsw.model.entities.card.types.character.*;
import it.polimi.ingsw.model.entities.card.types.building.Building;
import java.util.List;


@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes({
        // Sottoclasse diretta concreta
        @JsonSubTypes.Type(value = Building.class, name = "BUILDING"),

        // Sottoclassi di Event
        @JsonSubTypes.Type(value = Feast.class, name = "FEAST"),
        @JsonSubTypes.Type(value = Hunt.class, name = "HUNT"),
        @JsonSubTypes.Type(value = Ritual.class, name = "RITUAL"),
        @JsonSubTypes.Type(value = StonePainting.class, name = "STONE_PAINTING"),

        // Sottoclassi di Character
        @JsonSubTypes.Type(value = Builder.class, name = "BUILDER"),
        @JsonSubTypes.Type(value = Crafter.class, name = "CRAFTER"),
        @JsonSubTypes.Type(value = Gatherer.class, name = "GATHERER"),
        @JsonSubTypes.Type(value = Hunter.class, name = "HUNTER"),
        @JsonSubTypes.Type(value = Painter.class, name = "PAINTER"),
        @JsonSubTypes.Type(value = Shaman.class, name = "SHAMAN")
})


public abstract class Card {
    private int id;
    protected GamePhaseEnum trigger;
    private List <CardEffectInteractive> interactiveEffects;
    private List<CardEffectInstant> instantEffects;
    private int age;
    private CardTypeEnum type;

    public Card(int id, GamePhaseEnum trigger, List <CardEffectInteractive> interactiveEffects,List <CardEffectInstant> instantEffects, int age, CardTypeEnum type) {
        this.id = id;
        this.trigger = trigger;
        this.interactiveEffects = interactiveEffects;
        this.instantEffects = instantEffects;
        this.age = age;
        this.type = type;
    }

    public int getAge(){
        return this.age;
    }

    public List<CardEffectInteractive> getInteractiveEffects(){
        return this.interactiveEffects;
    }

    public List<CardEffectInstant> getInstantEffects(){
        return this.instantEffects;
    }

    public void execInstantEffect(Player p, GamePhaseEnum currPhase) {
        for (CardEffectInstant e : instantEffects) {
            if (e.canApply(trigger, currPhase)) {
                e.apply(p, this);
                if(e.isOneTime())
                    instantEffects.remove(e);
            }
        }
    }
/*
    - il controllo e.canApply(trigger, currPhase); viene eseguito per tutti
    gli effetti ritornando sempre lo stesso risultato (inutile), il trigger
    dovrebbe essere relativo all'effetto e non alla carta, ci potrebbero
    essere più effetti di una stessa carta che hanno trigger diversi =>
    modifica json(?)
    - effetti come FOOD_FLAT dovrebbero tenere un flag per indicare che
     l'effetto è già stato eseguito, essendo una tantum
    - dato che esecuzione degli effetti è automatica e non dipende
    da input del player dovremmo togliere le exception e rendere canApply boolean
*/

    public int getId(){
        return this.id;
    }
    public void execInteractiveEffect(Player p, GamePhaseEnum gamePhase){
    }

    public CardTypeEnum getType(){
        return this.type;
    }

    public abstract void accept(GainFoodVisitor visitor, Player p, GainFood effect);
    public abstract void accept(GainPPVisitor visitor, Player p, GainPP effect);
}
