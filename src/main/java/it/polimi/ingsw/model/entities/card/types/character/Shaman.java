package it.polimi.ingsw.model.entities.card.types.character;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.entities.card.effects.instant.CardEffectInstant;
import it.polimi.ingsw.model.entities.card.effects.instant.GainFood;
import it.polimi.ingsw.model.entities.card.effects.instant.GainPP;
import it.polimi.ingsw.model.entities.card.effects.interactive.CardEffectInteractive;
import it.polimi.ingsw.model.interfaces.GainFoodVisitor;
import it.polimi.ingsw.model.interfaces.GainPPVisitor;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Village;

import java.util.List;

public class Shaman extends Character {

    @JsonCreator
    public Shaman(@JsonProperty("id") int id,@JsonProperty("trigger") GamePhaseEnum trigger,
                  @JsonProperty("interactiveEffects") List<CardEffectInteractive> interactiveEffects,
                  @JsonProperty("instantEffects") List<CardEffectInstant> instantEffects,
                  @JsonProperty("age") int age, @JsonProperty("type") CardTypeEnum type) {
        super(id,trigger, interactiveEffects, instantEffects, age, type);
    }

    @Override
    public void dispatch(Village v){
        v.add(this);
    }

    public void accept(GainFoodVisitor visitor, Player p, GainFood e){
        visitor.visit(this, p, e);
    }

    public void accept (GainPPVisitor visitor, Player p, GainPP e){
        visitor.visit(this, p, e);
    }
}