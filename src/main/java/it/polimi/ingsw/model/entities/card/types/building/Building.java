package it.polimi.ingsw.model.entities.card.types.building;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.entities.card.effects.instant.CardEffectInstant;
import it.polimi.ingsw.model.entities.card.effects.instant.GainFood;
import it.polimi.ingsw.model.entities.card.effects.instant.GainPP;
import it.polimi.ingsw.model.entities.card.effects.interactive.CardEffectInteractive;
import it.polimi.ingsw.model.interfaces.GainFoodVisitor;
import it.polimi.ingsw.model.interfaces.GainPPVisitor;
import it.polimi.ingsw.model.player.Player;

import java.util.List;

public class Building extends Card {
    private final int ppValue;
    private final int foodCost;

    @JsonCreator
    public Building(@JsonProperty("id") int id, @JsonProperty("trigger") GamePhaseEnum trigger,
                    @JsonProperty("interactiveEffects") List<CardEffectInteractive> interactiveEffects,
                    @JsonProperty("instantEffects") List<CardEffectInstant> instantEffects,
                    @JsonProperty("age") int age,@JsonProperty("ppValue") int ppValue,
                    @JsonProperty("foodCost") int foodCost,
                    @JsonProperty("type") CardTypeEnum type) {
        super(id, trigger, interactiveEffects, instantEffects, age, type);
        this.ppValue = ppValue;
        this.foodCost = foodCost;
    }

    public int getPpValue() {
        return ppValue;
    }

    public int getFoodCost() {
        return foodCost;
    }

    public void accept(GainFoodVisitor visitor, Player p, GainFood e){
        visitor.visit(this, p, e);
    }

    public void accept (GainPPVisitor visitor, Player p, GainPP e){
        visitor.visit(this, p, e);
    }
}
