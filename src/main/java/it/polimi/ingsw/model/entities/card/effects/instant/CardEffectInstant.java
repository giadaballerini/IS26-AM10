package it.polimi.ingsw.model.entities.card.effects.instant;

import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.player.Player;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type" // Questo deve corrispondere al "type" dentro l'effetto nel JSON
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DiscountFood.class, name = "DISCOUNT_FOOD"),
        @JsonSubTypes.Type(value = GainFood.class, name = "GAIN_FOOD"),
        @JsonSubTypes.Type(value = GainPP.class, name = "GAIN_PP"),
        @JsonSubTypes.Type(value = GainStars.class, name = "GAIN_STARS"),
        @JsonSubTypes.Type(value = ProtectPP.class, name = "PROTECT_PP")
})
public abstract class  CardEffectInstant {

    public void apply(Player p) {}

    public void apply(Player p, Card c) {}


    public boolean canApply(GamePhaseEnum trigger, GamePhaseEnum currPhase){
        return trigger == currPhase;
    }
    public void displayEffect(){

    }
    public int getPpAmount(){
        return 0;
    }

    public boolean isOneTime(){return false;}
}
