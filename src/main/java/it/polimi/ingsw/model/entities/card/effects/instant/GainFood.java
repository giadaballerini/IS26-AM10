package it.polimi.ingsw.model.entities.card.effects.instant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.enumerations.GainFoodEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.player.Player;

public class GainFood extends CardEffectInstant{
    private final int foodAmount;
    private final GainFoodEnum gainFoodType;
    @JsonCreator
    public GainFood(@JsonProperty("foodAmount") int foodAmount,@JsonProperty("gainFoodType") GainFoodEnum gainFoodType) {
        this.foodAmount = foodAmount;
        this.gainFoodType = gainFoodType;
    }

    @Override
    public void apply(Player p, Card c){
        gainFoodType.apply(p, this, c);
    }


    @Override
    public void displayEffect(){
        System.out.printf("");
    }

    public int getFoodAmount(){
        return foodAmount;
    }

    @Override
    public boolean isOneTime(){return gainFoodType.isOneTime();}
}
