package it.polimi.ingsw.network.dto;

import java.io.Serializable;

public class PlayerStatusDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String nickname;
    private boolean huntFlag;
    private boolean discountPainter;
    private boolean discountCrafter;
    private boolean discountGatherer;
    private boolean paintFlag;
    private boolean extraFlag;
    private boolean hasProtection;
    private boolean hasDoubleShamanIncome;

    public PlayerStatusDTO(String nickname, boolean hasProtection, boolean hasDoubleShamanIncome, boolean extraFlag, boolean paintFlag,
                           boolean discountPainter, boolean discountCrafter, boolean discountGatherer, boolean huntFlag){
        this.nickname = nickname;
        this.hasProtection = hasProtection;
        this.hasDoubleShamanIncome = hasDoubleShamanIncome;
        this.extraFlag = extraFlag;
        this.paintFlag = paintFlag;
        this.discountPainter = discountPainter;
        this.discountCrafter = discountCrafter;
        this.discountGatherer = discountGatherer;
        this.huntFlag = huntFlag;
    }

    public PlayerStatusDTO(String nickname){
        this.nickname = nickname;
        this.hasProtection = false;
        this.hasDoubleShamanIncome = false;
        this.extraFlag = false;
        this.paintFlag = false;
        this.discountPainter = false;
        this.discountCrafter = false;
        this.discountGatherer = false;
    }

    public boolean isHuntFlag() {
        return huntFlag;
    }

    public boolean isDiscountPainter() {
        return discountPainter;
    }

    public boolean isDiscountCrafter() {
        return discountCrafter;
    }

    public boolean isDiscountGatherer() {
        return discountGatherer;
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
