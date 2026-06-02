package it.polimi.ingsw.model.persistence;

import it.polimi.ingsw.exceptions.WrongPasswordException;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.logging.Logger;

public class RankingDAO {

    private static final Logger LOG = Logger.getLogger(RankingDAO.class.getName());

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
        String query = "SELECT psw FROM credentials WHERE nickname = ?";

        try (Connection conn = DBManager.connect();
             PreparedStatement prepStmnt = conn.prepareStatement(query)) {

            prepStmnt.setString(1, nickname);

            try (ResultSet rs = prepStmnt.executeQuery()) {
                if (rs.next()) {
                    if (!hashFunction(password).equals(rs.getString("psw"))) {
                        throw new WrongPasswordException("Password sbagliata, l'utente è già esistente!");
                    }
                } else {
                    String query2 = "INSERT INTO credentials (nickname, psw) VALUES (?, ?)";
                    try (PreparedStatement statement2 = conn.prepareStatement(query2)) {
                        statement2.setString(1, nickname);
                        statement2.setString(2, hashFunction(password));
                        int written = statement2.executeUpdate();
                        if (written > 0)
                            LOG.info("User Inserted");
                    }
                }
            }
        } catch (SQLException e) {
            LOG.warning("Errore Database isLoginOk: " + e.getMessage());
            throw e;
        }
    }

    public static int requestLastId() {
        String query = "SELECT gameId FROM last_id";
        try (Connection conn = DBManager.connect();
             PreparedStatement prepStmnt = conn.prepareStatement(query);
             ResultSet rs = prepStmnt.executeQuery()) {
            if (rs.next())
                return rs.getInt("gameId");
        } catch (SQLException e) {
            LOG.warning("DB non raggiungibile in requestLastId: " + e.getMessage());
        }
        return 0;
    }

    public static int reportPlayerPlace(String nickname, int playerCount) {
        String query = "SELECT count(*) + 1 as ris " +
                "FROM totals " +
                "WHERE nPlayers = ? AND num > (SELECT num FROM totals WHERE nickname = ? AND nPlayers = ?)";

        try (Connection conn = DBManager.connect();
             PreparedStatement prepStmnt = conn.prepareStatement(query)) {
            prepStmnt.setInt(1, playerCount);
            prepStmnt.setString(2, nickname);
            prepStmnt.setInt(3, playerCount);
            try (ResultSet rs = prepStmnt.executeQuery()) {
                if (rs.next())
                    return rs.getInt("ris");
            }
        } catch (SQLException e) {
            LOG.warning("DB non raggiungibile in reportPlayerPlace: " + e.getMessage());
        }
        return -1;
    }

    public static Map<String, Integer> requestRanking(String nickname, int playerCount) {
        String query = "SELECT nickname, SUM(score) as tot " +
                "FROM matches " +
                "WHERE nPlayers = ? " +
                "GROUP BY nickname " +
                "ORDER BY tot DESC";

        try (Connection conn = DBManager.connect();
             PreparedStatement prepStmnt = conn.prepareStatement(query)) {
            prepStmnt.setInt(1, playerCount);
            try (ResultSet rs = prepStmnt.executeQuery()) {
                Map<String, Integer> ranking = new LinkedHashMap<>();
                while (rs.next())
                    ranking.put(rs.getString("nickname"), rs.getInt("tot"));
                return ranking;
            }
        } catch (SQLException e) {
            LOG.warning("DB non raggiungibile in requestRanking: " + e.getMessage());
        }
        return new LinkedHashMap<>();
    }

    public static void insertMatch(int id, String nickname, int punti, int nGiocatori, Date date) {
        String query = "INSERT INTO matches (gameId, nickname, score, nPlayers, matchDate) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBManager.connect();
             PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setInt(1, id);
            statement.setString(2, nickname);
            statement.setInt(3, punti);
            statement.setInt(4, nGiocatori);
            statement.setDate(5, date);
            int written = statement.executeUpdate();
            if (written > 0)
                LOG.info("Match Inserted");
        } catch (SQLException e) {
            LOG.warning("Errore durante l'inserimento del match: " + e.getMessage());
        }
    }
}