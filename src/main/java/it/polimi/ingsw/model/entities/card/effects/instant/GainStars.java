package it.polimi.ingsw.model.entities.card.effects.instant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import it.polimi.ingsw.enumerations.GainStarsEnum;

import it.polimi.ingsw.model.player.Player;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class GainStars extends CardEffectInstant{
    private final int starsAmount;
    private final GainStarsEnum gainStarsType;

    @JsonCreator
    public GainStars(@JsonProperty("starsAmount") int starsAmount,@JsonProperty("gainStarsEnum") GainStarsEnum gainStarsType) {
        this.starsAmount = starsAmount;
        this.gainStarsType = gainStarsType;
    }

    @Override
    public void apply(Player p){
        gainStarsType.apply(p, this);
    }


    @Override
    public void displayEffect(){
        System.out.println("\nAggiunte " + starsAmount + " stelle");
    }

    public int getStarsAmount(){
        return starsAmount;
    }

    @Override
    public boolean isOneTime(){return gainStarsType.isOneTime();}

}
