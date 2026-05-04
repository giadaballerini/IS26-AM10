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
    @JsonProperty("cost")
    private int cost = 0;
    @JsonProperty("PP")
    private int PP = 0;
    @JsonProperty("age")
    private int age;
    @JsonProperty("ppGain")
    private int ppGain = 0;
    @JsonProperty("ppLoss")
    private int ppLoss = 0;
    @JsonProperty("food")
    private int food = 0;
    @JsonProperty("thresh")
    private int thresh = 0;
    @JsonProperty("mark")
    private boolean mark = false;
    @JsonProperty("symbol")
    private CrafterSymbolEnum symbol = null;

    public int getId() { return id; }
    public CardTypeEnum getName() { return type; }
    public String getDescription() { return caption; }
    public int getCost() { return cost; }
    public int getPp() { return PP; }
    public int getAge() { return age; }
    public int getPpGain() { return ppGain; }
    public int getPpLoss() { return ppLoss; }
    public int getFood() { return food; }
    public int getThresh() { return thresh; }
    public boolean isMark() { return mark; }
    public CrafterSymbolEnum getSymbol() { return symbol; }

}
