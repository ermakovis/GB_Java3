package Server.ServerEngine;

import java.sql.*;

public class SqlClient {

    private static Connection connection;
    private static Statement statement;

    synchronized static void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:MyServer/chat-db.db");
            statement = connection.createStatement();
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    synchronized static void disconnect() {
        try {
            connection.close();
        } catch (SQLException throwables) {
            throw new RuntimeException(throwables);
        }
    }

    synchronized static String getNickname(String login) {
        try {
            ResultSet rs = statement.executeQuery(
                    String.format("select nickname from users where login = '%s'",
                            login));
            if (rs.next()) {
                return rs.getString("nickname");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    synchronized static String changeNickName(String login, String newName) {
        try {
            statement.executeUpdate(
                    String.format("update users set nickname = '%s' where login = '%s'",
                            newName, login));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }
}
