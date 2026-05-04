package it.polimi.ingsw.model.entities.card.types.event;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
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

import java.util.List;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type" // Il campo "type" che si trova dentro l'oggetto effect nel JSON
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Feast.class, name = "FEAST"),
        @JsonSubTypes.Type(value = Hunt.class, name = "HUNT"),
        @JsonSubTypes.Type(value = Ritual.class, name = "RITUAL"),
        @JsonSubTypes.Type(value = StonePainting.class, name = "STONE_PAINTING")

})


public abstract class Event extends Card {
    public Event(int id, GamePhaseEnum trigger, List<CardEffectInteractive> interactiveEffects, List<CardEffectInstant> instantEffects, int age, CardTypeEnum type) {
        super(id, trigger, interactiveEffects, instantEffects, age,  type);
    }

    public abstract void execEvent(List<Player> players, GamePhaseEnum phase);
    public void accept(GainFoodVisitor visitor, Player p, GainFood e){
        visitor.visit(this, p, e);
    }

    public void accept (GainPPVisitor visitor, Player p, GainPP e){
        visitor.visit(this, p, e);
    }
    public void accept(PlayEventVisitor visitor){
        visitor.visit(this);
    }
    public void accept(DrawCardVisitor visitor){
        visitor.visit(this);
    }
    public void accept(CanDrawVisitor visitor){visitor.visit(this);}

}
