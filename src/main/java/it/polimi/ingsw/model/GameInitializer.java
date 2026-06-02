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

public class GameInitializer {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final Logger LOG = Logger.getLogger(GameInitializer.class.getName());

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

            List<Card> allCards = MAPPER.readValue(is, new TypeReference<List<Card>>() {});
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

    public List<Card> initBuildingDeck(int numPlayers) {
        if (numPlayers < 2 || numPlayers > 5) {
            LOG.warning("Numero di giocatori non valido: " + numPlayers);
            return new ArrayList<>();
        }

        try (InputStream is = getClass().getResourceAsStream("/json/buildings.json")) {
            if (is == null)
                throw new IOException("File buildings.json non trovato nelle risorse");

            List<Card> buildings = MAPPER.readValue(is, new TypeReference<List<Card>>() {});

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

            List<Tile> allBoardTiles = MAPPER.readValue(is, new TypeReference<List<Tile>>() {});

            return new LinkedList<>(allBoardTiles.stream()
                    .filter(t -> t.getId() >= minId && t.getId() <= maxId)
                    .toList());

        } catch (IOException e) {
            LOG.severe("Errore nel setup della coda di gioco: " + e.getMessage());
            return new LinkedList<>();
        }
    }

    public ArrayList<Tile> initBoard(int numPlayers) {
        if (numPlayers < 2) {
            LOG.warning("Numero di giocatori non valido: " + numPlayers);
            return new ArrayList<>();
        }

        try (InputStream is = getClass().getResourceAsStream("/json/tiles.json")) {
            if (is == null)
                throw new IOException("File tiles.json non trovato nelle risorse");

            List<Tile> allTiles = MAPPER.readValue(is, new TypeReference<List<Tile>>() {});

            return new ArrayList<>(allTiles.stream()
                    .filter(t -> t.getMinPlayers() <= numPlayers)
                    .sorted(Comparator.comparing(Tile::getId))
                    .toList());

        } catch (IOException e) {
            LOG.severe("Errore nel setup della board di gioco: " + e.getMessage());
            return new ArrayList<>();
        }
    }

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
}