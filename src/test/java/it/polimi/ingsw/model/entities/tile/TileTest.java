package it.polimi.ingsw.model.entities.tile;

import it.polimi.ingsw.enumerations.ColorPawnEnum;
import it.polimi.ingsw.model.entities.pawn.Pawn;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TileTest {
    @Test
    void testShouldSetPawn(){
        Tile tile = new Tile('A', 2, null, null);
        Pawn pawn = new Pawn(null, ColorPawnEnum.BLUE);
        tile.setPawn(pawn);
        assertEquals(pawn, tile.getPawn());
    }

}