package it.polimi.ingsw.network.dto;

import it.polimi.ingsw.enumerations.ColorPawnEnum;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a player as seen by the client.
 *
 * <p>Carries the player's identity (nickname and pawn color) together with
 * their current hand of building and character cards. Returned defensive
 * copies of the card lists to prevent accidental mutation.
 */
public class PlayerDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** The player's unique nickname. */
    private final String nickname;

    /** The color of the player's pawn on the board. */
    private final ColorPawnEnum color;

    /** Building cards currently held by the player. */
    private final List<CardDTO> myBuildings;

    /** Character cards currently held by the player. */
    private final List<CardDTO> myCharacters;

    /**
     * Creates a {@code PlayerDTO} with the given identity and card hand.
     *
     * @param nickname     the player's nickname
     * @param color        the player's pawn color
     * @param myBuildings  building cards held by the player
     * @param myCharacters character cards held by the player
     */
    public PlayerDTO(String nickname, ColorPawnEnum color,
                     List<CardDTO> myBuildings, List<CardDTO> myCharacters) {
        this.nickname = nickname;
        this.color = color;
        this.myBuildings = myBuildings;
        this.myCharacters = myCharacters;
    }

    /**
     * Copy constructor that replaces the building card list.
     * The nickname, color, and character cards are taken from {@code p}.
     *
     * @param p           the source player DTO
     * @param myBuildings the new building card list
     */
    public PlayerDTO(PlayerDTO p, List<CardDTO> myBuildings) {
        this.nickname = p.getNickname();
        this.color = p.getColor();
        this.myBuildings = myBuildings;
        this.myCharacters = p.getMyCharacters();
    }

    /**
     * Returns the player's nickname.
     *
     * @return nickname
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * Returns the color of the player's pawn.
     *
     * @return pawn colour
     */
    public ColorPawnEnum getColor() {
        return color;
    }

    /**
     * Returns a copy of the building cards currently held by the player.
     *
     * @return building card list
     */
    public List<CardDTO> getMyBuildings() {
        return new ArrayList<>(myBuildings);
    }

    /**
     * Returns a copy of the character cards currently held by the player.
     *
     * @return character card list
     */
    public List<CardDTO> getMyCharacters() {
        return new ArrayList<>(myCharacters);
    }

    /**
     * Returns a new {@code PlayerDTO} identical to this one but with the given
     * building card list.
     *
     * @param myBuilds the replacement building card list
     * @return updated player DTO
     */
    public PlayerDTO withMyBuilds(List<CardDTO> myBuilds) {
        return new PlayerDTO(nickname, color, myBuilds, myCharacters);
    }

    /**
     * Returns a new {@code PlayerDTO} identical to this one but with the given
     * character card list.
     *
     * @param myChars the replacement character card list
     * @return updated player DTO
     */
    public PlayerDTO withMyCharacters(List<CardDTO> myChars) {
        return new PlayerDTO(nickname, color, myBuildings, myChars);
    }
}