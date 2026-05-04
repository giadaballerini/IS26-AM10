package it.polimi.ingsw.network.dto;

import java.io.Serializable;

public class ActionsDTO implements Serializable {
    int upDraws;
    int downDraws;
    boolean skippable;


    public ActionsDTO(int upDraws, int downDraws, boolean skippable) {
        this.upDraws = upDraws;
        this.downDraws = downDraws;
        this.skippable = skippable;
    }

    public ActionsDTO(ActionsDTO a){
        this.upDraws = a.upDraws;
        this.downDraws = a.downDraws;
        this.skippable = a.skippable;
    }

    public int getUpDraws() {
        return upDraws;
    }

    public int getDownDraws() {
        return downDraws;
    }

    public boolean isOptionalFlag() {
        return skippable;
    }
}
