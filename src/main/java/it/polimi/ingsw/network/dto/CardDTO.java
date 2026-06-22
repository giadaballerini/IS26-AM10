package it.polimi.ingsw.network.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.enumerations.CardTypeEnum;

import java.io.Serial;
import java.io.Serializable;

/**
 * Represents a single card transferred between server and client.
 *
 * <p>Carries only the data the client needs to display and interact with a
 * card: its unique ID, the age it belongs to, and its category.
 */
public class CardDTO implements Serializable {

    /**
     *  Required by the {@link Serializable} interface.
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /** Unique identifier of the card. */
    private final int id;

    /** Age (era) the card belongs to. */
    private final int age;

    /** Category of the card. */
    private final CardTypeEnum type;

    /**
     * Creates a {@code CardDTO} with the given attributes.
     * The {@link JsonCreator} annotation allows Jackson to deserialize this
     * object directly from JSON.
     *
     * @param id   unique card identifier
     * @param age  age (era) the card belongs to
     * @param type category of the card
     */
    @JsonCreator
    public CardDTO(
            @JsonProperty("id") int id,
            @JsonProperty("age") int age,
            @JsonProperty("type") CardTypeEnum type) {
        this.id = id;
        this.age = age;
        this.type = type;
    }

    /**
     * Returns the unique identifier of this card.
     *
     * @return card ID
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the age (era) this card belongs to.
     *
     * @return card age
     */
    public int getAge() {
        return age;
    }

    /**
     * Returns the category of this card.
     *
     * @return card type
     */
    public CardTypeEnum getType() {
        return type;
    }
}