package it.polimi.ingsw.enumerations;

import it.polimi.ingsw.model.action.Action;
import it.polimi.ingsw.model.interfaces.InteractiveEffectModifier;
import it.polimi.ingsw.model.player.Player;

public enum DrawCardEnum implements InteractiveEffectModifier {
    DOWN_DRAW((p, self) -> {return new Action(p, self.DOWN_DRAW);}),
    UP_DRAW((p, self) -> {return new Action(p, self.DOWN_DRAW);});

    private final InteractiveEffectModifier modifier;
    DrawCardEnum(InteractiveEffectModifier modifier){
        this.modifier = modifier;
    }
    @Override
    public Action apply(Player p,DrawCardEnum drawCard) {
        return modifier.apply(p, this);
    }
}
