package it.polimi.ingsw.model.entities.card.types.event;

public class StonePainting {
    private final int nPainter;
    private final int ppGain;
    private final int ppLoss;

    public StonePainting(int nPainter, int ppGain, int ppLoss){
        this.nPainter = nPainter;
        this.ppGain = ppGain;
        this.ppLoss = ppLoss;
    }
}
