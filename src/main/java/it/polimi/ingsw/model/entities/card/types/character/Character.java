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

import java.util.List;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type" // Il campo "type" che si trova dentro l'oggetto effect nel JSON
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Builder.class, name = "BUILDER"),
        @JsonSubTypes.Type(value = Crafter.class, name = "CRAFTER"),
        @JsonSubTypes.Type(value = Gatherer.class, name = "GATHERER"),
        @JsonSubTypes.Type(value = Hunter.class, name = "HUNTER"),
        @JsonSubTypes.Type(value = Painter.class, name = "PAINTER"),
        @JsonSubTypes.Type(value = Shaman.class, name = "SHAMAN")

})


public abstract class Character extends Card {

    public Character(int id,GamePhaseEnum trigger, List<CardEffectInteractive> interactiveEffects, List<CardEffectInstant> instantEffects, int age, CardTypeEnum type) {
        super(id, trigger, interactiveEffects, instantEffects, age, type);
    }

    public abstract void dispatch(Village v);

    public void accept(DrawCardVisitor visitor){visitor.visit(this);}
    public void accept(CanDrawVisitor visitor){visitor.visit(this);}
    public void accept(PlayEventVisitor visitor){}
}
