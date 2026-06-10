package it.polimi.ingsw.client.ui.TUI.utility;

import it.polimi.ingsw.client.VirtualModel;
import it.polimi.ingsw.client.ui.TUI.ViewTUI;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.network.dto.ActionsDTO;
import it.polimi.ingsw.network.dto.CardDTO;
import it.polimi.ingsw.network.dto.PlayerDTO;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.List;

/**
 * JLine {@link Completer} that provides context-aware tab-completion for TUI commands.
 * Available command completions are filtered based on the current game phase: pre-game
 * commands (such as {@code create}, {@code join}, and {@code choose}) are only suggested
 * when no match is in progress, while in-game commands (such as {@code move}, {@code draw},
 * and {@code skip}) are only suggested during the appropriate phase. Argument completions
 * are populated from the current {@link VirtualModel} state (available card IDs, unoccupied
 * tile positions, valid player counts, and so on).
 */
public class TUICompleter implements Completer {

    /** The TUI view used to access the current {@link VirtualModel} and game state. */
    private final ViewTUI tui;

    /**
     * Creates a new completer for the given TUI view.
     *
     * @param tui the TUI view to read game state from
     */
    public TUICompleter(ViewTUI tui) {
        this.tui = tui;
    }

    /**
     * Populates the candidate list based on the current word position in the input line.
     * If the cursor is on the first word, command names are suggested. If it is on the
     * second word, argument completions for the typed command are suggested.
     *
     * @param reader     the line reader requesting completion
     * @param line       the currently parsed input line
     * @param candidates the list to populate with completion candidates
     */
    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        int wordIndex = line.wordIndex();
        List<String> words = line.words();

        if (wordIndex == 0) {
            completeCommands(candidates);
            return;
        }

        if (wordIndex == 1) {
            String command = words.getFirst().toLowerCase();
            completeArgument(command, candidates);
        }
    }

    /**
     * Populates the candidate list with the command names available in the current game phase.
     * Commands {@code help} and {@code exit} are always available. Pre-game commands
     * ({@code create}, {@code join}, {@code choose}, {@code ranking}) are suggested only when
     * no match is in progress. In-game commands ({@code move}, {@code draw}, {@code skip},
     * {@code info}, {@code quit}) are suggested only during an active match, further filtered
     * by the current phase and the available draw actions.
     *
     * @param candidates the list to populate with command name candidates
     */
    private void completeCommands(List<Candidate> candidates) {
        VirtualModel model = tui.getModel();
        GamePhaseEnum phase = (model != null) ? model.getCurrentPhase() : null;

        candidates.add(new Candidate("help"));
        candidates.add(new Candidate("exit"));

        if (phase == null || phase == GamePhaseEnum.NONE) {
            candidates.add(new Candidate("create"));
            candidates.add(new Candidate("join"));
            candidates.add(new Candidate("choose"));
            candidates.add(new Candidate("ranking"));
        } else {
            candidates.add(new Candidate("info"));
            candidates.add(new Candidate("quit"));

            switch (phase) {
                case SETUP_PHASE:
                    candidates.add(new Candidate("move"));
                    break;

                case DRAW_PHASE: {
                    ActionsDTO actions = model.getToDoActions();
                    if (actions.getUpDraws() > 0 || actions.getDownDraws() > 0) {
                        candidates.add(new Candidate("draw"));
                    }
                    if (actions.isOptionalFlag()) {
                        candidates.add(new Candidate("skip"));
                    }
                    break;
                }

                case OPTIONAL_DRAW_PHASE: {
                    ActionsDTO actions = model.getToDoActions();
                    if (actions.getUpDraws() > 0 || actions.getDownDraws() > 0) {
                        candidates.add(new Candidate("draw"));
                    }
                    candidates.add(new Candidate("skip"));
                    break;
                }

                default:
                    break;
            }
        }
    }

    /**
     * Dispatches argument completion for the given command to the appropriate helper method.
     *
     * @param command    the command keyword typed by the player (lowercase)
     * @param candidates the list to populate with argument candidates
     */
    private void completeArgument(String command, List<Candidate> candidates) {
        VirtualModel model = tui.getModel();

        switch (command) {
            case "draw"   -> completeDraw(model, candidates);
            case "move"   -> completeMove(model, candidates);
            case "info"   -> completeInfo(model, candidates);
            case "create" -> completeCreate(candidates);
        }
    }

    /**
     * Populates the candidate list with the IDs of cards currently drawable from the
     * upper and lower lists, based on the remaining draw actions in the current turn.
     * If no targeted draws remain (e.g. during an optional draw phase), all visible
     * card IDs from both lists are suggested.
     *
     * @param model      the current virtual model; if {@code null}, no candidates are added
     * @param candidates the list to populate with drawable card ID candidates
     */
    private void completeDraw(VirtualModel model, List<Candidate> candidates) {
        if (model == null) return;
        ActionsDTO actions = model.getToDoActions();

        if (actions.getUpDraws() > 0 && model.getUpperList() != null) {
            for (CardDTO card : model.getUpperList()) {
                candidates.add(new Candidate(String.valueOf(card.getId())));
            }
        }
        if (actions.getDownDraws() > 0 && model.getLowerList() != null) {
            for (CardDTO card : model.getLowerList()) {
                candidates.add(new Candidate(String.valueOf(card.getId())));
            }
        }
        if (actions.getUpDraws() == 0 && actions.getDownDraws() == 0) {
            if (model.getUpperList() != null) {
                for (CardDTO card : model.getUpperList()) {
                    candidates.add(new Candidate(String.valueOf(card.getId())));
                }
            }
            if (model.getLowerList() != null) {
                for (CardDTO card : model.getLowerList()) {
                    candidates.add(new Candidate(String.valueOf(card.getId())));
                }
            }
        }
    }

    /**
     * Populates the candidate list with the 1-based positions of unoccupied board tiles,
     * suggesting only valid move targets during the setup phase.
     *
     * @param model      the current virtual model; if {@code null} or the board is not yet
     *                   available, no candidates are added
     * @param candidates the list to populate with tile position candidates
     */
    private void completeMove(VirtualModel model, List<Candidate> candidates) {
        if (model == null || model.getBoard() == null) return;
        int i = 1;
        for (var tile : model.getBoard()) {
            if (!tile.isOccupied()) {
                candidates.add(new Candidate(String.valueOf(i)));
            }
            i++;
        }
    }

    /**
     * Populates the candidate list with the IDs of all cards currently visible to the player:
     * cards in the upper and lower draw lists, and all character and building cards
     * held by every player in the match.
     *
     * @param model      the current virtual model; if {@code null}, no candidates are added
     * @param candidates the list to populate with card ID candidates
     */
    private void completeInfo(VirtualModel model, List<Candidate> candidates) {
        if (model == null) return;

        if (model.getUpperList() != null) {
            for (CardDTO card : model.getUpperList()) {
                candidates.add(new Candidate(String.valueOf(card.getId())));
            }
        }
        if (model.getLowerList() != null) {
            for (CardDTO card : model.getLowerList()) {
                candidates.add(new Candidate(String.valueOf(card.getId())));
            }
        }

        if (model.getPlayers() != null) {
            for (PlayerDTO player : model.getPlayers()) {
                if (player.getMyBuildings() != null) {
                    for (CardDTO card : player.getMyBuildings()) {
                        candidates.add(new Candidate(String.valueOf(card.getId())));
                    }
                }
                if (player.getMyCharacters() != null) {
                    for (CardDTO card : player.getMyCharacters()) {
                        candidates.add(new Candidate(String.valueOf(card.getId())));
                    }
                }
            }
        }
    }

    /**
     * Populates the candidate list with the valid player counts for game creation (2–5).
     *
     * @param candidates the list to populate with player count candidates
     */
    private void completeCreate(List<Candidate> candidates) {
        for (int n = 2; n <= 5; n++) {
            candidates.add(new Candidate(String.valueOf(n)));
        }
    }
}