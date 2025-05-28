package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
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
                            name VARCHAR(255),
                            gameState TEXT,
                            white VARCHAR(255),
                            black VARCHAR(255),
                            FOREIGN KEY (white) REFERENCES users(username),
                            FOREIGN KEY (black) REFERENCES users(username)
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
    public void createUser(UserData user) throws DataAccessException {
        System.out.println("Creating user: " + user.username());
        String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());
            stmt.setString(1, user.username());
            stmt.setString(2, hashedPassword);
            stmt.setString(3, user.email());
            stmt.executeUpdate();
            System.out.println("User created successfully: " + user.username());
        } catch (SQLException e) {
            System.err.println("Error creating user: " + e.getMessage());
            throw new DataAccessException("Failed to create user", e);
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
            throw new DataAccessException("Failed to create auth token", e);
        }
    }

    @Override
    public int createGame(GameData game) throws DataAccessException {
        System.out.println("Creating game: " + game.gameName());
        String sql = "INSERT INTO games (name, gameState, white, black) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, game.gameName());
            stmt.setString(2, game.game() != null ? game.game().serialize() : null);
            stmt.setString(3, game.whiteUsername());
            stmt.setString(4, game.blackUsername());
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int gameId = generatedKeys.getInt(1);
                    System.out.println("Game created with ID: " + gameId);
                    return gameId;
                } else {
                    throw new DataAccessException("Failed to retrieve game ID after insert");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating game: " + e.getMessage());
            throw new DataAccessException("Failed to create game", e);
        }
    }


    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        String sql = "SELECT id, name, gameState, white, black FROM games WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, gameID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("name");
                    String state = rs.getString("gameState");
                    String white = rs.getString("white");
                    String black = rs.getString("black");

                    return new GameData(gameID, white, black, name, ChessGame.deserialize(state));

                }
                return null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get game", e);
        }
    }


    @Override
    public void updateGame(GameData game) throws DataAccessException {
        String sql = "UPDATE games SET gameState = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, game.game().serialize());
            stmt.setInt(2, game.gameID());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update game", e);
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
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String state = rs.getString("gameState");
                String white = rs.getString("white");
                String black = rs.getString("black");
                ChessGame game = state != null ? ChessGame.deserialize(state) : null;

                games.add(new GameData(id, white, black, name, game));
            }

            return games.toArray(new GameData[0]);

        } catch (SQLException e) {
            throw new DataAccessException("Failed to list games", e);
        }
    }
}

