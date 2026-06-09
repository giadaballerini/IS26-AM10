package it.polimi.ingsw.model.player;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.ColorPawnEnum;
import it.polimi.ingsw.enumerations.CrafterSymbolEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.action.Action;
import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.entities.card.types.building.Building;
import it.polimi.ingsw.model.entities.card.types.character.Character;
import it.polimi.ingsw.network.dto.PlayerDTO;
import it.polimi.ingsw.network.dto.PlayerStatsDTO;
import it.polimi.ingsw.network.dto.PlayerStatusDTO;

import java.util.*;

/**
 * Represents a player in the game, holding all their resources, bonuses,
 * buildings, and village state.
 *
 * <p>Supports both normal construction (via {@link #Player(String, ColorPawnEnum)})
 * and full deserialization from a saved snapshot (via the {@link JsonCreator}
 * constructor). All mutable state (food, stars, PP, bonuses, etc.) is managed
 * through dedicated methods rather than direct field access.</p>
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Player {

    /** Prestige points accumulated by this player. */
    private int pps;

    /** Amount of food currently held by this player. */
    private int nFood;

    /** Number of stars currently held by this player. */
    private int nStars;

    /** Total building discount accumulated across all buildings. */
    private int totBuildDisc;

    /** Flat food discount applied when paying feast costs. */
    private int foodDiscount;

    /** The player's village, holding all placed character cards. */
    protected Village myVillage;

    /** Buildings owned by this player. */
    private List<Building> myBuilds;

    /**
     * Draw actions that this player may choose to skip rather than being
     * forced to resolve them.
     */
    private List<Action> skippableDraws;

    /** The player's unique nickname. */
    private final String nickname;

    /** The color of this player's pawn on the board. */
    private final ColorPawnEnum colorPawn;

    /** Whether the hunt bonus is active (extra PP and food per Hunter). */
    private boolean huntBonus;

    /**
     * Card categories for which this player has a discount when calculating
     * feast costs (one food discount per card of each category owned).
     */
    private EnumSet<CardTypeEnum> categoryDiscounts;

    /** Whether the paint bonus is active (extra food per Painter on queue). */
    private boolean paintBonus;

    /** Whether this player gains an extra food when entering a tile with a food effect. */
    private boolean extraFoodOnQueue;

    /** Whether this player is protected from prestige point losses during rituals. */
    private boolean ppProtection;

    /** Whether this player's shaman income is doubled during rituals. */
    private boolean doubleShamanIncome;

    /**
     * Constructs a new player with default (zero) resource values and no active bonuses.
     *
     * @param nickname  the player's unique nickname; must not be {@code null}
     * @param colorPawn the color assigned to this player's pawn; must not be {@code null}
     */
    public Player(String nickname, ColorPawnEnum colorPawn) {
        this.pps = 0;
        this.nFood = 0;
        this.nStars = 0;
        this.totBuildDisc = 0;
        this.foodDiscount = 0;
        this.myVillage = new Village();
        this.myBuilds = new LinkedList<>();
        this.skippableDraws = new LinkedList<>();
        this.nickname = nickname;
        this.ppProtection = false;
        this.doubleShamanIncome = false;
        this.colorPawn = colorPawn;
        this.huntBonus = false;
        categoryDiscounts = EnumSet.noneOf(CardTypeEnum.class);
        this.paintBonus = false;
        this.extraFoodOnQueue = false;
    }

    /**
     * Constructs a fully initialised {@code Player} from a saved snapshot.
     *
     * <p>Used by Jackson during deserialization; all parameters map directly
     * to their corresponding JSON properties.</p>
     *
     * @param nickname           the player's unique nickname
     * @param colorPawn          the color of the player's pawn
     * @param pps                prestige points
     * @param nFood              amount of food
     * @param nStars             number of stars
     * @param totBuildDisc       total building discount
     * @param foodDiscount       flat food discount for feast costs
     * @param myVillage          the player's village
     * @param myBuilds           list of owned buildings
     * @param skippableDraws     list of pending skippable draw actions
     * @param ppProtection       whether PP loss protection is active
     * @param doubleShamanIncome whether double shaman income is active
     * @param huntBonus          whether the hunt bonus is active
     * @param categoryDiscounts  card categories granting a feast discount
     * @param paintBonus         whether the paint bonus is active
     * @param extraFoodOnQueue   whether extra food on queue tiles is active
     */
    @JsonCreator
    public Player(
            @JsonProperty("nickname")             String            nickname,
            @JsonProperty("colorPawn")            ColorPawnEnum     colorPawn,
            @JsonProperty("pps")                  int               pps,
            @JsonProperty("nFood")                int               nFood,
            @JsonProperty("nStars")               int               nStars,
            @JsonProperty("totBuildDisc")         int               totBuildDisc,
            @JsonProperty("foodDiscount")         int               foodDiscount,
            @JsonProperty("myVillage")            Village           myVillage,
            @JsonProperty("myBuilds")             List<Building>    myBuilds,
            @JsonProperty("skippableDraws")       List<Action>      skippableDraws,
            @JsonProperty("ppProtection")         boolean           ppProtection,
            @JsonProperty("doubleShamanIncome")   boolean           doubleShamanIncome,
            @JsonProperty("huntBonus")            boolean           huntBonus,
            @JsonProperty("categoryDiscounts")    Set<CardTypeEnum> categoryDiscounts,
            @JsonProperty("paintBonus")           boolean           paintBonus,
            @JsonProperty("extraFoodOnQueue")     boolean           extraFoodOnQueue) {

        this.nickname = nickname;
        this.colorPawn = colorPawn;
        this.pps = pps;
        this.nFood = nFood;
        this.nStars = nStars;
        this.totBuildDisc = totBuildDisc;
        this.foodDiscount = foodDiscount;
        this.myVillage = myVillage;
        this.myBuilds = myBuilds;
        this.skippableDraws = skippableDraws;
        this.ppProtection = ppProtection;
        this.doubleShamanIncome = doubleShamanIncome;
        this.huntBonus = huntBonus;
        this.categoryDiscounts = (categoryDiscounts != null && !categoryDiscounts.isEmpty())
                ? EnumSet.copyOf(categoryDiscounts)
                : EnumSet.noneOf(CardTypeEnum.class);
        this.paintBonus = paintBonus;
        this.extraFoodOnQueue = extraFoodOnQueue;
    }

    /**
     * Returns the player's current prestige points.
     *
     * @return prestige points
     */
    public int getPP() {
        return this.pps;
    }

    /**
     * Returns the player's current food amount.
     *
     * @return amount of food
     */
    public int getNFood() {
        return this.nFood;
    }

    /**
     * Returns the player's current number of stars.
     *
     * @return number of stars
     */
    public int getNStars() {
        return this.nStars;
    }

    /**
     * Returns the total building discount accumulated by this player.
     *
     * @return total building discount
     */
    public int getTotBuildDisc() {
        return this.totBuildDisc;
    }

    /**
     * Returns the number of character cards currently in the player's village.
     *
     * @return number of characters in the village
     */
    public int getNumCharacters() {
        return myVillage.getNumCharacters();
    }

    /**
     * Returns the number of cards of the given type in the player's village.
     *
     * @param t the card type to count
     * @return count of cards of type {@code t} in the village
     */
    public int getNumType(CardTypeEnum t) {
        return myVillage.getNumType(t);
    }

    /**
     * Returns the player's flat food discount applied to feast costs.
     *
     * @return food discount
     */
    public int getFoodDiscount() {
        return this.foodDiscount;
    }

    /**
     * Returns the list of buildings owned by this player.
     *
     * @return the player's buildings; never {@code null}
     */
    public List<Building> getBuildings() {
        return this.myBuilds;
    }

    /**
     * Places a character card into the player's village.
     *
     * @param c the character card to add; must not be {@code null}
     */
    public void addCard(Character c) {
        c.dispatch(this.myVillage);
    }

    /**
     * Adds the given amount of food to the player's stock.
     *
     * @param food the amount of food to add (may be negative to subtract)
     */
    public void addFood(int food) {
        this.nFood += food;
    }

    /**
     * Adds a building to the player's collection.
     *
     * @param building the building to add; must not be {@code null}
     */
    public void addBuilding(Building building) {
        this.myBuilds.add(building);
    }

    /**
     * Adds the given amount to the player's prestige points.
     *
     * @param pp the amount to add (may be negative to subtract)
     */
    public void addPP(int pp) {
        this.pps += pp;
    }

    /**
     * Increases the player's flat food discount by the given delta.
     *
     * @param delta the amount to add to the food discount
     */
    public void addFoodDiscount(int delta) {
        this.foodDiscount += delta;
    }

    /**
     * Returns the number of crafter symbols of the given type in the player's village.
     *
     * @param c the crafter symbol type to count
     * @return count of matching crafter symbols
     */
    public int getNumSymbolsForCrafter(CrafterSymbolEnum c) {
        return myVillage.getNumSymbolsForCrafter(c);
    }

    /**
     * Adds the given number of stars to the player's total.
     *
     * @param starsAmount the number of stars to add
     */
    public void addStars(int starsAmount) {
        this.nStars += starsAmount;
    }

    /**
     * Returns the prestige points contributed by builder cards in the village.
     *
     * @return builder prestige points
     */
    public int getBuilderPoints() {
        return myVillage.builderPoints();
    }

    /**
     * Returns the number of distinct crafter symbol types present in the village
     * (each type counts once, regardless of how many symbols of that type exist).
     *
     * @return number of distinct crafter symbol types
     */
    public int getTotSymbolsForCrafter() {
        int tot = 0;
        for (CrafterSymbolEnum s : CrafterSymbolEnum.values()) {
            if (getNumSymbolsForCrafter(s) != 0)
                tot += 1;
        }
        return tot;
    }

    /**
     * Executes the effects of all buildings owned by this player for the given
     * game phase, collecting and returning any interactive actions they generate.
     *
     * @param currPhase the current game phase
     * @return list of {@link Action} instances produced by building effects;
     *         never {@code null}, may be empty
     */
    public List<Action> checkBuildsEffects(GamePhaseEnum currPhase) {
        List<Action> actions = new ArrayList<>();
        for (Building b : myBuilds) {
            b.execInstantEffect(this, currPhase);
            if (b.getTrigger() == currPhase) {
                List<Action> buildingActions = b.execInteractiveEffect(this);
                if (!buildingActions.isEmpty())
                    actions.addAll(buildingActions);
            }
        }
        return actions;
    }

    /**
     * Activates the hunt bonus, causing {@link #applyHuntBonus()} to grant
     * extra PP and food equal to the number of Hunter cards in the village.
     */
    public void activateHuntBonus() {
        this.huntBonus = true;
    }

    /**
     * If the hunt bonus is active, awards PP and food equal to the number of
     * Hunter cards in the player's village.
     */
    public void applyHuntBonus() {
        if (huntBonus) {
            int hunters = getNumType(CardTypeEnum.HUNTER);
            addPP(hunters);
            addFood(hunters);
        }
    }

    /**
     * Activates the paint bonus, causing {@link #applyPaintBonus()} to grant
     * extra food equal to the number of Painter cards in the village.
     */
    public void activatePaintBonus() {
        this.paintBonus = true;
    }

    /**
     * If the paint bonus is active, awards food equal to the number of
     * Painter cards in the player's village.
     */
    public void applyPaintBonus() {
        if (paintBonus)
            addFood(getNumType(CardTypeEnum.PAINTER));
    }

    /**
     * Activates the extra food on queue bonus, causing
     * {@link #applyQueueFoodBonus(boolean)} to award an additional food
     * when entering a tile that has a food effect.
     */
    public void activateExtraFoodOnQueue() {
        this.extraFoodOnQueue = true;
    }

    /**
     * If the extra food on queue bonus is active and the tile has a food effect,
     * awards one additional food to the player.
     *
     * @param tileHasFoodEffect {@code true} if the tile the player entered
     *                          normally provides a food reward
     */
    public void applyQueueFoodBonus(boolean tileHasFoodEffect) {
        if (extraFoodOnQueue && tileHasFoodEffect)
            addFood(1);
    }

    /**
     * Activates the double shaman income bonus, causing
     * {@link #applyRitualGain(int)} to award twice the base PP gain.
     */
    public void activateDoubleShaman() {
        this.doubleShamanIncome = true;
    }

    /**
     * Awards PP from a ritual, doubling the gain if the double shaman bonus is active.
     *
     * @param basePpGain the base amount of PP the ritual would normally award
     */
    public void applyRitualGain(int basePpGain) {
        addPP(doubleShamanIncome ? basePpGain * 2 : basePpGain);
    }

    /**
     * Activates PP loss protection, preventing {@link #applyRitualLoss(int)}
     * from deducting prestige points during rituals.
     */
    public void activatePpProtection() {
        this.ppProtection = true;
    }

    /**
     * Deducts PP from a ritual loss, unless PP protection is active.
     *
     * @param ppLoss the amount of PP the ritual would normally deduct
     */
    public void applyRitualLoss(int ppLoss) {
        if (!ppProtection) addPP(-ppLoss);
    }

    /**
     * Increases the total building discount by the given amount.
     *
     * @param disc the discount amount to add
     */
    public void addTotBuildDiscount(int disc) {
        this.totBuildDisc += disc;
    }

    /**
     * Returns the player's nickname.
     *
     * @return the nickname; never {@code null}
     */
    public String getNickname() {
        return this.nickname;
    }

    /**
     * Adds the given card type to the set of categories that grant a feast discount.
     *
     * @param type the card type to add as a discount category
     */
    public void addCategoryDiscount(CardTypeEnum type) {
        categoryDiscounts.add(type);
    }

    /**
     * Calculates the total feast discount for this player, combining the flat
     * food discount with the count of cards in each discount category.
     *
     * @return the total feast discount
     */
    public int calculateFeastDiscount() {
        int discount = foodDiscount;
        for (CardTypeEnum type : categoryDiscounts) {
            discount += getNumType(type);
        }
        return discount;
    }

    /**
     * Adds a list of skippable draw actions to the player's pending queue.
     *
     * @param skippableDraws the actions to add; must not be {@code null}
     */
    public void addSkippableDraws(List<Action> skippableDraws) {
        this.skippableDraws.addAll(skippableDraws);
    }

    /**
     * Returns whether this player has any pending skippable draw actions.
     *
     * @return {@code true} if there is at least one skippable draw pending
     */
    public boolean hasSkippableDraws() {
        return !skippableDraws.isEmpty();
    }

    /**
     * Removes and returns all pending skippable draw actions, clearing the queue.
     *
     * @return the list of resolved actions; never {@code null}
     */
    public List<Action> resolveSkippableDraws() {
        List<Action> resolved = new ArrayList<>(skippableDraws);
        skippableDraws.clear();
        return resolved;
    }

    /**
     * Converts this player to a {@link PlayerDTO} for network transfer,
     * including nickname, pawn color, buildings, and village characters.
     *
     * @return a DTO representation of this player; never {@code null}
     */
    public PlayerDTO toDTO() {
        return new PlayerDTO(
                nickname,
                colorPawn,
                myBuilds.stream().map(Card::toDTO).toList(),
                myVillage.getCharactersDTO());
    }

    /**
     * Converts this player's active bonus flags to a {@link PlayerStatusDTO}
     * for network transfer.
     *
     * @return a DTO representing the player's active bonuses; never {@code null}
     */
    public PlayerStatusDTO toStatusDTO() {
        return new PlayerStatusDTO(nickname, ppProtection, doubleShamanIncome, extraFoodOnQueue, paintBonus, categoryDiscounts, huntBonus);
    }

    /**
     * Converts this player's current resource totals to a {@link PlayerStatsDTO}
     * for network transfer.
     *
     * @return a DTO representing the player's current stats; never {@code null}
     */
    public PlayerStatsDTO toStatsDTO() {
        return new PlayerStatsDTO(nickname, nFood, pps, nStars, totBuildDisc, foodDiscount);
    }
}