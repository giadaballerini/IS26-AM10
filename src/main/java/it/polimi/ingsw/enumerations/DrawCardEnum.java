package it.polimi.ingsw.enumerations;

import it.polimi.ingsw.model.action.Action;
import it.polimi.ingsw.model.interfaces.InteractiveEffectModifier;
import it.polimi.ingsw.model.player.Player;

public enum DrawCardEnum implements InteractiveEffectModifier {
    DOWN_DRAW(Action::new),
    UP_DRAW(Action::new);

    private final InteractiveEffectModifier modifier;
    DrawCardEnum(InteractiveEffectModifier modifier){
        this.modifier = modifier;
    }
    @Override
    public Action apply(Player p,DrawCardEnum drawCard) {
        return modifier.apply(p, this);
    }
}
