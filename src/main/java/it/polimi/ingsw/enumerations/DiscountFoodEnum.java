package it.polimi.ingsw.enumerations;

import it.polimi.ingsw.model.entities.card.effects.instant.DiscountFood;
import it.polimi.ingsw.model.interfaces.DiscountFoodModifier;
import it.polimi.ingsw.model.player.Player;

public enum DiscountFoodEnum implements DiscountFoodModifier {
    DISCOUNT_CAT((p, e) -> {
        int numCat = p.getNumType(e.getCat());
        int foodDisc = 0;
        if(numCat % 2 == 0){
            foodDisc = e.getFoodAmount();
        }
        p.addFoodDiscount(foodDisc);
    }),
    DISCOUNT_FLAT((p, e) -> {
        p.addFoodDiscount(e.getFoodAmount());
    }){
        public boolean isOneTime(){return true;}
    },
    DISCOUNT_FOR_BUILDING((p, e) -> {
        p.addTotBuildDiscount(e.getFoodAmount());
    }){public boolean isOneTime(){return true;}};




    private final DiscountFoodModifier modifier;
    DiscountFoodEnum(DiscountFoodModifier modifier) {
        this.modifier = modifier;
    }

    @Override
    public void apply(Player p, DiscountFood effect) {
        modifier.apply(p, effect);
    }

    public boolean isOneTime(){return false;}
}

