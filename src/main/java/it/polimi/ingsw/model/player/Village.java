package it.polimi.ingsw.model.player;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.CrafterSymbolEnum;
import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.entities.card.types.character.Builder;
import it.polimi.ingsw.model.entities.card.types.character.Character;
import it.polimi.ingsw.model.entities.card.types.character.Crafter;
import it.polimi.ingsw.network.dto.CardDTO;
import it.polimi.ingsw.visitors.VillageVisitor;

import java.util.ArrayList;
import java.util.List;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Village {

    private final List<Character> characters;

    public Village() {
        this.characters = new ArrayList<Character>();
    }

    @JsonCreator
    public Village(@JsonProperty("characters")  List<Character>  characters)
    {
        this.characters  = characters != null ? characters : new ArrayList<>();
    }

    public void add(Character c) {
        characters.add(c);
    }

    public int getNumCharacters(){
        return characters.size();
    }

    public int getNumType(CardTypeEnum t){
        return (int) characters.stream().filter(c->c.getType() == t ).count();
    }

    public int getNumSymbolsForCrafter(CrafterSymbolEnum s){
        CrafterSymbolCounter counter = new CrafterSymbolCounter(s);

        for(Character c: characters){
            c.accept(counter);
        }

        return counter.getCount();
    }

    public int builderPoints(){
        BuilderPointsSum visitor = new BuilderPointsSum();
        for(Character c: characters){
            c.accept(visitor);
        }

        return visitor.getTotal();
    }

    public List<CardDTO> getCharactersDTO(){
        List<CardDTO> allCharactersDTO = characters.stream()
                .map(Card::toDTO)
                .toList();

        return new ArrayList<CardDTO>(allCharactersDTO);
    }

    private static class CrafterSymbolCounter implements VillageVisitor {
        private final CrafterSymbolEnum symbol;
        private int count = 0;

        private CrafterSymbolCounter(CrafterSymbolEnum symbol) {
            this.symbol = symbol;
        }

        @Override
        public void visit(Crafter crafter) {
            if (crafter.getSymbol().equals(symbol)) count++;
        }

        private int getCount() { return count; }
    }

    private static class BuilderPointsSum implements VillageVisitor {
        private int total = 0;

        @Override
        public void visit(Builder builder) {
            total += builder.getPpAmount();
        }

        private int getTotal() { return total; }
    }
}
