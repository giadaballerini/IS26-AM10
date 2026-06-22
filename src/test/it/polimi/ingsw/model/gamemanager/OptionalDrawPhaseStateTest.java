package it.polimi.ingsw.model.gamemanager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class OptionalDrawPhaseStateTest {

    private OptionalDrawPhaseState phase;
    private GameManager gameManager;

    @BeforeEach
    void setUp() {
        phase = new OptionalDrawPhaseState();
        gameManager = mock(GameManager.class);
    }

    @Test
    void nextPhase_WithEmptyToDoActions_TransitionsToEndRoundPhase() {
        when(gameManager.getToDoActions()).thenReturn(Collections.emptyList());

        GamePhaseState nextState = phase.nextPhase(gameManager);

        assertTrue(nextState instanceof EndRoundPhaseState);

        verify(gameManager).setSkippableDraw(false);
        verify(gameManager).nextPlayer();
    }

    @Test
    void nextPhase_WithPendingToDoActions_RemainsInOptionalDrawPhase() {
        when(gameManager.getToDoActions()).thenReturn(List.of(mock(it.polimi.ingsw.model.action.Action.class)));

        GamePhaseState nextState = phase.nextPhase(gameManager);

        assertSame(phase, nextState);

        verify(gameManager, never()).setSkippableDraw(anyBoolean());
        verify(gameManager, never()).nextPlayer();
    }

    @Test
    void onEntry_LoadsSkippableDraws() {
        phase.onEntry(gameManager);

        verify(gameManager).loadSkippableDraws();
    }
}