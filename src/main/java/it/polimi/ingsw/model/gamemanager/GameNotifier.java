package it.polimi.ingsw.model.gamemanager;

import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.network.dto.*;
import it.polimi.ingsw.observer.ModelObserver;

import java.util.List;
import java.util.Map;

public class GameNotifier {
    final List<ModelObserver> listeners;

    public GameNotifier(List<ModelObserver> listeners) {
        this.listeners = listeners;
    }

    public void notifyCurrPlayerUpdate(String currPlayer){
        for(ModelObserver c: listeners){
            c.onCurrPlayerUpdate(currPlayer);
        }
    }


    public void notifyMoveUpdate(TileDTO tdto, String currPlayer){

        for(ModelObserver c: listeners){
            c.onMoveUpdate(tdto, currPlayer);
        }
    }

     void notifyPhaseUpdate(GamePhaseState currPhaseState){
        PhaseDTO phaseDTO = new PhaseDTO(currPhaseState.getPhase());
        for (ModelObserver c: listeners){
            c.onPhaseUpdate(phaseDTO);
        }
    }


     void notifyGameEnding(List<PlayerStatsDTO> statsList,Map<String, Integer> globalPositions, Map<String, Integer> rankingPositions){
         for (ModelObserver listener : listeners) {
             int rankingPos = rankingPositions.getOrDefault(listener.getNickname(), -1);
             int globalPos = globalPositions.getOrDefault(listener.getNickname(), -1);
             listener.onGameEnding(statsList, rankingPos, globalPos);
         }
    }

     void notifyDrawUpdate(Player currPlayer, Card card){
        CardDTO cardDTO = card.toDTO();
        for (ModelObserver c : listeners){
            c.onDrawUpdate(cardDTO, currPlayer.getNickname());
        }
    }

     void notifyStatusUpdate(Player currPlayer){
        PlayerStatusDTO status = currPlayer.toStatusDTO();
        for (ModelObserver c : listeners){
            c.onStatusUpdate(status);
        }
    }

    void notifyStatsUpdate(Player currPlayer, Card card){
        PlayerStatsDTO statsDto = currPlayer.toStatsDTO();
        for(ModelObserver c: listeners){
            c.onStatsUpdate(statsDto,card.getId());
        }
    }

    void notifyEventUpdate(EventDTO events){
        for(ModelObserver c: listeners) {
            c.onEvent(events);
        }
    }


    void showBoard(BoardDTO dto){
        for(ModelObserver c: listeners){
            c.showBoard(dto);
        }
    }

    void notifySkip(Player currPlayer){
        for(ModelObserver c: listeners){
            c.notifySkip(currPlayer.getNickname());
        }
    }

    void notifyDrawable(ActionsDTO dto, String currPlayer){
        for(ModelObserver c : listeners){
            if(c.getNickname().equals(currPlayer))
                c.notifyDrawable(dto);
            else
                c.notifyDrawable(new ActionsDTO(0, 0, false));
        }

    }

    void notifyReturnToQueue(PlayerStatsDTO statsDTO, TileDTO t){
        for(ModelObserver c: listeners){
            c.onReturnToQueue(t, statsDTO);
        }
    }

    void notifyChangeAge(ChangeAgeDTO ageDTO){
        for(ModelObserver c: listeners){
            c.onChangeAge(ageDTO);
        }
    }

}
