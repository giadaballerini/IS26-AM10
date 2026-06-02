package it.polimi.ingsw.network.dto;

import it.polimi.ingsw.enumerations.CardTypeEnum;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.Set;

public class PlayerStatusDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String nickname;
    private boolean huntBonus;
    private final Set<CardTypeEnum> categoryDiscounts;
    private boolean paintFlag;
    private boolean extraFlag;
    private boolean hasProtection;
    private boolean hasDoubleShamanIncome;

    public PlayerStatusDTO(String nickname, boolean hasProtection, boolean hasDoubleShamanIncome, boolean extraFlag, boolean paintFlag,
                           Set<CardTypeEnum> categoryDiscounts, boolean huntBonus){
        this.nickname = nickname;
        this.hasProtection = hasProtection;
        this.hasDoubleShamanIncome = hasDoubleShamanIncome;
        this.categoryDiscounts = Set.copyOf(categoryDiscounts);
        this.extraFlag = extraFlag;
        this.paintFlag = paintFlag;
        this.huntBonus = huntBonus;
    }

    public PlayerStatusDTO(String nickname){
        this.nickname = nickname;
        this.hasProtection = false;
        this.hasDoubleShamanIncome = false;
        this.categoryDiscounts = EnumSet.noneOf(CardTypeEnum.class);
        this.extraFlag = false;
        this.paintFlag = false;

    }

    public boolean isHuntBonus() {
        return huntBonus;
    }

    public boolean hasDiscountFor(CardTypeEnum type) {
        return categoryDiscounts.contains(type);
    }

    public Set<CardTypeEnum> getCategoryDiscounts() {
        return categoryDiscounts;
    }

    public boolean isPaintFlag() {
        return paintFlag;
    }

    public boolean isExtraFlag() {
        return extraFlag;
    }

    public boolean hasProtection() {
        return hasProtection;
    }

    public boolean hasDoubleShamanIncome() {
        return hasDoubleShamanIncome;
    }

    public String getNickname() {return nickname;}
}
