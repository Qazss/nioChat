package Server;

import javax.swing.*;
import java.sql.*;

public class AuthService {

    private static Connection connection;
    private static Statement statement;

    public static void connect() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:DBusers.db");
            statement = connection.createStatement();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void disconnect(){
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String getNickByAuthorization(String login, String pass){
        String sql = String.format("SELECT nickname FROM users WHERE login = '%s' and password = '%s' ", login, pass);

        try {
            ResultSet resultSet = statement.executeQuery(sql);

            if(resultSet.next()){
                return resultSet.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String addToBlacklist(String userNickname, String blockedNickname){
        String sql = String.format("INSERT into blacklist VALUES ('%s','%s')", userNickname, blockedNickname);

        if(userNickname.equals(blockedNickname)){
            return "Нельзя добавить себя в черный список";
        }

        try {
            if(!checkBlackListRecord(userNickname, blockedNickname)) {
                statement.execute(sql);
                return "Пользователь " + blockedNickname + " успешо добавлен в черный список";
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Данный пользователь уже добавлен в черный список";
    }

    public static boolean checkBlackListRecord(String userNickname, String blockedNickname){
        boolean result = false;
        Statement statement;

        String check = String.format("SELECT * from blacklist WHERE UserNickname = '%s' and BlockedNickname = '%s'", userNickname, blockedNickname);

        try {
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(check);
            result = resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void addToMessageHistory(String sender, String message){
        String sql = String.format("INSERT INTO MessageHistory (Sender, Message) VALUES ('%s', '%s')", sender, message);

        try {
            statement.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ResultSet getMessageHistory(){
        ResultSet resultSet = null;
        String sql = String.format("SELECT Sender, Message FROM MessageHistory");

        try {
            resultSet = statement.executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultSet;
    }
}
