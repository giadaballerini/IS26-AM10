package it.polimi.ingsw.model.entities.card.effects.interactive;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.enumerations.DrawCardEnum;
import it.polimi.ingsw.model.action.Action;
import it.polimi.ingsw.model.player.Player;

public class DrawCard extends CardEffectInteractive{
    public final DrawCardEnum drawCardType;


    @JsonCreator
    public DrawCard(@JsonProperty("drawCardType") DrawCardEnum drawCardType) {
        this.drawCardType = drawCardType;
    }

    @Override
    public Action apply(Player p){
        return drawCardType.apply(p,drawCardType);
    }

    @Override
    public void displayEffect(){
        System.out.printf("Pescata effettuata");
    }

    @Override
    public int getUpDraws(){
        return drawCardType.equals(DrawCardEnum.UP_DRAW) ? 1 : 0;
    }

    @Override
    public int getDownDraws(){
        return drawCardType.equals(DrawCardEnum.DOWN_DRAW) ? 1 : 0;
    }

}
