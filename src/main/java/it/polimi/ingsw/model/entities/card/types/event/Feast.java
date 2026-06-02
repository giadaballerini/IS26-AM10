package it.polimi.ingsw.model.entities.card.types.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.entities.card.effects.instant.CardEffectInstant;
import it.polimi.ingsw.model.entities.card.effects.interactive.CardEffectInteractive;
import it.polimi.ingsw.model.player.Player;

import java.util.List;

public class Feast extends Event {

    private final int foodCost;
    private final int ppCost;

    @JsonCreator
    public Feast( @JsonProperty("id") int id, @JsonProperty("trigger") GamePhaseEnum trigger,@JsonProperty("interactiveEffects") List<CardEffectInteractive> interactiveEffects,
                 @JsonProperty("instantEffects") List<CardEffectInstant> instantEffects,
                 @JsonProperty("age") int age,@JsonProperty("foodCost") int foodCost,@JsonProperty("ppCost") int ppCost,
                 @JsonProperty("type") CardTypeEnum type) {
        super(id, trigger, interactiveEffects, instantEffects, age, type);
        this.foodCost = foodCost;
        this.ppCost = ppCost;
    }

    public void execEvent(List<Player> players, GamePhaseEnum phase){
        for(Player playerRef : players){
            int discount = playerRef.calculateFeastDiscount();
            int totalCost = playerRef.getNumCharacters() - discount;
            for(int k = 0; k < totalCost; k++){
                if(playerRef.getNFood() > 0){
                    playerRef.addFood(-this.foodCost);
                } else {
                    playerRef.addPP(-this.ppCost);
                }
            }
        }
    }

}
