package it.polimi.ingsw.model.entities.card.effects.instant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import it.polimi.ingsw.enumerations.GainFoodEnum;
import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.player.Player;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
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
    public void apply(Player p){gainFoodType.apply(p, this);}

    @Override
    public void displayEffect(){
        System.out.println("\nAggiunto " + foodAmount + " di cibo");
    }


    @Override
    public int getFoodAmount(){
        return foodAmount;
    }

    @Override
    public boolean isOneTime(){return gainFoodType.isOneTime();}

    @Override
    public boolean isFoodEffect(){return true;}

}
