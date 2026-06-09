package it.polimi.ingsw.model.database;

import it.polimi.ingsw.exceptions.WrongPasswordException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Data Access Object for player credentials and match ranking data.
 *
 * <p>Provides methods to authenticate players, retrieve and update rankings,
 * and record match results in the database. All methods open and close their
 * own {@link Connection} via {@link DBManager#connect()}.</p>
 */
public class RankingDAO {

    private static final Logger LOG = Logger.getLogger(RankingDAO.class.getName());

    /**
     * Computes the SHA-256 hash of the given string and returns it as a
     * lowercase hexadecimal string.
     *
     * <p>Used to hash passwords before storing or comparing them in the database.</p>
     *
     * @param base the string to hash; must not be {@code null}
     * @return the SHA-256 hex digest of {@code base}
     * @throws RuntimeException if the SHA-256 algorithm is not available
     */
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

    /**
     * Validates a player's login credentials, registering the player if they
     * do not yet exist in the database.
     *
     * <p>If the nickname is already present, the provided password is checked
     * against the stored hash. If the nickname is new, a new record is inserted
     * with the hashed password.</p>
     *
     * @param nickname the player's nickname; must not be {@code null}
     * @param password the player's plaintext password; must not be {@code null}
     * @throws WrongPasswordException if the nickname exists but the password does not match
     * @throws SQLException           if a database access error occurs
     */
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
                    String insertQuery = "INSERT INTO credentials (nickname, psw) VALUES (?, ?)";
                    try (PreparedStatement insertStmnt = conn.prepareStatement(insertQuery)) {
                        insertStmnt.setString(1, nickname);
                        insertStmnt.setString(2, hashFunction(password));
                        int rowsWritten = insertStmnt.executeUpdate();
                        if (rowsWritten > 0)
                            LOG.info("User Inserted");
                    }
                }
            }
        } catch (SQLException e) {
            LOG.warning("Errore Database isLoginOk: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Retrieves the ID of the last recorded game from the database.
     *
     * <p>Returns {@code 0} if the database is unreachable or the table is empty.</p>
     *
     * @return the last game ID, or {@code 0} if it cannot be retrieved
     */
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

    /**
     * Returns the global ranking position of a player for matches with the
     * given number of players.
     *
     * <p>The position is 1-based: a return value of {@code 1} means the player
     * has the highest cumulative score among all players in that category.
     * Returns {@code -1} if the database is unreachable.</p>
     *
     * @param nickname    the player's nickname
     * @param playerCount the number of players in the match category to consider
     * @return the player's ranking position (1-based), or {@code -1} on error
     */
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

    /**
     * Retrieves the global ranking for matches with the given number of players,
     * ordered by total score descending.
     *
     * <p>Each entry maps a player's nickname to their cumulative score across
     * all recorded matches in that category. Returns an empty map if the
     * database is unreachable.</p>
     *
     * @param nickname    the nickname of the requesting player (reserved for
     *                    future filtering; currently unused in the query)
     * @param playerCount the number of players in the match category to rank
     * @return an ordered map of nickname → total score, never {@code null}
     */
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

    /**
     * Inserts a match result for a single player into the database.
     *
     * @param gameId      unique identifier of the game
     * @param nickname    the player's nickname
     * @param score       the score achieved by the player in this match
     * @param playerCount the total number of players in the match
     * @param date        the date on which the match was played
     */
    public static void insertMatch(int gameId, String nickname, int score, int playerCount, Date date) {
        String query = "INSERT INTO matches (gameId, nickname, score, nPlayers, matchDate) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBManager.connect();
             PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setInt(1, gameId);
            statement.setString(2, nickname);
            statement.setInt(3, score);
            statement.setInt(4, playerCount);
            statement.setDate(5, date);
            int rowsWritten = statement.executeUpdate();
            if (rowsWritten > 0)
                LOG.info("Match Inserted");
        } catch (SQLException e) {
            LOG.warning("Errore durante l'inserimento del match: " + e.getMessage());
        }
    }
}