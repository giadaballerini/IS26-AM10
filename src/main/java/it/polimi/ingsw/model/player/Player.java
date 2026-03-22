package it.polimi.ingsw.model.player;

import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.CrafterSymbolEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.entities.card.types.building.Building;
import it.polimi.ingsw.model.entities.card.types.character.Builder;
import it.polimi.ingsw.model.entities.pawn.Pawn;
import it.polimi.ingsw.model.entities.card.types.character.Character;
import it.polimi.ingsw.model.interfaces.GainPPVisitor;

import javax.xml.stream.events.Characters;
import java.util.*;

public class Player {
    private int pps;
    private int nFood;
    private int nStars;
    private int totBuildDisc;
    private int foodDiscount;
    private Village myVillage;
    private List<Building> myBuilds;
    private String nickname;
    private Pawn myPawn;
    private boolean hasProtection;
    private boolean hasDoubleShamanIncome;

    public Player(String nickname, Pawn myPawn) {
        this.pps = 0;
        this.nFood = 0;
        this.nStars = 0;
        this.totBuildDisc = 0;
        this.foodDiscount = 0;
        this.myVillage = new Village();
        this.myBuilds = new LinkedList<>();
        this.nickname = nickname;
        this.myPawn = myPawn;
        this.hasProtection = false;
        this.hasDoubleShamanIncome = false;
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
    public void setPp(int pps){
        this.pps = pps;
    }
    public void setFood(int nFood){
        this.nFood = nFood;
    }
    public void setStars(int nStars){
        this.nStars = nStars;
    }
    public void addCard(Character c) {
        c.dispatch(this.myVillage);
    }
    public void addFood(int nFood){
        this.nFood += nFood;
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
    public Pawn getPawn(){
        return this.myPawn;
    }

    public void checkBuildsEffects(GamePhaseEnum currPhase){
        for(Building b: myBuilds){
            b.execInstantEffect(this, currPhase);
        }
    }

}
