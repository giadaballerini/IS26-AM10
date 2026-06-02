package it.polimi.ingsw.model.entities.card.effects.interactive;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.polimi.ingsw.model.action.Action;
import it.polimi.ingsw.model.player.Player;
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type" // Il campo "type" che si trova dentro l'oggetto effect nel JSON
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DrawCard.class, name = "DRAW_CARD")
})
public abstract class CardEffectInteractive{
    public abstract Action apply(Player p);
    public abstract void displayEffect();

    public int getUpDraws(){
        return 0;
    }

    public int getDownDraws(){
        return 0;
    }

}
