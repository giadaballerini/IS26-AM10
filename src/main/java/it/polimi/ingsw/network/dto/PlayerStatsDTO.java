package it.polimi.ingsw.network.dto;

import java.io.Serializable;

public class PlayerStatsDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    String nickname;
    int nFood;
    int pps;
    int nStars;
    public PlayerStatsDTO(String nickname, int nFood, int pps, int nStars){
        this.nickname = nickname;
        this.nFood = nFood;
        this.pps = pps;
        this.nStars = nStars;

    }
    public int getnFood() {
        return nFood;
    }
    public int getPPs() {
        return pps;
    }
    public int getnStars() {
        return nStars;
    }
    public String getNickname() {
        return nickname;
    }
}
