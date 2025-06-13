package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;
import model.request.ListGamesRequest;
import model.result.ListGamesResult;
import org.junit.jupiter.api.*;
import exceptions.UnauthorizedException;
import service.GameService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class SqlDataAccessTests {

    static SqlDataAccess db;

    @BeforeAll
    public static void setupAll() throws Exception {
        db = new SqlDataAccess();
    }

    @BeforeEach
    public void clear() throws DataAccessException {
        db.clear();
    }

    @Test
    public void clearSuccess() {
        assertDoesNotThrow(() -> db.clear());
    }

    @Test
    public void createUserSuccess() throws DataAccessException {
        UserData user = new UserData("alice", "password", "alice@email.com");
        db.createUser(user);
        UserData found = db.getUser("alice");
        assertNotNull(found);
        assertEquals("alice", found.username());
    }

    @Test
    public void createUserFailure() {
        assertThrows(DataAccessException.class, () -> db.createUser(new UserData("bob", null, "b@email.com")));
    }

    @Test
    public void getUserSuccess() throws DataAccessException {
        UserData user = new UserData("carol", "pw", "carol@email.com");
        db.createUser(user);
        UserData retrieved = db.getUser("carol");
        assertNotNull(retrieved);
        assertEquals("carol", retrieved.username());
    }

    @Test
    public void getUserFailure() throws DataAccessException {
        assertNull(db.getUser("ghost"));
    }

    @Test
    public void createAuthSuccess() throws DataAccessException {
        UserData user = new UserData("dan", "pw", "dan@email.com");
        db.createUser(user);

        AuthData auth = new AuthData("token-123", "dan");
        db.createAuth(auth);
        AuthData found = db.getAuth("token-123");
        assertNotNull(found);
        assertEquals("dan", found.username());
    }



    @Test
    public void createAuthFailure() throws DataAccessException {
        AuthData auth1 = new AuthData("duplicate-token", "user1");
        AuthData auth2 = new AuthData("duplicate-token", "user2"); // same token

        db.createUser(new UserData("user1", "pw", "u1@email.com"));
        db.createUser(new UserData("user2", "pw", "u2@email.com"));

        db.createAuth(auth1);

        assertThrows(DataAccessException.class, () -> db.createAuth(auth2),
                "Expected failure when inserting a duplicate auth token");
    }


    @Test
    public void getAuthSuccess() throws DataAccessException {
        UserData user = new UserData("ellen", "pw", "ellen@email.com");
        db.createUser(user);
        AuthData auth = new AuthData("auth-token", "ellen");
        db.createAuth(auth);

        AuthData found = db.getAuth("auth-token");
        assertNotNull(found);
        assertEquals("ellen", found.username());
    }

    @Test
    public void getAuthFailure() throws DataAccessException {
        assertNull(db.getAuth("invalid-token"));
    }

    @Test
    public void deleteAuthSuccess() throws DataAccessException {
        UserData user = new UserData("frank", "pw", "frank@email.com");
        db.createUser(user);
        AuthData auth = new AuthData("delete-token", "frank");
        db.createAuth(auth);

        db.deleteAuth("delete-token");
        assertNull(db.getAuth("delete-token"));
    }

    @Test
    public void deleteAuthFailure() {
        assertDoesNotThrow(() -> db.deleteAuth("nonexistent-token"));
    }



    @Test
    public void listGamesSuccess() throws Exception {
        UserData user = new UserData("tester", "pass", "email@test.com");
        db.createUser(user);
        String token = UUID.randomUUID().toString();
        db.createAuth(new AuthData(token, user.username()));

        ChessGame game = new ChessGame();
        GameData gameData = new GameData(0, null, null, "TestGame", game);
        db.createGame(gameData);

        // Act
        ListGamesRequest request = new ListGamesRequest(token);
        ListGamesResult result = new GameService(db).listGames(request);

        // Assert
        assertNotNull(result.games());
        assertTrue(result.games().size() >= 1);
        assertTrue(result.games().stream().anyMatch(g -> "TestGame".equals(g.gameName())));
    }


    @Test
    public void listGamesFailure() {
        ListGamesRequest badRequest = new ListGamesRequest(null);

        assertThrows(UnauthorizedException.class, () -> {
            new GameService(db).listGames(badRequest);
        });
    }

    @Test
    public void createGameSuccess() throws DataAccessException {
        ChessGame game = new ChessGame();
        GameData gameData = new GameData(0, null, null, "Cool Chess Match", game);
        int gameID = db.createGame(gameData);
        assertTrue(gameID > 0, "Game ID should be a positive integer");
    }

    @Test
    public void createGameFailure() {
        GameData invalidGame = new GameData(0, null, null, null, null); // missing name
        assertThrows(DataAccessException.class, () -> db.createGame(invalidGame));
    }

    @Test
    public void getGameSuccess() throws DataAccessException {
        ChessGame game = new ChessGame();
        GameData newGame = new GameData(0, null, null, "Fetchable Game", game);
        int gameID = db.createGame(newGame);

        GameData fetched = db.getGame(gameID);
        assertNotNull(fetched);
        assertEquals("Fetchable Game", fetched.gameName());
    }

    @Test
    public void getGameFailure() throws DataAccessException {
        GameData game = db.getGame(99999); // assuming this ID does not exist
        assertNull(game);
    }

    @Test
    public void updateGameSuccess() throws DataAccessException {
        // Setup: Create game and user
        UserData white = new UserData("whitePlayer", "pw", "white@email.com");
        UserData black = new UserData("blackPlayer", "pw", "black@email.com");
        db.createUser(white);
        db.createUser(black);

        ChessGame game = new ChessGame();
        GameData originalGame = new GameData(0, null, null, "To Be Updated", game);
        int gameID = db.createGame(originalGame);

        GameData updated = new GameData(gameID, white.username(), black.username(), "To Be Updated", game);
        db.updateGame(updated);

        GameData afterUpdate = db.getGame(gameID);
        assertEquals("whitePlayer", afterUpdate.whiteUsername());
        assertEquals("blackPlayer", afterUpdate.blackUsername());
    }

    @Test
    public void updateGameFailure() throws DataAccessException {
        ChessGame game = new ChessGame();
        GameData fakeGame = new GameData(99999, "nobody", "noone", "Fake Game", game);
        assertDoesNotThrow(() -> db.updateGame(fakeGame));

        GameData result = db.getGame(99999);
        assertNull(result, "Expected game to not exist after failed update");
    }



}
