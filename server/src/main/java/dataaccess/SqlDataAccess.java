package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.sql.*;


public class SqlDataAccess implements DataAccess {

    public SqlDataAccess() throws DataAccessException {
        createTablesIfNotExist();
    }

    private void createTablesIfNotExist() throws DataAccessException {
        try (Connection connection = DatabaseManager.getConnection();
             Statement statement = connection.createStatement()) {

            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS users (
                    username VARCHAR(255) PRIMARY KEY,
                    password VARCHAR(255) NOT NULL,
                    email VARCHAR(255)
                )
            """);

            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS auth (
                    token VARCHAR(255) PRIMARY KEY,
                    username VARCHAR(255),
                    FOREIGN KEY (username) REFERENCES users(username)
                )
            """);

            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS games (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    name VARCHAR(255),
                    gameState TEXT
                )
            """);

        } catch (SQLException e) {
            throw new DataAccessException("Error initializing database tables", e);
        }
    }

    @Override
    public void clear() {

    }

    @Override
    public void createUser(UserData user) throws DataAccessException {

    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        return null;
    }

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {

    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        return null;
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {

    }

    @Override
    public void createGame(GameData game) throws DataAccessException {

    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return null;
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {

    }

    @Override
    public GameData[] listGames() throws DataAccessException {
        return new GameData[0];
    }
}
