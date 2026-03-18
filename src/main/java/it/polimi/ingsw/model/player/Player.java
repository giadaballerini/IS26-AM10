package it.polimi.ingsw.model.player;

import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.entities.card.types.building.Building;
import it.polimi.ingsw.model.entities.pawn.Pawn;

import javax.xml.stream.events.Characters;
import java.util.*;

public class Player {
    private int pps;
    private int nFood;
    private int nStars;
    private int totBuildDisc;
    private int foodDiscount;
    private Map<CardTypeEnum,List<Character>> myChars;
    private List<Building> myBuilds;
    private String nickname;
    private Pawn myPawn;
    private boolean hasProtection;

    public Player(String nickname, Pawn myPawn) {
        this.pps = 0;
        this.nFood = 0;
        this.nStars = 0;
        this.totBuildDisc = 0;
        this.foodDiscount = 0;
        this.myChars = new HashMap<CardTypeEnum,List<Character>>();
        this.myBuilds = new LinkedList<>();
        this.nickname = nickname;
        this.myPawn = myPawn;
        this.hasProtection = false;
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
        return myChars.values().size();
    }
    public int getNumType(CardTypeEnum t){
        return this.myChars.get(t).size();
    }
    public int getFoodDiscount(){
        return this.foodDiscount;
    }
    public List<Building> getBuildings(){
        return this.myBuilds;
    }
    public boolean isHasProtection(){
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
    public void addCard(CardTypeEnum t, Character c){
        List<Character> list = this.myChars.get(t);
        if(list == null){
            list = new ArrayList<Character>();
            myChars.put(t, list);
        }
        list.add(c);
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

}
