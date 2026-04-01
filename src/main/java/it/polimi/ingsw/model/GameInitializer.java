package it.polimi.ingsw.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.entities.tile.Tile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;


public class GameInitializer {


    public List<Card> initDeck(int numPlayers) {
        int deckSize = switch (numPlayers) {
            case 2 -> 63;
            case 3 -> 74;
            case 4 -> 85;
            case 5 -> 96;
            default -> 0;
        };

        if (deckSize != 0) {

            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            try (InputStream is = getClass().getClassLoader().getResourceAsStream("tribes.json")) {
                if (is == null) {
                    throw new IOException("File tribes.json non trovato nelle risorse");
                }
                List<Card> allCards = mapper.readValue(is, new TypeReference<List<Card>>() {});

                List<Card> deck = allCards.subList(0, deckSize);

                List<Card> age1 = new ArrayList<>(deck.stream()
                        .filter(c -> c.getAge() == 1).toList());

                List<Card> age2 = new ArrayList<>(deck.stream().filter(c -> c.getAge() == 2).toList());

                List<Card> age3 = new ArrayList<>(deck.stream().filter(c -> c.getAge() == 3).toList());

                Collections.shuffle(age1);
                Collections.shuffle(age2);
                Collections.shuffle(age3);

                List<Card> finalDeck = new ArrayList<>();

                finalDeck.addAll(age1);
                finalDeck.addAll(age2);
                finalDeck.addAll(age3);

                return finalDeck;

            } catch (IOException e) {
                System.err.println("Errore nel setup del mazzo: " + e.getMessage());
                return List.of();
            }
        }
        else
            return List.of();
    }

    public List<Card> initBuildingDeck(int numPlayers){
        if(numPlayers >= 2) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            try (InputStream is = getClass().getClassLoader().getResourceAsStream("buildings.json")) {
                if (is == null) {
                    throw new IOException("File buildings.json non trovato nelle risorse");
                }

                List<Card> buildings = new ArrayList<>(mapper.readValue(is, new TypeReference<List<Card>>() {
                }));

                List<Card> age1Buildings = new ArrayList<Card>(buildings.stream()
                        .filter(c -> c.getAge() == 1).toList());

                List<Card> age2Buildings = new ArrayList<Card>(buildings.stream().filter(c -> c.getAge() == 2).toList());

                List<Card> age3Buildings = new ArrayList<Card>(buildings.stream().filter(c -> c.getAge() == 3).toList());

                Collections.shuffle(age1Buildings);
                Collections.shuffle(age2Buildings);
                Collections.shuffle(age3Buildings);

                List<Card> finalBuildings = new ArrayList<>();

                finalBuildings.addAll(age1Buildings);
                finalBuildings.addAll(age2Buildings);
                finalBuildings.addAll(age3Buildings);


                return finalBuildings;

            } catch (IOException e) {
                System.err.println("Errore nel setup degli edifici: " + e.getMessage());
                return new ArrayList<>();
            }
        }
        else {
            System.err.println("Numero di giocatori non valido: " + numPlayers);
            return new ArrayList<>();
        }
    }

    public Queue<Tile> initQueue(int numPlayers) {

        int minId = switch(numPlayers) {
            case 2 -> 0;
            case 3 -> 2;
            case 4 -> 5;
            case 5 -> 9;
            default -> -1;
        };

        int maxId =  switch(numPlayers) {
            case 2 -> 1;
            case 3 -> 4;
            case 4 -> 8;
            case 5 -> 13;
            default -> -1;
        };

        if (minId == -1) {
            System.err.println("Numero di giocatori non valido: " + numPlayers);
            return new LinkedList<>();
        }
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("qtiles.json")) {
            if (is == null) {
                throw new IOException("File qtiles.json non trovato nelle risorse");
            }

            List<Tile> allBoardTiles = mapper.readValue(is, new TypeReference<List<Tile>>() {});

            return new LinkedList<>(allBoardTiles.stream()
                    .filter(t -> t.getId() >= minId && t.getId() <= maxId)
                    .toList());


        } catch (IOException e) {
            System.err.println("Errore nel setup della coda di gioco: " + e.getMessage());
            return new LinkedList<>();
        }

    }

    public ArrayList<Tile> initBoard(int numPlayers) {
        if(numPlayers >= 2) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            try (InputStream is = getClass().getClassLoader().getResourceAsStream("tiles.json")) {
                if (is == null) {
                    throw new IOException("File tiles.json non trovato nelle risorse");
                }

                List<Tile> allTiles = mapper.readValue(is, new TypeReference<List<Tile>>() {
                });

                return new ArrayList<>(allTiles.stream()
                        .filter(t -> t.getMinPlayers() <= numPlayers)
                        .sorted(Comparator.comparing(Tile::getId))
                        .toList());


            } catch (IOException e) {
                System.err.println("Errore nel setup della board di gioco: " + e.getMessage());
                return new ArrayList<>();
            }
        }
        else {
            System.err.println("Numero di giocatori non valido: " + numPlayers);
            return new ArrayList<>();
        }
    }
    public List<Card> initUpperList(List<Card> deck,List<Card> buildings, List<Card> upperList, int numPlayers) {


        int buildingsToAdd = switch (numPlayers) {
            case 2 -> 1;
            case 3, 4 -> 2;
            case 5 -> 3;
            default -> 0;
        };

        if(buildingsToAdd > 0) {
            int cardsToAdd = numPlayers + 4;

            for (int i = upperList.size(); i < cardsToAdd && !deck.isEmpty(); i++) {
                upperList.add(deck.removeFirst());
            }

            for (int i = 0; i < buildingsToAdd; i++) {
                upperList.add(buildings.removeFirst());
            }
            return upperList;
        }
        else{
            System.err.println("Numero di giocatori non valido: " + numPlayers);
            return new ArrayList<>();
        }
    }

    public List<Card> initLowerList(List<Card> deck,List<Card> upperList, int numPlayers) {
        if(!deck.isEmpty()) {
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
        else{
            System.err.println("Numero di giocatori non valido o partita non inizializzata correttamente!");
            return new ArrayList<>();
        }
    }
}