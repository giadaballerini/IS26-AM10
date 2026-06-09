package it.polimi.ingsw.network.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Carries the data needed to update the client when the game advances to a
 * new age (era).
 *
 * <p>Sent by the server at the start of each new age so that the client can
 * refresh the drawable card rows and the deck counter.
 */
public class ChangeAgeDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** The new age the game has advanced to. */
    private int age;

    /** Cards now available in the upper row for the new age. */
    private final List<CardDTO> upperList;

    /** Cards now available in the lower row for the new age. */
    private final List<CardDTO> lowerList;

    /** Number of cards remaining in the deck after the age transition. */
    private final int deckSize;

    /**
     * Creates a {@code ChangeAgeDTO} describing a transition to a new age.
     *
     * @param upperList new upper card row
     * @param lowerList new lower card row
     * @param currAge   the age the game has just entered
     * @param deckSize  number of cards remaining in the deck
     */
    public ChangeAgeDTO(List<CardDTO> upperList, List<CardDTO> lowerList, int currAge, int deckSize) {
        this.upperList = upperList;
        this.lowerList = lowerList;
        this.age = currAge;
        this.deckSize = deckSize;
    }

    /**
     * Returns a copy of the upper card row for the new age.
     *
     * @return upper card list
     */
    public List<CardDTO> getUpperList() {
        return new ArrayList<>(upperList);
    }

    /**
     * Returns a copy of the lower card row for the new age.
     *
     * @return lower card list
     */
    public List<CardDTO> getLowerList() {
        return new ArrayList<>(lowerList);
    }

    /**
     * Returns the number of cards remaining in the deck after the transition.
     *
     * @return deck size
     */
    public int getDeckSize() {
        return deckSize;
    }

    /**
     * Returns the age the game has just entered.
     *
     * @return new game age
     */
    public int getAge() {
        return age;
    }
}