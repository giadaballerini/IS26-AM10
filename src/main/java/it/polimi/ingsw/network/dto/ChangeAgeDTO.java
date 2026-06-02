package it.polimi.ingsw.network.dto;


import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ChangeAgeDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private int age;
    private final List<CardDTO> upperList;
    private final List<CardDTO> lowerList;


    public ChangeAgeDTO(List<CardDTO> upperList, List<CardDTO> lowerList, int currAge) {
        this.upperList = upperList;
        this.lowerList = lowerList;
        this.age = currAge;
    }

    public List<CardDTO> getUpperList() {
        return new ArrayList<>(upperList);
    }

    public List<CardDTO> getLowerList() {
        return new ArrayList<>(lowerList);
    }

    public int getAge() {return age;}
}
