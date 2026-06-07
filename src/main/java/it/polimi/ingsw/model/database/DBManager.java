package it.polimi.ingsw.model.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBManager {
    private static final String url = "jdbc:postgresql://localhost:5432/mesos_db";
    private static final String username = "admin";
    private static final String password = "password123";

    public static Connection connect () throws SQLException{
        return DriverManager.getConnection(url,username,password);
    }
}
