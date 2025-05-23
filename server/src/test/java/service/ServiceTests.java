package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ServiceTests {

    static ClearService clearService;
    static GameService gameService;
    static UserService userService;


    static MemoryDataAccess chessData;

    @BeforeAll
    public static void init() {
        chessData = new MemoryDataAccess();
        clearService = new ClearService(chessData);
        gameService = new GameService(chessData);
        userService = new UserService(chessData);
    }

    @AfterEach
    public void clear() {
        clearService.clear();
    }


    @Test
    public void clearSuccess() {
        try {
            RegisterRequest request = new RegisterRequest("alice", "pass", "a@b.com");
            userService.register(request);
        } catch (DataAccessException e) {
            fail("Unexpected error during setup");
        }

        clearService.clear();
        assert chessData.getUsers().isEmpty();
        assert chessData.getGames().isEmpty();
        assert chessData.getAuthData().isEmpty();
    }


    @Test
    public void registerSuccess() {
        RegisterRequest request = new RegisterRequest("alice", "pass", "a@b.com");
        try {
            AuthResult auth = userService.register(request);

            assertNotNull(auth);
            assertEquals("alice", auth.username());

            UserData storedUser = chessData.getUser("alice");
            assertNotNull(storedUser);
            assertEquals("pass", storedUser.password());
            assertEquals("a@b.com", storedUser.email());
        } catch (DataAccessException e) {
            fail("Registration should not have thrown an exception.");
        }
    }

    @Test
    public void registerFail() {
        RegisterRequest request = new RegisterRequest("alice", "pass", "a@b.com");
        try {
            AuthResult auth = userService.register(request);
        } catch (DataAccessException e) {
            fail("First register should succeed");
        }
        try {
            AuthResult auth = userService.register(request);
            fail("Expected DataAccessException due to duplicate username.");
        } catch (DataAccessException e) {
            //test passes
        }

    }

    @Test
    public void loginSuccess() {
        try {
            RegisterRequest request = new RegisterRequest("bob", "hunter2", "bob@example.com");
            userService.register(request);

            AuthResult auth = userService.login(new LoginRequest("bob", "hunter2"));

            assertNotNull(auth);
            assertEquals("bob", auth.username());
            assertNotNull(chessData.getAuth(auth.authToken()));
        } catch (DataAccessException e) {
            fail("Login should succeed with correct credentials.");
        }
    }

    @Test
    public void loginFail() {
        try {
            RegisterRequest request = new RegisterRequest("carol", "password", "carol@example.com");
            userService.register(request);

            userService.login(new LoginRequest("carol", "wrongPassword"));
            fail("Expected DataAccessException due to wrong password.");
        } catch (DataAccessException e) {
            // test passes
        }
    }

    @Test
    public void logoutSuccess() {
        try {
            RegisterRequest request = new RegisterRequest("dave", "securepass", "dave@example.com");
            AuthResult auth = userService.register(request);

            userService.logout(auth.authToken());

            assertNull(chessData.getAuth(auth.authToken()));
        } catch (DataAccessException e) {
            fail("Logout should succeed with valid token.");
        }
    }

    @Test
    public void logoutFail() {
        try {
            userService.logout("invalidToken");
            fail("Expected DataAccessException due to invalid token.");
        } catch (DataAccessException e) {
            // test passes
        }
    }

    @Test
    public void createGameSuccess() {
        try {
            RegisterRequest request = new RegisterRequest("eve", "123456", "eve@example.com");
            AuthResult auth = userService.register(request);

            CreateGameRequest gameRequest = new CreateGameRequest("Eve's Game");
            CreateGameResult result = gameService.createGame(gameRequest, auth.authToken());

            assertTrue(result.gameID() > 0);
            assertNotNull(chessData.getGame(result.gameID()));
        } catch (DataAccessException e) {
            fail("Game creation should succeed with valid token.");
        }
    }

    @Test
    public void createGameFail() {
        try {
            CreateGameRequest gameRequest = new CreateGameRequest("NoAuth Game");
            gameService.createGame(gameRequest, "badAuthToken");
            fail("Expected DataAccessException due to invalid auth.");
        } catch (DataAccessException e) {
            // test passes
        }
    }

    @Test
    public void listGamesSuccess() {
        try {
            RegisterRequest request = new RegisterRequest("frank", "pass123", "frank@example.com");
            AuthResult auth = userService.register(request);

            gameService.createGame(new CreateGameRequest("Game1"), auth.authToken());
            gameService.createGame(new CreateGameRequest("Game2"), auth.authToken());

            ListGamesResult result = gameService.listGames(auth.authToken());
            assertTrue(result.games().size() >= 2);
        } catch (DataAccessException e) {
            fail("Listing games should succeed with valid auth.");
        }
    }

    @Test
    public void listGamesFail() {
        try {
            gameService.listGames("fakeToken");
            fail("Expected DataAccessException due to invalid auth token.");
        } catch (DataAccessException e) {
            // test passes
        }
    }

    @Test
    public void joinGameSuccess() {
        try {
            RegisterRequest request = new RegisterRequest("george", "pw", "george@example.com");
            AuthResult auth = userService.register(request);

            CreateGameResult game = gameService.createGame(new CreateGameRequest("Joinable Game"), auth.authToken());

            JoinGameRequest joinRequest = new JoinGameRequest("WHITE", game.gameID());
            gameService.joinGame(joinRequest, auth.authToken());

            assertEquals("george", chessData.getGame(game.gameID()).whiteUsername());
        } catch (DataAccessException e) {
            fail("Join game should succeed with valid auth and color.");
        }
    }

    @Test
    public void joinGameFail() {
        try {
            RegisterRequest request = new RegisterRequest("harry", "pw", "harry@example.com");
            AuthResult auth = userService.register(request);

            CreateGameResult game = gameService.createGame(new CreateGameRequest("Bad Join"), auth.authToken());

            JoinGameRequest joinRequest = new JoinGameRequest("INVALID_COLOR", game.gameID());
            gameService.joinGame(joinRequest, auth.authToken());
            fail("Expected DataAccessException due to invalid color.");
        } catch (DataAccessException e) {
            // test passes
        }
    }

}
