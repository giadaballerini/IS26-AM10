package it.polimi.ingsw.model.entities.card.types.building;

public class Building {
    final private int ppValue;
    final private int foodCost;

    public  Building(int ppValue, int foodCost) {
        this.ppValue = ppValue;
        this.foodCost = foodCost;
    }

    public int getPpValue() {
        return ppValue;
    }

    public int getFoodCost() {
        return foodCost;
    }
}
