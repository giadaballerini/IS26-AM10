package it.polimi.ingsw.enumerations;

import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.entities.card.effects.instant.GainFood;
import it.polimi.ingsw.model.entities.card.effects.instant.GainPP;
import it.polimi.ingsw.model.entities.card.types.character.Builder;
import it.polimi.ingsw.model.entities.card.types.character.Crafter;
import it.polimi.ingsw.model.interfaces.GainPPModifier;
import it.polimi.ingsw.model.interfaces.GainPPVisitor;
import it.polimi.ingsw.model.player.Player;

public enum GainPPEnum implements GainPPModifier, GainPPVisitor {
    PP_FOR_CAT((p, e,c) -> {
        p.addPP(e.getPpAmount() * p.getNumType(e.getCat()));
    }),
    PP_FOR_SET((p, e, c) -> {
        int newNSets = Integer.MAX_VALUE;
        int oldNSets = Integer.MAX_VALUE;
        for(CardTypeEnum type : CardTypeEnum.values()){
            newNSets = Math.min(newNSets, p.getNumType(type));
            if(type.equals(c.getType()))
                oldNSets = Math.min(oldNSets, p.getNumType(type)-1);
            else
                oldNSets = Math.min(oldNSets, p.getNumType(type));
        }
        if(newNSets > oldNSets)
            p.addFood(e.getPpAmount());
    }),
    PP_FLAT((p, e, c) -> {
        p.addPP(e.getPpAmount());
    }){ public boolean isOneTime(){return true;}},
    DOUBLE_PP_SHAMAN((p, e, c) -> {
        p.addDouble();
    }),
    DOUBLE_BUILDER((p, e, c) -> {
        p.addPP(p.getBuilderPoints());
    }),
    /*PP_CRAFTER((p, e,  c) -> {}){
        @Override
        public void apply (Player p, GainPP gainPP, Card c) {
            c.accept(this,p,gainPP);
        }
        @Override
        public void visit(Crafter c, Player p, GainPP gainPP) {
            int numCrafters = p.getNumType(c.getType());
            int numSymbols = p.getTotSymbolsForCrafter();
            p.addPP(numCrafters * numSymbols);

        }

    }*/;

    private final GainPPModifier modifier;
    GainPPEnum(GainPPModifier modifier) {
        this.modifier = modifier;
    }


    @Override
    public void apply(Player p, GainPP effect, Card c) {
        modifier.apply(p, effect, c);
    }

    public boolean isOneTime(){return false;};
}
