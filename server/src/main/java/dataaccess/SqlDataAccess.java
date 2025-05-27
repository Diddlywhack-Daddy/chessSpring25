package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.sql.*;


public class SqlDataAccess implements DataAccess {

    public SqlDataAccess() throws DataAccessException {
        DatabaseManager.createDatabase();
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
    public void clear() throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM auth");
            stmt.executeUpdate("DELETE FROM users");
            stmt.executeUpdate("DELETE FROM games");
        } catch (SQLException e) {
            throw new DataAccessException("Failed to clear database", e);
        }
    }


    @Override
    public void createUser(UserData user) throws DataAccessException {
        String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.username());
            stmt.setString(2, user.password());
            stmt.setString(3, user.email());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to create user", e);
        }

    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        String sql = "SELECT username, password, email FROM users WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new UserData(
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("email")
                    );
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to retrieve user", e);
        }

    }

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {

    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        String sql = "SELECT token, username FROM auth WHERE token = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, authToken);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new AuthData(rs.getString("username"), rs.getString("token"));
                } else {
                    return null; // No auth found
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to retrieve auth data", e);
        }
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        String sql = "DELETE FROM auth WHERE token = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, authToken);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete auth token", e);
        }
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
