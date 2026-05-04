package it.polimi.ingsw.enumerations;

import it.polimi.ingsw.model.entities.card.effects.instant.DiscountFood;
import it.polimi.ingsw.model.interfaces.DiscountFoodModifier;
import it.polimi.ingsw.model.player.Player;

public enum DiscountFoodEnum implements DiscountFoodModifier {
    DISCOUNT_CAT((p, e) -> {
        switch (e.getCat())
            {
                case CardTypeEnum.PAINTER:
                    p.setDiscountPainter(true);
                    break;

                case CardTypeEnum.CRAFTER:
                    p.setDiscountCrafter(true);
                    break;

                case CardTypeEnum.GATHERER:
                    p.setDiscountGatherer(true);
                    break;
            }
    }){public boolean isOneTime(){return true;}},
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

