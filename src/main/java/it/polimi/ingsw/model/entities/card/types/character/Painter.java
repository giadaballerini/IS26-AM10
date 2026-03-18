package it.polimi.ingsw.model.entities.card.types.character;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.entities.card.effects.instant.CardEffectInstant;
import it.polimi.ingsw.model.entities.card.effects.interactive.CardEffectInteractive;

import java.util.List;

public class Painter extends Character {

    @JsonCreator
    public Painter( @JsonProperty("id") int id, @JsonProperty("trigger") GamePhaseEnum trigger,
                   @JsonProperty("interactiveEffects") List<CardEffectInteractive> interactiveEffects,
                   @JsonProperty("instantEffects") List<CardEffectInstant> instantEffects,@JsonProperty("age") int age,
                   @JsonProperty("type") CardTypeEnum type) {
        super(id, trigger, interactiveEffects, instantEffects, age, type);
    }
}
