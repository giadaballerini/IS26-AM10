package it.polimi.ingsw.network.dto;

import it.polimi.ingsw.enumerations.CardTypeEnum;

import java.io.Serial;
import java.io.Serializable;

public class CardDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final int id;
    private final int age;
    private final CardTypeEnum type;

    public CardDTO(int id, int age, CardTypeEnum type) {
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
