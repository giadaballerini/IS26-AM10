package it.polimi.ingsw.model.entities.card.types.building;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.action.Action;
import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.entities.card.effects.instant.CardEffectInstant;
import it.polimi.ingsw.model.entities.card.effects.instant.GainFood;
import it.polimi.ingsw.model.entities.card.effects.instant.GainPP;
import it.polimi.ingsw.model.entities.card.effects.interactive.CardEffectInteractive;
import it.polimi.ingsw.model.interfaces.GainFoodVisitor;
import it.polimi.ingsw.model.interfaces.GainPPVisitor;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.visitors.CanDrawVisitor;
import it.polimi.ingsw.visitors.DrawCardVisitor;
import it.polimi.ingsw.visitors.PlayEventVisitor;

import java.util.ArrayList;
import java.util.List;

public class Building extends Card {
    private final int ppValue;
    private final int foodCost;

    @JsonCreator
    public Building(@JsonProperty("type") CardTypeEnum type,
                    @JsonProperty("id") int id, @JsonProperty("trigger") GamePhaseEnum trigger,
                    @JsonProperty("age") int age,@JsonProperty("ppValue") int ppValue,
                    @JsonProperty("foodCost") int foodCost,
                    @JsonProperty("instantEffects") List<CardEffectInstant> instantEffects,
                    @JsonProperty("interactiveEffects") List<CardEffectInteractive> interactiveEffects
                    ) {
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

    public void accept(GainPPVisitor visitor, Player p, GainPP e){
        visitor.visit(this, p, e);
    }

    public void accept(PlayEventVisitor visitor){}

    public void accept(CanDrawVisitor visitor){visitor.visit(this);}

    public void accept(DrawCardVisitor visitor){visitor.visit(this);}

    @Override
    public List<Action> execInteractiveEffect(Player p){
        List<Action> actions = new ArrayList<>();
            for(CardEffectInteractive e : interactiveEffects){
                Action a = e.apply(p);
                actions.add(a);
            }
        return actions;
    }
}
