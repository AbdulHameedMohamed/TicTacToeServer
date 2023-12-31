package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javafx.scene.control.Alert;
import util.Util;

public class Database {

    public static final String DB_NAME = "TicTacToeDB";
    private static final String JDBC_URL = "jdbc:derby://localhost:1527/TicTacToeDB";
    public static String USERNAME;
    public static String PASSWORD;

    public static Connection connection;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            closeConnection();
        }));
    }

    private Database() {
    }

    public static Connection startConnection(String username, String password) throws ClassNotFoundException, SQLException {
        USERNAME = username;
        PASSWORD = password;
        if (connection == null) {
            Class.forName("org.apache.derby.jdbc.ClientDriver");
            connection = DriverManager.getConnection(JDBC_URL, username, password);
        }
        return connection;
    }

    public static Connection getConnection() throws ClassNotFoundException, SQLException {
        if (connection == null) {
            Class.forName("org.apache.derby.jdbc.ClientDriver");
            connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                Util.showDialog(Alert.AlertType.ERROR, "close Connection Error", "Error While close Database Connection");
            } finally {
                connection = null;
            }
        }
    }
}
