package it.polimi.ingsw.model.interfaces;

import it.polimi.ingsw.persistency.GameSnapshot;

public interface Snapshotable {
    GameSnapshot toSnapshot(int matchId);
}
