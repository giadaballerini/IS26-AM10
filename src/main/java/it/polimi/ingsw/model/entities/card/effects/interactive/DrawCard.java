package it.polimi.ingsw.model.entities.card.effects.interactive;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.DrawCardEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.player.Player;

public class DrawCard extends CardEffectInteractive{
    public final DrawCardEnum drawCardType;


    @JsonCreator
    public DrawCard(@JsonProperty("drawCardType") DrawCardEnum drawCardType,@JsonProperty("upDraw") boolean upDraw) {
        this.drawCardType = drawCardType;
    }

    @Override
    public void apply(Player p, GamePhaseEnum gamePhase){
        drawCardType.apply(p, gamePhase, drawCardType);
    }

    @Override
    public void canApply(Card c, Player p, GamePhaseEnum phase){

    }

    @Override
    public void displayEffect(){
        System.out.printf("");
    }

}
