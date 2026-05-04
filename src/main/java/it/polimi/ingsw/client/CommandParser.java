package it.polimi.ingsw.client;

import it.polimi.ingsw.exceptions.InvalidLobbySizeException;
import it.polimi.ingsw.exceptions.InvalidTimingException;
import it.polimi.ingsw.client.commands.*;

public class CommandParser {
    public static Command parse(String input, Client client) throws InvalidLobbySizeException, InvalidTimingException {
        String[] parts = input.toLowerCase().split(" ");
        if (parts.length == 0) return null;

        String action = parts[0];
        try {
            return switch (action) {
                case "move" -> new MoveCommand(Integer.parseInt(parts[1]));
                case "create" -> {
                    int numPlayers = Integer.parseInt(parts[1]);
                    if (numPlayers > 5 || numPlayers < 2)
                        throw new InvalidLobbySizeException("Numero di giocatori non valido");
                    yield new CreateGameCommand(numPlayers);
                }
                case "join" -> new ShowLobbiesCommand();
                case "choose" -> {
                    if (!client.hasAvailableLobbies()) {
                        System.out.println("Errore: Non puoi scegliere una partita se non ce ne sono di disponibili.");
                        System.out.println("Digita prima 'join' per aggiornare la lista delle partite.");
                        yield null;
                    }
                    yield new JoinGameCommand(Integer.parseInt(parts[1]));
                }
                case "draw" -> new DrawCardCommand(Integer.parseInt(parts[1]));
                case "skip" -> new SkipDrawCommand();
                case "quit" -> new QuitCommand();
                case "exit" -> new ExitCommand();
                case "help" -> new HelpCommand();
                case "info" -> new InfoCommand(Integer.parseInt(parts[1]));
                default -> null;
            };
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            System.out.println("Formato comando errato. Esempio: 'choose 1' o 'join'");
            return null;
        }
    }
}