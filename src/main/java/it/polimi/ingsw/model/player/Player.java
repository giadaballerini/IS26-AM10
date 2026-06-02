package it.polimi.ingsw.model.player;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.ColorPawnEnum;
import it.polimi.ingsw.enumerations.CrafterSymbolEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.action.Action;
import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.entities.card.types.building.Building;
import it.polimi.ingsw.model.entities.card.types.character.Character;
import it.polimi.ingsw.network.dto.*;

import java.util.*;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Player {
    private int pps;
    private int nFood;
    private int nStars;
    private int totBuildDisc;
    private int foodDiscount;
    protected Village myVillage;
    private List<Building> myBuilds;
    private List<Action> skippableDraws;
    private final String nickname;
    private final ColorPawnEnum  colorPawn;

    private boolean huntBonus;
    private EnumSet<CardTypeEnum> categoryDiscounts;

    private boolean paintBonus;
    private boolean extraFoodOnQueue;
    private boolean ppProtection;
    private boolean doubleShamanIncome;


    public Player(String nickname, ColorPawnEnum colorPawn) {
        // si potrebbe anche fare cosi
        //this(nickname, colorPawn, 0, 0, 0, 0, 0, new Village(), new LinkedList<>(), false, false, false, false, false, false, false, false);
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

    @JsonCreator
    public Player(
            @JsonProperty("nickname")             String         nickname,
            @JsonProperty("colorPawn")            ColorPawnEnum  colorPawn,
            @JsonProperty("pps")                  int            pps,
            @JsonProperty("nFood")                int            nFood,
            @JsonProperty("nStars")               int            nStars,
            @JsonProperty("totBuildDisc")         int            totBuildDisc,
            @JsonProperty("foodDiscount")         int            foodDiscount,
            @JsonProperty("myVillage")            Village        myVillage,
            @JsonProperty("myBuilds")             List<Building> myBuilds,
            @JsonProperty("skippableDraws")       List<Action>   skippableDraws,
            @JsonProperty("ppProtection")         boolean        ppProtection,
            @JsonProperty("doubleShamanIncome")   boolean        doubleShamanIncome,
            @JsonProperty("huntBonus")            boolean        huntBonus,
            @JsonProperty("categoryDiscounts")    Set<CardTypeEnum> categoryDiscounts,
            @JsonProperty("paintBonus")           boolean        paintBonus,
            @JsonProperty("extraFoodOnQueue")     boolean        extraFoodOnQueue) {

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
    public int getPP() {
        return this.pps;
    }
    public int getNFood(){
        return this.nFood;
    }
    public int getNStars(){
        return this.nStars;
    }
    public int getTotBuildDisc(){
        return this.totBuildDisc;
    }
    public int getNumCharacters(){
        return myVillage.getNumCharacters();
    }
    public int getNumType(CardTypeEnum t){
        return myVillage.getNumType(t);
    }
    public int getFoodDiscount(){
        return this.foodDiscount;
    }
    public List<Building> getBuildings(){
        return this.myBuilds;
    }
    public void addCard(Character c) {
        c.dispatch(this.myVillage);
    }
    public void addFood(int food){
        this.nFood += food;
    }
    public void addBuilding(Building building){
        this.myBuilds.add(building);
    }

    public void addPP(int pp){
        this.pps += pp;
    }

    public void addFoodDiscount(int delta){
        this.foodDiscount += delta;
    }

    public int getNumSymbolsForCrafter(CrafterSymbolEnum c){
        return myVillage.getNumSymbolsForCrafter(c);
    }

    public void addStars(int starsAmount) {
        this.nStars += starsAmount;
    }



    public int getBuilderPoints(){
       return myVillage.builderPoints();
    }

    public int getTotSymbolsForCrafter(){
        int tot = 0;
        for(CrafterSymbolEnum s: CrafterSymbolEnum.values()){
            if(getNumSymbolsForCrafter(s) != 0)
                tot += 1;
        }
        return tot;
    }

    public List<Action> checkBuildsEffects(GamePhaseEnum currPhase){
        List<Action> actions = new ArrayList<>();
        for(Building b: myBuilds){
            b.execInstantEffect(this, currPhase);
            if(b.getTrigger() == currPhase) {
                List<Action> buildingActions = b.execInteractiveEffect(this);
                if (!buildingActions.isEmpty())
                    actions.addAll(buildingActions);
            }
        }
        return actions;
    }

    public void activateHuntBonus(){
        this.huntBonus = true;
    }

    public void applyHuntBonus(){
        if(huntBonus){
            int hunters = getNumType(CardTypeEnum.HUNTER);
            addPP(hunters);
            addFood(hunters);
        }
    }

    public void activatePaintBonus(){
        this.paintBonus = true;
    }

    public void applyPaintBonus(){
        if(paintBonus)
            addFood(getNumType(CardTypeEnum.PAINTER));
    }

    public  void activateExtraFoodOnQueue(){this.extraFoodOnQueue = true;}

    public void applyQueueFoodBonus(boolean tileHasFoodEffect){
        if(extraFoodOnQueue && tileHasFoodEffect)
            addFood(1);
    }

    public void activateDoubleShaman(){this.doubleShamanIncome = true;}

    public void applyRitualGain(int basePpGain) {
        addPP(doubleShamanIncome ? basePpGain * 2 : basePpGain);
    }

    public void activatePpProtection() { this.ppProtection = true; }

    public void applyRitualLoss(int ppLoss) {
        if (!ppProtection) addPP(-ppLoss);
    }

    public void addTotBuildDiscount(int disc){this.totBuildDisc += disc;}

    public String getNickname() {
        return this.nickname;
    }

    public void addCategoryDiscount(CardTypeEnum type) {
        categoryDiscounts.add(type);
    }

    public int calculateFeastDiscount() {
        int discount = foodDiscount;
        for (CardTypeEnum type : categoryDiscounts) {
            discount += getNumType(type);
        }
        return discount;
    }

    public void addSkippableDraws(List<Action> skippableDraws){
        this.skippableDraws.addAll(skippableDraws);
    }

    public boolean hasSkippableDraws(){
        return !(skippableDraws.isEmpty());
    }

    public List<Action> resolveSkippableDraws(){
        List<Action> resolved = new ArrayList<>(skippableDraws);
        skippableDraws.clear();
        return resolved;
    }

    public PlayerDTO toDTO(){
        return new  PlayerDTO(
                nickname,
                colorPawn,
                myBuilds.stream().map(Card::toDTO).toList(),
                myVillage.getCharactersDTO());
    }

    public PlayerStatusDTO toStatusDTO(){
        return new PlayerStatusDTO(nickname, ppProtection, doubleShamanIncome, extraFoodOnQueue, paintBonus,categoryDiscounts, huntBonus);
    }

    public PlayerStatsDTO toStatsDTO(){
        return new PlayerStatsDTO(nickname, nFood, pps, nStars, totBuildDisc, foodDiscount);
    }
}
