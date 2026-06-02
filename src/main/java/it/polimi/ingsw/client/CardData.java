package it.polimi.ingsw.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.CrafterSymbolEnum;

public class CardData {
    @JsonProperty("id")
    private int id;
    @JsonProperty("type")
    private CardTypeEnum type;
    @JsonProperty("caption")
    private String caption;
    @JsonProperty("age")
    private int age;
    @JsonProperty("cost")
    private Integer cost = null;
    @JsonProperty("PP")
    private Integer PP = null;

    @JsonProperty("ppGain")
    private Integer ppGain = null;

    @JsonProperty("ppLoss")
    private Integer ppLoss = null;

    @JsonProperty("food")
    private Integer food = null;

    @JsonProperty("thresh")
    private Integer thresh = null;

    @JsonProperty("mark")
    private Boolean mark = null;

    @JsonProperty("symbol")
    private CrafterSymbolEnum symbol = null;

    @JsonProperty("foodDiscount")
    private Integer foodDiscount = null;

    public int getId()                  { return id; }
    public CardTypeEnum getType()       { return type; }
    public String getDescription()      { return caption; }
    public int getAge()                 { return age; }

    public int getCost()                { return cost != null ? cost : 0; }
    public int getPp()                  { return PP != null ? PP : 0; }
    public int getPpGain()              { return ppGain != null ? ppGain : 0; }
    public int getPpLoss()              { return ppLoss != null ? ppLoss : 0; }
    public int getFood()                { return food != null ? food : 0; }
    public int getThresh()              { return thresh != null ? thresh : 0; }
    public boolean isMark()             { return mark != null && mark; }
    public CrafterSymbolEnum getSymbol(){ return symbol; }
    public int getFoodDiscount()        { return foodDiscount != null ? foodDiscount : 0; }

    public String getBackImagePath(){
        if(age == 3 && (type.equals(CardTypeEnum.FEAST) || type.equals(CardTypeEnum.RITUAL))){
            return "Back_card_final";
        } else if(type.equals(CardTypeEnum.BUILDING)){
            return "Back_build_" + age;
        } else
            return "Back_card_" + age;
    }

}
