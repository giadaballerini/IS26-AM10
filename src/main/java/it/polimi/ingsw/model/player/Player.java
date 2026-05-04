package it.polimi.ingsw.model.player;

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

public class Player {
    private int pps;
    private int nFood;
    private int nStars;
    private int totBuildDisc;
    private int foodDiscount;
    protected Village myVillage;
    private List<Building> myBuilds;
    private final String nickname;
    private boolean hasProtection;
    private boolean hasDoubleShamanIncome;
    private ColorPawnEnum  colorPawn;
    private boolean huntFlag;
    private boolean discountPainter;
    private boolean discountCrafter;
    private boolean discountGatherer;
    private boolean paintFlag;
    private boolean extraFlag;


    public Player(String nickname, ColorPawnEnum colorPawn) {
        this.pps = 0;
        this.nFood = 0;
        this.nStars = 0;
        this.totBuildDisc = 0;
        this.foodDiscount = 0;
        this.myVillage = new Village();
        this.myBuilds = new LinkedList<>();
        this.nickname = nickname;
        this.hasProtection = false;
        this.hasDoubleShamanIncome = false;
        this.colorPawn = colorPawn;
        this.huntFlag = false;
        this.discountPainter = false;
        this.discountCrafter = false;
        this.discountGatherer = false;
        this.paintFlag = false;
        this.extraFlag = false;
    }

    public int getPP(){
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
    public boolean getHasProtection(){
        return this.hasProtection;
    }
    public void addCard(Character c) {
        c.dispatch(this.myVillage);
    }
    public void addFood(int nFood){
        this.nFood += nFood;
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

    public void addProtection(){
        hasProtection = true;
    }

    public void addDouble(){
        hasDoubleShamanIncome = true;
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

            List<Action> buildingActions = b.execInteractiveEffect(this);
            if(!buildingActions.isEmpty())
                actions.addAll(buildingActions);
        }
        return actions;
    }

    public void setHuntFlag(boolean flag){
        this.huntFlag = flag;
    }

    public boolean hasHuntFlag(){return huntFlag;}

    public boolean getHasDoubleShamanIncome(){return hasDoubleShamanIncome;}

    public void addTotBuildDiscount(int disc){this.totBuildDisc += disc;}

    public String getNickname() {
        return this.nickname;
    }

    public void setDiscountGatherer(boolean flag){
        this.discountGatherer = flag;
    }

    public void setDiscountCrafter(boolean flag){
        this.discountCrafter = flag;
    }

    public void setDiscountPainter(boolean flag){
        this.discountPainter = flag;
    }

    public void setPaintFlag(boolean flag){
        this.paintFlag = flag;
    }

    public void setExtraFlag(boolean flag){this.extraFlag = flag;}

    public boolean hasExtraFlag(){return this.extraFlag;}

    public boolean hasPaintFlag(){return this.paintFlag;}
    public boolean hasDiscountGatherer(){return this.discountGatherer;}
    public boolean hasDiscountPainter(){return this.discountPainter;}
    public boolean hasDiscountCrafter(){return this.discountCrafter;}

    public PlayerDTO toDTO(){
        return new  PlayerDTO(
                nickname,
                colorPawn,
                myBuilds.stream().map(Card::toDTO).toList(),
                myVillage.getCharactersDTO());
    }

    public PlayerStatusDTO toStatusDTO(){
        return new PlayerStatusDTO(nickname, hasProtection, hasDoubleShamanIncome, extraFlag, paintFlag, discountPainter, discountCrafter, discountGatherer, huntFlag);
    }

    public PlayerStatsDTO toStatsDTO(){
        return new PlayerStatsDTO(nickname, nFood, pps, nStars);
    }
}
