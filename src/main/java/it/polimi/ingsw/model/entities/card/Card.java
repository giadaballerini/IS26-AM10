package it.polimi.ingsw.model.entities.card;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.entities.card.effects.instant.CardEffectInstant;
import it.polimi.ingsw.model.entities.card.effects.interactive.CardEffectInteractive;
import it.polimi.ingsw.model.player.Player;

import java.util.List;

public abstract class Card {
    private GamePhaseEnum trigger;
    private List <CardEffectInteractive> interactiveEffects;
    private List<CardEffectInstant> instantEffects;
    private int age;


    public Card(GamePhaseEnum trigger, List <CardEffectInteractive> interactiveEffects,List <CardEffectInstant> instantEffects, int age) {
        this.trigger = trigger;
        this.interactiveEffects = interactiveEffects;
        this.instantEffects = instantEffects;
        this.age = age;
    }

    public int getAge(){
        return this.age;
    }

    public List<CardEffectInteractive> getInteractiveEffects(){
        return this.interactiveEffects;
    }

    public List<CardEffectInstant> getInstantEffects(){
        return this.instantEffects;
    }

    public void execInstantEffect(Player p, GamePhaseEnum gamePhase){
        for(CardEffectInstant e : instantEffects){
            e.apply(p,this,gamePhase);
        }

    }

    public void execInteractiveEffect(Player p, GamePhaseEnum gamePhase){
    }
}
