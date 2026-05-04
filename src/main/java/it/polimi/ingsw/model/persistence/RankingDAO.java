package it.polimi.ingsw.model.persistence;

import it.polimi.ingsw.exceptions.WrongPasswordException;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

import java.sql.*;
import java.sql.Date;
import java.util.*;

public class RankingDAO {
    // todo: classe che gestisce interazione db
    /*Classifica partite su DB: Il server mantiene, su un data base esterno (mysql o postgresql), lo
    storico del risultato delle partite giocate. Di ogni giocatore che ha completato una partita si salvano
    il nickname, il punteggio finale, la data in cui si è volta la partita e il numero di giocatori della
    partita (il punteggio è influenzato dal numero di giocatori della partita). A fine partita il client riporta
    la posizione del giocatore nella classifica di tutte le partite con il medesimo numero di giocatori e
    permette di visualizzare la classifica completa per le partite con quel numero di giocatori. */
    // Per farlo funzionare avremo bisogno di due metodi, uno per inserire la partita e uno per fare la query per estrarre la classifica.
    // SE IMPLEMENTIAMO login, dobbiamo trovare hash function e aggiungere due metodi, uno per registrare l'utente e l'altro per verificare autenticazione
    // tabella GlobalRanking: nickname, posizione, punti, ngiocatori
    // tabella match played: IDpartita, giocatori, ngiocatori, vincitore.
    // tabella match played: KEY(IDPartita, Nickname), punti, NGiocatori, data
    // tabella GlobalRanking: KEY(nickname, ngiocatori), totscore

    public static String hashFunction(String base) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }


    public static void isLoginOk(String nickname, String password) throws SQLException, WrongPasswordException {
        String query = "SELECT nickname FROM credentials";

        try (
                Connection conn = DBManager.connect();
                PreparedStatement prepStmnt = conn.prepareStatement(query);
        ){

            try (ResultSet rs = prepStmnt.executeQuery()){
                boolean found = false;
                while(rs.next()) {
                    if(rs.getString("nickname").equals(nickname)){
                        found = true;
                        if(hashFunction(password).equals(rs.getString("password"))){
                            break;
                        } else {
                            throw new WrongPasswordException("Password sbagliata, l'untente è già esistente!");
                        }
                    }
                }
                if(!found){
                    String query2 = "INSERT INTO credentials (nickname, password) VALUES (?, ?)";

                    try(Connection connection = DBManager.connect();
                        PreparedStatement statement = connection.prepareStatement(query2)) {

                        statement.setString(1, nickname);
                        statement.setString(2, hashFunction(password));

                        int written = statement.executeUpdate(query2);
                        //METTERE WHILE/DO WHILE PER VERIFICARE CHE VENGA FATTA CORRETTAMENTE LA QUERY
                        //THROWA GIA' L'EXCEPTION!!!
                        if(written > 0) {
                            System.out.println("User Inserted");
                        }
                    }
                    catch (SQLException e) {
                        System.err.println("Errore!" + e.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public static int requestLastId (){
        int lastId = 0;

        String query = "SELECT id FROM last_id ";
        try (
                Connection conn = DBManager.connect();
                PreparedStatement prepStmnt = conn.prepareStatement(query);
        ){

            try (ResultSet rs = prepStmnt.executeQuery()){
                if(rs.next())
                    lastId = rs.getInt("id");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return lastId;
    }

    public static int reportPlayerPlace(String nickname, int playerCount){
        int rank = 0;

        String query = "SELECT count(*) + 1 as ris " +
                "FROM totals " +
                "WHERE num > (SELECT num FROM totals WHERE nickname = ? AND player_count = ? )";
        //guardare per i parimerito
        try (
                Connection conn = DBManager.connect();
                PreparedStatement prepStmnt = conn.prepareStatement(query);
        ){
            prepStmnt.setString(1, nickname);
            prepStmnt.setInt(2, playerCount);

            try (ResultSet rs = prepStmnt.executeQuery()){
                if (rs.next()){
                    rank = rs.getInt("ris");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return rank;
    }

    public static Map<String, Integer> requestRanking(String nickname, int playerCount){
        Map<String, Integer> ranking = new LinkedHashMap<String, Integer>();

        String query = "SELECT nickname, SUM(score) as tot " +
                "FROM matches " +
                "WHERE player_count = ? " +
                "GROUP BY nickname " +
                "ORDER BY tot DESC";
                //guardare per i parimerito
        try (
            Connection conn = DBManager.connect();
            PreparedStatement prepStmnt = conn.prepareStatement(query);
        ){
            prepStmnt.setInt(1, playerCount);

            try (ResultSet rs = prepStmnt.executeQuery()){
                while (rs.next()){
                    ranking.put(rs.getString("nickname"), rs.getInt("tot"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return ranking;
    }

    public static void insertMatch(int id, String nickname, int punti, int nGiocatori, Date date) {
        String query = "INSERT INTO matches (id, nickname, punti, nGiocatori, date) VALUES (?, ?, ?, ?, ?)";

        try(Connection connection = DBManager.connect();
            PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            statement.setString(2, nickname);
            statement.setInt(3, punti);
            statement.setInt(4, nGiocatori);
            statement.setDate(5, date);
            int written = statement.executeUpdate(query);

            if(written > 0) {
                System.out.println("Match Inserted");
            }
        }
        catch (SQLException e) {
            System.err.println("Errore!" + e.getMessage());
            e.printStackTrace();
        }
    }
}
