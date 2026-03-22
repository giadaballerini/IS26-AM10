package it.polimi.ingsw.model.entities.card.effects.instant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.enumerations.GainStarsEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.player.Player;

public class GainStars extends CardEffectInstant{
    private final int starsAmount;
    private final GainStarsEnum gainStarsType;

    @JsonCreator
    public GainStars(@JsonProperty("starsAmount") int starsAmount,@JsonProperty("gainStarsType") GainStarsEnum gainStarsType) {
        this.starsAmount = starsAmount;
        this.gainStarsType = gainStarsType;
    }

    @Override
    public void apply(Player p){
        gainStarsType.apply(p, this);
    }


    @Override
    public void displayEffect(){
        System.out.printf("");
    }

    public int getStarsAmount(){
        return starsAmount;
    }

    @Override
    public boolean isOneTime(){return gainStarsType.isOneTime();}

}
