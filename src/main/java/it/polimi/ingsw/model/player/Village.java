package it.polimi.ingsw.model.player;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.CrafterSymbolEnum;
import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.entities.card.types.character.Crafter;
import it.polimi.ingsw.model.entities.card.types.character.Builder;
import it.polimi.ingsw.model.entities.card.types.character.Painter;
import it.polimi.ingsw.model.entities.card.types.character.Gatherer;
import it.polimi.ingsw.model.entities.card.types.character.Hunter;
import it.polimi.ingsw.model.entities.card.types.character.Shaman;
import it.polimi.ingsw.network.dto.CardDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Village {

    private final List<Crafter> crafters;
    private final List<Builder> builders;
    private final List<Painter> painters;
    private final List<Hunter> hunters;
    private final List<Gatherer> gatherers;
    private final List<Shaman> shamans;

    public Village() {
        this.crafters = new ArrayList<Crafter>();
        this.builders = new ArrayList<Builder>();
        this.painters = new ArrayList<Painter>();
        this.hunters = new ArrayList<Hunter>();
        this.gatherers = new ArrayList<Gatherer>();
        this.shamans = new ArrayList<Shaman>();
    }

    @JsonCreator
    public Village(
            @JsonProperty("crafters")  List<Crafter>  crafters,
            @JsonProperty("builders")  List<Builder>  builders,
            @JsonProperty("painters")  List<Painter>  painters,
            @JsonProperty("hunters")   List<Hunter>   hunters,
            @JsonProperty("gatherers") List<Gatherer> gatherers,
            @JsonProperty("shamans")   List<Shaman>   shamans) {

        this.crafters  = crafters != null ? crafters : new ArrayList<>();
        this.builders  = builders != null ? builders : new ArrayList<>();
        this.painters  = painters != null ? painters : new ArrayList<>();
        this.hunters   = hunters != null ? hunters : new ArrayList<>();
        this.gatherers = gatherers != null ? gatherers : new ArrayList<>();
        this.shamans   = shamans != null ? shamans : new ArrayList<>();
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
            default -> 0;
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
        return builders.stream()
                .mapToInt(Builder::getPpAmount)
                .sum();
    }

    public List<CardDTO> getCharactersDTO(){
        List<CardDTO> allCharactersDTO = Stream.of(crafters, builders, painters, hunters, shamans, gatherers)
                .flatMap(List::stream)
                .map(Card::toDTO)
                .toList();

        return new ArrayList<CardDTO>(allCharactersDTO);
    }

}
