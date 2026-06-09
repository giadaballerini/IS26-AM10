package it.polimi.ingsw.network.dto;

import java.io.Serializable;

/**
 * Carries the current numerical statistics for a single player.
 *
 * <p>Sent by the server whenever a player's score or food count changes.
 */
public class PlayerStatsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** The player these statistics belong to. */
    String nickname;

    /** Current food amount held by the player. */
    int nFood;

    /** Current prestige points (PP) accumulated by the player. */
    int pps;

    /** Current number of stars collected by the player. */
    int nStars;

    /** Total building discount accumulated by the player. */
    int totBuildDisc;

    /** Food discount currently active for the player. */
    int foodDiscount;

    /**
     * Creates a {@code PlayerStatsDTO} with the given statistics.
     *
     * @param nickname     the player's nickname
     * @param nFood        current food amount
     * @param pps          current prestige points
     * @param nStars       current star count
     * @param totBuildDisc total building discount
     * @param foodDiscount current food discount
     */
    public PlayerStatsDTO(String nickname, int nFood, int pps, int nStars,
                          int totBuildDisc, int foodDiscount) {
        this.nickname = nickname;
        this.nFood = nFood;
        this.pps = pps;
        this.nStars = nStars;
        this.totBuildDisc = totBuildDisc;
        this.foodDiscount = foodDiscount;
    }

    /**
     * Returns the current food amount held by the player.
     *
     * @return food amount
     */
    public int getnFood() {
        return nFood;
    }

    /**
     * Returns the current prestige points accumulated by the player.
     *
     * @return prestige points
     */
    public int getPPs() {
        return pps;
    }

    /**
     * Returns the current number of stars collected by the player.
     *
     * @return star count
     */
    public int getnStars() {
        return nStars;
    }

    /**
     * Returns the nickname of the player these statistics belong to.
     *
     * @return player nickname
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * Returns the total building discount accumulated by the player.
     *
     * @return total building discount
     */
    public int getTotBuildDisc() {
        return totBuildDisc;
    }

    /**
     * Returns the food discount currently active for the player.
     *
     * @return food discount
     */
    public int getFoodDiscount() {
        return foodDiscount;
    }
}