package it.polimi.ingsw.model.entities.card.effects.instant;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.enumerations.LossIfBrokeEnum;
import it.polimi.ingsw.model.player.Player;

public class LossIfBroke extends CardEffectInstant{
    private final int ppCost;
    private final int foodCost;
    private final LossIfBrokeEnum lossIfBrokeType;

    public LossIfBroke(@JsonProperty("ppCost")int ppCost, @JsonProperty("foodCost") int foodCost, @JsonProperty("lossIfBrokeType")LossIfBrokeEnum lossIfBrokeType) {
        this.ppCost = ppCost;
        this.foodCost = foodCost;
        this.lossIfBrokeType = lossIfBrokeType;
    }

    public void apply(Player p, GamePhaseEnum gamePhase){
        lossIfBrokeType.apply(p, this, gamePhase);
    }

    @Override
    public void canApply(GamePhaseEnum trigger, Player p, GamePhaseEnum phase){

    }
}
