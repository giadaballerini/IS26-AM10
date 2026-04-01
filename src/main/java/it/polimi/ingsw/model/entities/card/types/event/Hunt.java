package it.polimi.ingsw.model.entities.card.types.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.entities.card.effects.instant.CardEffectInstant;
import it.polimi.ingsw.model.entities.card.effects.interactive.CardEffectInteractive;
import it.polimi.ingsw.model.player.Player;

import java.util.List;

public class Hunt extends Event {

    private final int ppGain;
    private final int foodGain;


    @JsonCreator
    public Hunt(@JsonProperty("id") int id, @JsonProperty("trigger") GamePhaseEnum trigger,
                @JsonProperty("interactiveEffects") List<CardEffectInteractive> interactiveEffects,
                @JsonProperty("instantEffects") List<CardEffectInstant> instantEffects,
                @JsonProperty("age") int age, @JsonProperty("ppGain") int ppGain, @JsonProperty("foodGain") int foodGain,
                @JsonProperty("type") CardTypeEnum type) {
        super(id, trigger, interactiveEffects, instantEffects, age, type);
        this.ppGain = ppGain;
        this.foodGain = foodGain;
    }

    public void execEvent(List<Player> players, GamePhaseEnum phase){
        if (phase == this.trigger){
            for (Player player : players){
                CardTypeEnum t = CardTypeEnum.HUNTER;
                int tmp = player.getNumType(t);
                player.addFood(tmp * foodGain);
                player.addPP(tmp * ppGain);
                if(player.hasHuntFlag()){
                    player.addFood(tmp);
                    player.addPP(tmp);
                }
            }
        }
    }
}
