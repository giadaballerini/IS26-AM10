package it.polimi.ingsw.client.ui.TUI.utility;

import it.polimi.ingsw.client.Client;
import it.polimi.ingsw.client.commands.*;
import it.polimi.ingsw.exceptions.InvalidLobbySizeException;
import it.polimi.ingsw.exceptions.InvalidTimingException;

/**
 * Utility class that parses raw TUI input strings into {@link Command} instances.
 * Each recognized keyword maps to the corresponding command class; unrecognized
 * keywords or malformed arguments return {@code null} and print an error message.
 */
public class CommandParser {

    /**
     * Parses the given input string and returns the corresponding {@link Command},
     * or {@code null} if the input is unrecognized or malformed.
     *
     * <p>Recognized commands:</p>
     * <ul>
     *   <li>{@code move <n>} — moves the player's pawn to tile {@code n}</li>
     *   <li>{@code create <n>} — creates a new game with {@code n} players (2–5)</li>
     *   <li>{@code join} — requests the list of available lobbies</li>
     *   <li>{@code choose <id>} — joins the lobby with the given ID (only after {@code join})</li>
     *   <li>{@code draw <id>} — draws the card with the given ID</li>
     *   <li>{@code skip} — skips the optional draw action</li>
     *   <li>{@code status} — displays the status of all players</li>
     *   <li>{@code quit} — disconnects and terminates the application</li>
     *   <li>{@code exit} — exits the current match</li>
     *   <li>{@code help} — displays the help screen</li>
     *   <li>{@code info <id>} — displays the details of the card with the given ID</li>
     *   <li>{@code ranking} — displays the global leaderboard</li>
     * </ul>
     *
     * @param input  the raw input string typed by the player
     * @param client the client instance, used to validate context-dependent commands
     *               such as {@code choose}
     * @return the parsed {@link Command}, or {@code null} if the input is unrecognized,
     *         malformed, or cannot be executed in the current context
     * @throws InvalidLobbySizeException if {@code create} is called with a player count
     *                                   outside the valid range (2–5)
     * @throws InvalidTimingException    if a command is issued at an invalid phase
     *                                   (propagated from the client)
     */
    public static Command parse(String input, Client client) throws InvalidLobbySizeException, InvalidTimingException {
        String[] parts = input.toLowerCase().split(" ");
        if (parts.length == 0) return null;

        String action = parts[0];
        try {
            return switch (action) {
                case "move"    -> new MoveCommand(Integer.parseInt(parts[1]));
                case "create"  -> {
                    int numPlayers = Integer.parseInt(parts[1]);
                    if (numPlayers > 5 || numPlayers < 2)
                        throw new InvalidLobbySizeException("Numero di giocatori non valido");
                    yield new CreateGameCommand(numPlayers);
                }
                case "join"    -> new ShowLobbiesCommand();
                case "choose"  -> {
                    if (!client.hasAvailableLobbies()) {
                        System.out.println("Errore: Non puoi scegliere una partita se non ce ne sono di disponibili.");
                        System.out.println("Digita prima 'join' per aggiornare la lista delle partite.");
                        yield null;
                    }
                    yield new JoinGameCommand(Integer.parseInt(parts[1]));
                }
                case "draw"    -> new DrawCardCommand(Integer.parseInt(parts[1]));
                case "skip"    -> new SkipDrawCommand();
                case "status"  -> new StatusCommand();
                case "quit"    -> new QuitCommand();
                case "exit"    -> new ExitCommand();
                case "help"    -> new HelpCommand();
                case "info"    -> new InfoCommand(Integer.parseInt(parts[1]));
                case "ranking" -> new RankingRequestCommand();
                default        -> null;
            };
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            System.out.println("Formato comando errato. Esempio: 'choose 1' o 'join'");
            return null;
        }
    }
}