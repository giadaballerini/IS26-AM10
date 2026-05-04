package it.polimi.ingsw.model.entities.card.effects.instant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.GainPPEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.player.Player;

public class GainPP extends CardEffectInstant{
    private final int ppAmount;
    private final CardTypeEnum cat;
    private final GainPPEnum gainPpType;

    @JsonCreator
    public GainPP(@JsonProperty("category") CardTypeEnum cat,@JsonProperty("ppAmount") int ppAmount,@JsonProperty("gainPpType") GainPPEnum gainPpType) {
        this.ppAmount = ppAmount;
        this.cat = cat;
        this.gainPpType = gainPpType;
    }
    @Override
    public void apply(Player p,Card c){
        gainPpType.apply(p, this, c);
    }


    @Override
    public void displayEffect(){
        System.out.println("\nAggiunti " + ppAmount + "PP");
    }

    public int getPpAmount() {
        return ppAmount;
    }

    public CardTypeEnum getCat() {return cat;}

    @Override
    public boolean isOneTime(){return gainPpType.isOneTime();}
}
