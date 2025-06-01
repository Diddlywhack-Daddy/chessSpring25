package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


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
                            name VARCHAR(255) NOT NULL,
                            gameState TEXT,
                            white VARCHAR(255),
                            black VARCHAR(255),
                            FOREIGN KEY (white) REFERENCES users(username),
                            FOREIGN KEY (black) REFERENCES users(username)
                            
                        )
                    """);

        } catch (SQLException e) {
            throw new DataAccessException("Error: initializing database tables", e);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM games");
            stmt.executeUpdate("DELETE FROM auth");
            stmt.executeUpdate("DELETE FROM users");
        } catch (SQLException e) {
            throw new DataAccessException("Error: failed to clear database", e);
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
                            rs.getString("email"));
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error: failed to retrieve user", e);
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        String sql = "SELECT token, username FROM auth WHERE token = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, authToken);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new AuthData(rs.getString("token"), rs.getString("username"));
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error: failed to retrieve auth data", e);
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
            throw new DataAccessException("Error: failed to delete auth token", e);
        }
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        if (user.password() == null) {
            throw new DataAccessException("Error: password cannot be null");
        }

        System.out.println("Creating user with password hash: " + user.password());

        String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.username());
            stmt.setString(2, user.password());
            stmt.setString(3, user.email());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error: failed to create user", e);
        }
    }

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        System.out.println("Creating auth token for user: " + auth.username());
        String sql = "INSERT INTO auth (token, username) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, auth.authToken());
            stmt.setString(2, auth.username());
            stmt.executeUpdate();
            System.out.println("Auth token created successfully: " + auth.authToken());
        } catch (SQLException e) {
            System.err.println("Error creating auth token: " + e.getMessage());
            throw new DataAccessException("Error: failed to create auth token", e);
        }
    }

    @Override
    public int createGame(GameData game) throws DataAccessException {
        String sql = "INSERT INTO games (name, gameState, white, black) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            System.out.println("DEBUG: Preparing to insert game: " + game.gameName());

            stmt.setString(1, game.gameName());
            stmt.setString(2, game.game() != null ? game.game().serialize() : null);

            if (game.whiteUsername() != null) {
                stmt.setString(3, game.whiteUsername());
            } else {
                stmt.setNull(3, Types.VARCHAR);
            }

            if (game.blackUsername() != null) {
                stmt.setString(4, game.blackUsername());
            } else {
                stmt.setNull(4, Types.VARCHAR);
            }

            int affectedRows = stmt.executeUpdate();
            System.out.println("DEBUG: Rows affected by insert: " + affectedRows);

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    System.out.println("DEBUG: Generated game ID: " + id);
                    return id;
                } else {
                    System.out.println("DEBUG: No generated key returned!");
                    throw new DataAccessException("Error: Failed to get auto-generated game ID");
                }
            }

        } catch (SQLException e) {
            System.out.println("DEBUG: SQLException: " + e.getMessage());
            throw new DataAccessException("Error: Failed to create game", e);
        }
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        String sql = "UPDATE games SET gameState = ?, white = ?, black = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            System.out.printf("updateGame: id=%d, white=%s, black=%s, name=%s%n",
                    game.gameID(), game.whiteUsername(), game.blackUsername(), game.gameName());
            stmt.setString(1, game.game().serialize());

            if (game.whiteUsername() != null) {
                stmt.setString(2, game.whiteUsername());
            } else {
                stmt.setNull(2, Types.VARCHAR);
            }

            if (game.blackUsername() != null) {
                stmt.setString(3, game.blackUsername());
            } else {
                stmt.setNull(3, Types.VARCHAR);
            }

            stmt.setInt(4, game.gameID());


            System.out.printf("Updating game ID %d with white=%s, black=%s%n", game.gameID(), game.whiteUsername(), game.blackUsername());
            int rowsUpdated = stmt.executeUpdate();
            System.out.println("updateGame: rows updated = " + rowsUpdated);

            System.out.printf("Saving game ID %d: white=%s, black=%s%n",
                    game.gameID(), game.whiteUsername(), game.blackUsername());
            System.out.println("Updated game white player: " + game.whiteUsername());


        } catch (SQLException e) {
            throw new DataAccessException("Error: failed to update game", e);
        }
    }


    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        System.out.println("Attempting to fetch game with ID: " + gameID);

        String sql = "SELECT id, name, gameState, white, black FROM games WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, gameID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Game found: ID = " + rs.getInt("id"));
                    System.out.println("Fetched game white player: " + rs.getString("white"));
                    System.out.printf("getGame: id=%d, white=%s, black=%s, name=%s%n",
                            rs.getInt("id"), rs.getString("white"), rs.getString("black"), rs.getString("name"));

                    return new GameData(
                            rs.getInt("id"),
                            rs.getString("white"),
                            rs.getString("black"),
                            rs.getString("name"),
                            ChessGame.deserialize(rs.getString("gameState"))
                    );
                } else {
                    System.out.println("No game found with ID: " + gameID);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error: failed to get game", e);
        }
    }



    @Override
    public GameData[] listGames() throws DataAccessException {
        String sql = "SELECT id, name, gameState, white, black FROM games";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            List<GameData> games = new ArrayList<>();
            while (rs.next()) {
                ChessGame game = rs.getString("gameState") != null
                        ? ChessGame.deserialize(rs.getString("gameState"))
                        : null;
                games.add(new GameData(
                        rs.getInt("id"),
                        rs.getString("white"),
                        rs.getString("black"),
                        rs.getString("name"),
                        game));
            }
            return games.toArray(new GameData[0]);
        } catch (SQLException e) {
            throw new DataAccessException("Error: failed to list games", e);
        }
    }
}
