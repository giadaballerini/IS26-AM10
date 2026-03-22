package it.polimi.ingsw.model.entities.card.effects.instant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.enumerations.ProtectPPEnum;
import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.player.Player;

public class ProtectPP extends CardEffectInstant{
    private final ProtectPPEnum protectPpType;

    @JsonCreator
    public ProtectPP(@JsonProperty("protectPpType") ProtectPPEnum protectPpType) {
        this.protectPpType = protectPpType;
    }

    @Override
    public void apply(Player p){
        protectPpType.apply(p);
    }


    @Override
    public void displayEffect(){
        System.out.printf("");
    }

    @Override
    public boolean isOneTime(){return protectPpType.isOneTime();}
}
