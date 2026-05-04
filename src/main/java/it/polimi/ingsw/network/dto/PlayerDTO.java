package it.polimi.ingsw.network.dto;

import it.polimi.ingsw.enumerations.ColorPawnEnum;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PlayerDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String nickname;
    private final ColorPawnEnum color;

    private final List<CardDTO> myBuildings;
    private final List<CardDTO> myCharacters;

    public PlayerDTO(String nickname, ColorPawnEnum color, List<CardDTO> myBuildings, List<CardDTO> myCharacters) {
        this.nickname = nickname;
        this.color = color;
        this.myBuildings = myBuildings;
        this.myCharacters = myCharacters;
    }

    public PlayerDTO(PlayerDTO p, List<CardDTO> myBuildings) {
        this.nickname = p.getNickname();
        this.color = p.getColor();
        this.myBuildings = myBuildings;
        this.myCharacters = p.getMyCharacters();
    }

    /*public PlayerDTO(PlayerDTO p, List<CardDTO> myCharacters) {
        this.nickname = p.getNickname();
        this.color = p.getColor();
        this.myBuildings = p.getMyBuildings();
        this.myCharacters = myCharacters;
    }*/

    public String getNickname() {
        return nickname;
    }

    public ColorPawnEnum getColor(){
        return color;
    }

    public List<CardDTO> getMyBuildings() {
        return new ArrayList<>(myBuildings);
    }

    public List<CardDTO> getMyCharacters() {
        return new ArrayList<>(myCharacters);
    }

    public PlayerDTO withMyBuilds(List<CardDTO> myBuilds) {
        return new PlayerDTO(nickname, color, myBuilds, myCharacters);
    }

    public PlayerDTO withMyCharacters(List<CardDTO> myChars) {
        return new PlayerDTO(nickname, color, myBuildings, myChars);
    }
}
