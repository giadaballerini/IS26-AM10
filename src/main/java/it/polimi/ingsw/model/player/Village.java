package it.polimi.ingsw.model.player;

import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.CrafterSymbolEnum;
import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.entities.card.effects.instant.CardEffectInstant;
import it.polimi.ingsw.model.entities.card.types.character.Crafter;
import it.polimi.ingsw.model.entities.card.types.character.Builder;
import it.polimi.ingsw.model.entities.card.types.character.Painter;
import it.polimi.ingsw.model.entities.card.types.character.Gatherer;
import it.polimi.ingsw.model.entities.card.types.character.Hunter;
import it.polimi.ingsw.model.entities.card.types.character.Shaman;
import it.polimi.ingsw.network.dto.CardDTO;
import javafx.scene.effect.Effect;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Village {

    private List<Crafter> crafters;
    private List<Builder> builders;
    private List<Painter> painters;
    private List<Hunter> hunters;
    private List<Gatherer> gatherers;
    private List<Shaman> shamans;

    public Village() {
        this.crafters = new ArrayList<Crafter>();
        this.builders = new ArrayList<Builder>();
        this.painters = new ArrayList<Painter>();
        this.hunters = new ArrayList<Hunter>();
        this.gatherers = new ArrayList<Gatherer>();
        this.shamans = new ArrayList<Shaman>();
    }

    public void add(Crafter c) {
        crafters.add(c);
    }

    public void add(Builder b) {
        builders.add(b);
    }

    public void add(Painter p) {
        painters.add(p);
    }

    public void add(Hunter h) {
        hunters.add(h);
    }

    public void add(Gatherer g) {
        gatherers.add(g);
    }

    public void add(Shaman s) {
        shamans.add(s);
    }

    public int getNumCharacters(){
        return crafters.size()+ builders.size()
                + painters.size() + hunters.size()
                + gatherers.size() + shamans.size();
    }

    public int getNumType(CardTypeEnum t){
        return switch(t){
            case CardTypeEnum.CRAFTER -> crafters.size();
            case CardTypeEnum.BUILDER -> builders.size();
            case CardTypeEnum.PAINTER -> painters.size();
            case CardTypeEnum.HUNTER -> hunters.size();
            case CardTypeEnum.SHAMAN -> shamans.size();
            case CardTypeEnum.GATHERER -> gatherers.size();
            default -> -1;
        };

    }

    public int getNumSymbolsForCrafter(CrafterSymbolEnum c){
        int num = 0;
        for(Crafter crafter : crafters){
            if(crafter.getSymbol().equals(c))
                num++;
        }
        return num;
    }

    public int builderPoints(){
        int points = 0;
        for(Builder b : builders){
            for(CardEffectInstant e : b.getInstantEffects()){
                 points += e.getPpAmount();
            }
        }
        return points;
    }

    public List<CardDTO> getCharactersDTO(){
        List<CardDTO> allCharactersDTO = Stream.of(crafters, builders, painters, hunters, shamans, gatherers)
                .flatMap(List::stream)
                .map(Card::toDTO)
                .toList();

        return new ArrayList<CardDTO>(allCharactersDTO);
    }

}
