package it.polimi.ingsw.model.entities.card.effects.interactive;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.player.Player;
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type" // Il campo "type" che hai messo dentro l'oggetto effect nel JSON
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DrawCard.class, name = "DRAW_CARD")
})
public abstract class CardEffectInteractive{
    public void apply(Player p){

    }

    public void canApply(Card c, Player p, GamePhaseEnum phase){

    }

    public void displayEffect(){
        System.out.printf("");
    }
}
