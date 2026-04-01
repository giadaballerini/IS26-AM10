package it.polimi.ingsw.enumerations;


import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.entities.card.effects.instant.GainFood;
import it.polimi.ingsw.model.entities.card.types.character.Crafter;
import it.polimi.ingsw.model.interfaces.GainFoodModifier;
import it.polimi.ingsw.model.interfaces.GainFoodVisitor;
import it.polimi.ingsw.model.player.Player;
import java.lang.Math;

public enum GainFoodEnum implements GainFoodModifier, GainFoodVisitor {
    FOOD_FOR_SET((p, e, c) -> {
        int newNSets = Integer.MAX_VALUE;
        int oldNSets = Integer.MAX_VALUE;
        for(CardTypeEnum type : CardTypeEnum.values()){
            if(type.isCharacter()) {
                newNSets = Math.min(newNSets, p.getNumType(type));
                if (type.equals(c.getType()))
                    oldNSets = Math.min(oldNSets, p.getNumType(type) - 1);
                else
                    oldNSets = Math.min(oldNSets, p.getNumType(type));
            }
        }
        if(newNSets > oldNSets)
            p.addFood(e.getFoodAmount());
    }),
    FOOD_FOR_CRAFTER((p, e, c) -> {}){
    @Override
        public void apply (Player p,GainFood gainFood, Card c) {
         c.accept(this,p,gainFood);
    }
    @Override
    public void visit(Crafter c, Player p, GainFood gainFood) {
        CrafterSymbolEnum symbol = c.getSymbol();

        int count = p.getNumSymbolsForCrafter(symbol);

        if(count != 0 && count % 2 == 0)
            p.addFood(gainFood.getFoodAmount());

    }
    },
    FOOD_FOR_HUNTER_HUNT((p, e, c ) -> {
        p.setHuntFlag(true);
    }){public boolean isOneTime(){return true;}},
    FOOD_FOR_ARTIST_PAINT((p, e, c) -> {
        p.addFood(p.getNumType(CardTypeEnum.PAINTER) * e.getFoodAmount());
    }),
    FOOD_FLAT((p, e, c) -> {
        p.addFood(e.getFoodAmount());
    }){ public boolean isOneTime(){return true;}};

    private final GainFoodModifier modifier;
    GainFoodEnum(GainFoodModifier modifier) {
        this.modifier = modifier;
    }

    @Override
    public void apply(Player p, GainFood effect, Card c) {
        modifier.apply(p, effect, c);
    }

    public boolean isOneTime(){return false;}
}
