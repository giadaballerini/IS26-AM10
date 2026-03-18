package it.polimi.ingsw.enumerations;

import it.polimi.ingsw.model.entities.card.effects.instant.DiscountFood;
import it.polimi.ingsw.model.interfaces.DiscountFoodModifier;
import it.polimi.ingsw.model.player.Player;

public enum DiscountFoodEnum implements DiscountFoodModifier {
    DISCOUNT_CAT((p, e, g) -> {
        p.addFoodDiscount(p.getNumType(e.getCat()));
    }),
    DISCOUNT_FLAT((p, e, g) -> {
        p.addFoodDiscount(3);
    });


    private final DiscountFoodModifier modifier;
    DiscountFoodEnum(DiscountFoodModifier modifier) {
        this.modifier = modifier;
    }
    @Override
    public void apply(Player p, DiscountFood effect, GamePhaseEnum g) {
        modifier.apply(p, effect, g);
    }
}
