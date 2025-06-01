package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class SqlDataAccessTests {

    static SqlDataAccess db;

    @BeforeAll
    public static void setup() throws Exception {
        db = new SqlDataAccess();
    }

    @BeforeEach
    public void clearDB() throws Exception {
        db.clear();
    }

    @Test
    public void testCreateAndGetUserSuccess() throws DataAccessException {
        UserData user = new UserData("alice", "hashedpw", "alice@email.com");
        db.createUser(user);
        UserData fetched = db.getUser("alice");
        assertNotNull(fetched);
        assertEquals("alice", fetched.username());
    }

    @Test
    public void testCreateAndGetAuthSuccess() throws DataAccessException {
        UserData user = new UserData("bob", "hashedpw", "bob@email.com");
        db.createUser(user);
        AuthData auth = new AuthData("token123", "bob");
        db.createAuth(auth);
        AuthData fetched = db.getAuth("token123");
        assertNotNull(fetched);
        assertEquals("bob", fetched.username());
    }

    @Test
    public void testDeleteAuthSuccess() throws DataAccessException {
        UserData user = new UserData("carol", "hashedpw", "carol@email.com");
        db.createUser(user);
        AuthData auth = new AuthData("token456", "carol");
        db.createAuth(auth);
        db.deleteAuth("token456");
        assertNull(db.getAuth("token456"));
    }

    @Test
    public void testCreateAndGetGameSuccess() throws DataAccessException {
        GameData game = new GameData(0, "white", "black", "TestGame", new ChessGame());
        int id = db.createGame(game);
        GameData fetched = db.getGame(id);
        assertNotNull(fetched);
        assertEquals("TestGame", fetched.gameName());
    }

    @Test
    public void testUpdateGameSuccess() throws DataAccessException {
        GameData game = new GameData(0, "white", "black", "Initial", new ChessGame());
        int id = db.createGame(game);
        GameData updated = new GameData(id, "newWhite", "newBlack", "Initial", new ChessGame());
        db.updateGame(updated);
        GameData fetched = db.getGame(id);
        assertEquals("newWhite", fetched.whiteUsername());
    }

    @Test
    public void testListGamesSuccess() throws DataAccessException {
        db.createGame(new GameData(0, "a", "b", "Game1", new ChessGame()));
        db.createGame(new GameData(0, "c", "d", "Game2", new ChessGame()));
        GameData[] games = db.listGames();
        assertTrue(games.length >= 2);
    }

    @Test
    public void testListUsersSuccess() throws DataAccessException {
        db.createUser(new UserData("eve", "pw", "eve@email.com"));
        db.createUser(new UserData("frank", "pw", "frank@email.com"));
        Collection<UserData> users = db.listUsers();
        assertTrue(users.size() >= 2);
    }


    @Test
    public void getUserFailure() {
        assertThrows(DataAccessException.class, () -> {
            db.getUser(null);
        });
    }

    @Test
    public void getAuthFailure() {
        assertDoesNotThrow(() -> {
            assertNull(db.getAuth("nonexistent-token"));
        });
    }

    @Test
    public void deleteAuthFailure() {
        assertDoesNotThrow(() -> {
            db.deleteAuth("nonexistent-token");
        });
    }

    @Test
    public void createUserFailure() {
        assertThrows(DataAccessException.class, () -> {
            db.createUser(new UserData("user", null, "email@test.com"));
        });
    }

    @Test
    public void createGameFailure() {
        assertThrows(DataAccessException.class, () -> {
            db.createGame(new GameData(0, null, null, null, null));
        });
    }

    @Test
    public void getGameFailure() {
        assertDoesNotThrow(() -> {
            assertNull(db.getGame(-1));
        });
    }
}
