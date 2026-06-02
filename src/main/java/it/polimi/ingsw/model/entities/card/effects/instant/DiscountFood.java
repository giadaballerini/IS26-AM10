package it.polimi.ingsw.model.entities.card.effects.instant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.DiscountFoodEnum;
import it.polimi.ingsw.model.player.Player;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class DiscountFood extends CardEffectInstant{
    private final CardTypeEnum cat;
    private final int foodAmount;
    private final DiscountFoodEnum discountFoodType;

    @JsonCreator
    public DiscountFood(@JsonProperty("category") CardTypeEnum cat,@JsonProperty("foodAmount") int foodAmount,@JsonProperty("discountFoodType") DiscountFoodEnum discountFoodType) {
        this.cat = cat;
        this.foodAmount = foodAmount;
        this.discountFoodType = discountFoodType;
    }

    @Override
    public void apply(Player p){
        discountFoodType.apply(p, this);
    }


    @Override
    public void displayEffect(){
        System.out.println("\nAggiunto discount di " + foodAmount);
    }


    public CardTypeEnum getCat() {
        return cat;
    }

    @Override
    public int getFoodAmount() {
        return foodAmount;
    }

    @Override
    public boolean isOneTime(){return discountFoodType.isOneTime();}
}
