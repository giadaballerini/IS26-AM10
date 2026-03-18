package it.polimi.ingsw.model.entities.tile;

import it.polimi.ingsw.model.entities.card.effects.instant.CardEffectInstant;
import it.polimi.ingsw.model.entities.card.effects.interactive.CardEffectInteractive;
import it.polimi.ingsw.model.entities.pawn.Pawn;

import java.util.List;

public class Tile {
    private boolean occupied;
    private char id;
    private int minPlayers;
    private Pawn pawn;
    private final List<CardEffectInstant> autoEffects;
    private final List<CardEffectInteractive> interactiveEffects;

    public Tile(boolean occupied, Pawn pawn, List<CardEffectInstant> autoEffects, List<CardEffectInteractive> interactiveEffects){
        this.occupied = occupied;
        this.pawn = pawn;
        this.autoEffects = autoEffects;
        this.interactiveEffects = interactiveEffects;
    }

    public void execInstantEffect(){

    }
    public void execInteractiveEffect(){

    }

    public Pawn getPawn(){
        return pawn;
    }

    public boolean isOccupied(){
        return occupied;
    }

    public int getMinPlayers(){
        return minPlayers;
    }

    public char getId(){
        return id;
    }

}
