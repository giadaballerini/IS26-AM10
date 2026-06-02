package it.polimi.ingsw.network.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.enumerations.CardTypeEnum;
import java.io.Serial;
import java.io.Serializable;

public class CardDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final int id;
    private final int age;
    private final CardTypeEnum type;

    @JsonCreator
    public CardDTO(
            @JsonProperty("id") int id,
            @JsonProperty("age") int age,
            @JsonProperty("type") CardTypeEnum type) {
        this.id = id;
        this.age = age;
        this.type = type;
    }

    public int getId() {
        return id;
    }
    public int getAge() {
        return age;
    }
    public CardTypeEnum getType() {
        return type;
    }
}