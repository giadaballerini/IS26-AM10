package it.polimi.ingsw.model.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility class for obtaining JDBC connections to the application database.
 *
 * <p>Connection parameters (URL, username, password) are defined as constants
 * and target a local PostgreSQL instance. Each call to {@link #connect()}
 * returns a new {@link Connection}; callers are responsible for closing it
 * after use.</p>
 */
public class DBManager {

    /** JDBC URL of the PostgreSQL database. */
    private static final String url = "jdbc:postgresql://localhost:5432/mesos_db";

    /** Database username. */
    private static final String username = "admin";

    /** Database password. */
    private static final String password = "password123";

    /**
     * Opens and returns a new JDBC connection to the database.
     *
     * <p>The caller is responsible for closing the returned {@link Connection}
     * when it is no longer needed, preferably via a try-with-resources block.</p>
     *
     * @return a new open {@link Connection}
     * @throws SQLException if a database access error occurs or the URL is invalid
     */
    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
}