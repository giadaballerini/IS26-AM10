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

/**
 * Represents a player's village: the collection of character cards they have
 * played during the game.
 *
 * <p>Provides query methods for counting characters by type or symbol, and
 * uses the Visitor pattern (via {@link VillageVisitor}) to compute aggregated
 * values such as builder prestige points and crafter symbol counts without
 * exposing the internal list directly.</p>
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Village {

    /** All character cards currently placed in this village. */
    private final List<Character> characters;

    /**
     * Constructs an empty village.
     */
    public Village() {
        this.characters = new ArrayList<>();
    }

    /**
     * Constructs a village from a list of characters, used by Jackson during
     * deserialization. If {@code characters} is {@code null}, an empty list
     * is used instead.
     *
     * @param characters the character cards to populate the village with
     */
    @JsonCreator
    public Village(@JsonProperty("characters") List<Character> characters) {
        this.characters = characters != null ? characters : new ArrayList<>();
    }

    /**
     * Adds a character card to this village.
     *
     * @param c the character to add; must not be {@code null}
     */
    public void add(Character c) {
        characters.add(c);
    }

    /**
     * Returns the total number of character cards in this village.
     *
     * @return number of characters
     */
    public int getNumCharacters() {
        return characters.size();
    }

    /**
     * Returns the number of characters of the given type in this village.
     *
     * @param t the card type to count
     * @return count of characters of type {@code t}
     */
    public int getNumType(CardTypeEnum t) {
        return (int) characters.stream().filter(c -> c.getType() == t).count();
    }

    /**
     * Returns the number of crafter cards in the village that carry the given symbol.
     *
     * @param s the crafter symbol to count
     * @return count of crafter cards with symbol {@code s}
     */
    public int getNumSymbolsForCrafter(CrafterSymbolEnum s) {
        CrafterSymbolCounter counter = new CrafterSymbolCounter(s);
        for (Character c : characters) {
            c.accept(counter);
        }
        return counter.getCount();
    }

    /**
     * Returns the total prestige points contributed by all builder cards in
     * the village.
     *
     * @return sum of PP amounts from all {@link Builder} cards
     */
    public int builderPoints() {
        BuilderPointsSum visitor = new BuilderPointsSum();
        for (Character c : characters) {
            c.accept(visitor);
        }
        return visitor.getTotal();
    }

    /**
     * Returns a list of {@link CardDTO} representations of all characters in
     * this village, suitable for network transfer.
     *
     * @return a new list of DTOs; never {@code null}
     */
    public List<CardDTO> getCharactersDTO() {
        List<CardDTO> allCharactersDTO = characters.stream()
                .map(Card::toDTO)
                .toList();
        return new ArrayList<>(allCharactersDTO);
    }

    /**
     * Visitor that counts how many {@link Crafter} cards in the village carry
     * a specific {@link CrafterSymbolEnum}.
     */
    private static class CrafterSymbolCounter implements VillageVisitor {

        /** The symbol being counted. */
        private final CrafterSymbolEnum symbol;

        /** Running count of matching crafter cards. */
        private int count = 0;

        /**
         * Constructs a counter for the given symbol.
         *
         * @param symbol the crafter symbol to match
         */
        private CrafterSymbolCounter(CrafterSymbolEnum symbol) {
            this.symbol = symbol;
        }

        /**
         * Increments the count if the visited crafter carries the target symbol.
         *
         * @param crafter the crafter card being visited
         */
        @Override
        public void visit(Crafter crafter) {
            if (crafter.getSymbol().equals(symbol)) count++;
        }

        /**
         * Returns the number of matching crafter cards found so far.
         *
         * @return the count
         */
        private int getCount() { return count; }
    }

    /**
     * Visitor that sums the prestige point contributions of all {@link Builder}
     * cards in the village.
     */
    private static class BuilderPointsSum implements VillageVisitor {

        /** Accumulated prestige points from builder cards. */
        private int total = 0;

        /**
         * Adds the visited builder's PP amount to the running total.
         *
         * @param builder the builder card being visited
         */
        @Override
        public void visit(Builder builder) {
            total += builder.getPpAmount();
        }

        /**
         * Returns the total prestige points accumulated so far.
         *
         * @return the total
         */
        private int getTotal() { return total; }
    }
}