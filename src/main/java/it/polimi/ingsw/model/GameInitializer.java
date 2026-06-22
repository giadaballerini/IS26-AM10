package it.polimi.ingsw.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.entities.tile.Tile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Responsible for initializing all game components at the start of a match.
 *
 * <p>Each method loads data from a JSON resource file and returns the
 * corresponding collection ready for use by the game engine. The size and
 * composition of each collection depend on the number of players.</p>
 */
public class GameInitializer {

    /**
     * Shared Jackson mapper configured to accept single values as arrays
     * and to ignore unknown properties during deserialization.
     */
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /**
     * Logger for this class.
     */
    private static final Logger LOG = Logger.getLogger(GameInitializer.class.getName());

    /**
     * Initializes and returns the main card deck for the given number of players.
     *
     * <p>Cards are loaded from {@code /json/tribes.json} and trimmed to the
     * appropriate deck size. They are then split by age, shuffled within each
     * age, and reassembled in order (age 1 → age 2 → age 3 → final events).</p>
     *
     * @param numPlayers number of players in the match (2–5)
     * @return the shuffled deck ordered by age; an empty list if
     *         {@code numPlayers} is invalid or the resource cannot be loaded
     */
    public List<Card> initDeck(int numPlayers) {
        int deckSize = switch (numPlayers) {
            case 2 -> 63;
            case 3 -> 74;
            case 4 -> 85;
            case 5 -> 96;
            default -> 0;
        };

        if (deckSize == 0)
            return List.of();

        try (InputStream is = getClass().getResourceAsStream("/json/tribes.json")) {
            if (is == null)
                throw new IOException("File tribes.json non trovato nelle risorse");

            List<Card> allCards = MAPPER.readValue(is, new TypeReference<>() {
            });
            List<Card> deck = allCards.subList(0, deckSize);

            List<Card> age1 = new ArrayList<>(deck.stream()
                    .filter(c -> c.getAge() == 1).toList());
            List<Card> age2 = new ArrayList<>(deck.stream()
                    .filter(c -> c.getAge() == 2).toList());
            List<Card> age3 = new ArrayList<>(deck.stream()
                    .filter(c -> c.getAge() == 3 && !c.getTrigger().equals(GamePhaseEnum.PLAY_EVENT)).toList());
            List<Card> finalEvents = new ArrayList<>(deck.stream()
                    .filter(c -> c.getTrigger().equals(GamePhaseEnum.PLAY_EVENT)).toList());

            Collections.shuffle(age1);
            Collections.shuffle(age2);
            Collections.shuffle(age3);
            Collections.shuffle(finalEvents);

            List<Card> finalDeck = new ArrayList<>();
            finalDeck.addAll(age1);
            finalDeck.addAll(age2);
            finalDeck.addAll(age3);
            finalDeck.addAll(finalEvents);

            return finalDeck;

        } catch (IOException e) {
            LOG.severe("Errore nel setup del mazzo: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Initializes and returns the building card deck for the given number of players.
     *
     * <p>Cards are loaded from {@code /json/buildings.json}, shuffled within
     * each age, and trimmed to the number of buildings appropriate for the
     * player count.</p>
     *
     * @param numPlayers number of players in the match (2–5)
     * @return the selected building cards ordered by age; an empty list if
     *         {@code numPlayers} is invalid or the resource cannot be loaded
     */
    public List<Card> initBuildingDeck(int numPlayers) {
        if (numPlayers < 2 || numPlayers > 5) {
            LOG.warning("Numero di giocatori non valido: " + numPlayers);
            return new ArrayList<>();
        }

        try (InputStream is = getClass().getResourceAsStream("/json/buildings.json")) {
            if (is == null)
                throw new IOException("File buildings.json non trovato nelle risorse");

            List<Card> buildings = MAPPER.readValue(is, new TypeReference<>() {
            });

            List<Card> age1Buildings = buildings.stream().filter(c -> c.getAge() == 1).collect(Collectors.toList());
            List<Card> age2Buildings = buildings.stream().filter(c -> c.getAge() == 2).collect(Collectors.toList());
            List<Card> age3Buildings = buildings.stream().filter(c -> c.getAge() == 3).collect(Collectors.toList());

            Collections.shuffle(age1Buildings);
            Collections.shuffle(age2Buildings);
            Collections.shuffle(age3Buildings);

            int count1, count2, count3;
            switch (numPlayers) {
                case 2: count1 = 1; count2 = 2; count3 = 3; break;
                case 3: count1 = 2; count2 = 2; count3 = 4; break;
                case 4: count1 = 2; count2 = 3; count3 = 4; break;
                case 5: count1 = 2; count2 = 3; count3 = 5; break;
                default: count1 = 0; count2 = 0; count3 = 0;
            }

            List<Card> finalBuildings = new ArrayList<>();
            finalBuildings.addAll(age1Buildings.subList(0, Math.min(count1, age1Buildings.size())));
            finalBuildings.addAll(age2Buildings.subList(0, Math.min(count2, age2Buildings.size())));
            finalBuildings.addAll(age3Buildings.subList(0, Math.min(count3, age3Buildings.size())));

            return finalBuildings;

        } catch (IOException e) {
            LOG.severe("Errore nel setup degli edifici: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Initializes and returns the tile queue for the given number of players.
     *
     * <p>Tiles are loaded from {@code /json/qtiles.json} and filtered by ID
     * range according to the player count. The result is returned as a
     * {@link LinkedList} to support efficient removal from the front.</p>
     *
     * @param numPlayers number of players in the match (2–5)
     * @return the tile queue as a {@link LinkedList}; an empty list if
     *         {@code numPlayers} is invalid or the resource cannot be loaded
     */
    public List<Tile> initQueue(int numPlayers) {
        int minId = switch (numPlayers) {
            case 2 -> 0;
            case 3 -> 2;
            case 4 -> 5;
            case 5 -> 9;
            default -> -1;
        };

        int maxId = switch (numPlayers) {
            case 2 -> 1;
            case 3 -> 4;
            case 4 -> 8;
            case 5 -> 13;
            default -> -1;
        };

        if (minId == -1) {
            LOG.severe("Numero di giocatori non valido: " + numPlayers);
            return new LinkedList<>();
        }

        try (InputStream is = getClass().getResourceAsStream("/json/qtiles.json")) {
            if (is == null)
                throw new IOException("File qtiles.json non trovato nelle risorse");

            List<Tile> allBoardTiles = MAPPER.readValue(is, new TypeReference<>() {
            });

            return new LinkedList<>(allBoardTiles.stream()
                    .filter(t -> t.getId() >= minId && t.getId() <= maxId)
                    .toList());

        } catch (IOException e) {
            LOG.severe("Errore nel setup della coda di gioco: " + e.getMessage());
            return new LinkedList<>();
        }
    }

    /**
     * Initializes and returns the game board tiles for the given number of players.
     *
     * <p>Tiles are loaded from {@code /json/tiles.json}, filtered to include only
     * those whose minimum player requirement is met, and sorted by ID.</p>
     *
     * @param numPlayers number of players in the match (2-5)
     * @return the board tiles sorted by ID; an empty list if
     *         {@code numPlayers} is invalid or the resource cannot be loaded
     */
    public ArrayList<Tile> initBoard(int numPlayers) {
        if (numPlayers < 2) {
            LOG.warning("Numero di giocatori non valido: " + numPlayers);
            return new ArrayList<>();
        }

        try (InputStream is = getClass().getResourceAsStream("/json/tiles.json")) {
            if (is == null)
                throw new IOException("File tiles.json non trovato nelle risorse");

            List<Tile> allTiles = MAPPER.readValue(is, new TypeReference<>() {
            });

            return new ArrayList<>(allTiles.stream()
                    .filter(t -> t.getMinPlayers() <= numPlayers)
                    .sorted(Comparator.comparing(Tile::getId))
                    .toList());

        } catch (IOException e) {
            LOG.severe("Errore nel setup della board di gioco: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Populates the upper card list by drawing from the deck and the building deck.
     *
     * <p>Cards are drawn from {@code deck} until the upper list reaches
     * {@code numPlayers + 4} entries, then a fixed number of building cards
     * (depending on {@code numPlayers}) are appended.</p>
     *
     * @param deck       the main card deck to draw from; modified in place
     * @param buildings  the building card deck to draw from; modified in place
     * @param upperList  the list to populate; modified in place
     * @param numPlayers number of players in the match (2–5)
     * @return the populated {@code upperList}; an empty list if
     *         {@code numPlayers} is invalid
     */
    public List<Card> initUpperList(List<Card> deck, List<Card> buildings, List<Card> upperList, int numPlayers) {
        int buildingsToAdd = switch (numPlayers) {
            case 2 -> 1;
            case 3, 4, 5 -> 2;
            default -> 0;
        };

        if (buildingsToAdd == 0) {
            LOG.severe("Numero di giocatori non valido: " + numPlayers);
            return new ArrayList<>();
        }

        int cardsToAdd = numPlayers + 4;
        for (int i = upperList.size(); i < cardsToAdd && !deck.isEmpty(); i++) {
            upperList.add(deck.removeFirst());
        }
        for (int i = 0; i < buildingsToAdd; i++) {
            upperList.add(buildings.removeFirst());
        }

        return upperList;
    }

    /**
     * Populates and returns the lower card list by drawing from the deck.
     *
     * <p>Cards are drawn one at a time until the lower list reaches
     * {@code numPlayers + 1} entries. Event cards encountered during drawing
     * are redirected to {@code upperList} rather than added to the lower list.</p>
     *
     * @param deck       the main card deck to draw from; modified in place
     * @param upperList  the upper list, which receives any event cards drawn;
     *                   modified in place
     * @param numPlayers number of players in the match
     * @return the populated lower list; an empty list if the deck is empty
     */
    public List<Card> initLowerList(List<Card> deck, List<Card> upperList, int numPlayers) {
        if (deck.isEmpty()) {
            LOG.severe("Numero di giocatori non valido o partita non inizializzata correttamente!");
            return new ArrayList<>();
        }

        List<Card> lowerList = new ArrayList<>();
        int size = numPlayers + 1;

        while (lowerList.size() < size && !deck.isEmpty()) {
            Card card = deck.removeFirst();
            if (card.getType().isEvent())
                upperList.add(card);
            else
                lowerList.add(card);
        }

        return lowerList;
    }
    /**
     * Creates a new {@code GameInitializer} instance.
     */
    public GameInitializer() {
    }
}