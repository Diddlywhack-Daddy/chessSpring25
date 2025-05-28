package service;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.*;
import model.request.*;
import model.result.CreateGameResult;
import model.result.ListGamesResult;
import model.result.LoginResult;
import model.result.RegisterResult;
import org.junit.jupiter.api.*;
import server.exceptions.AlreadyTakenException;
import server.exceptions.BadRequestException;
import server.exceptions.UnauthorizedException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ServiceTests {

    static ClearService clearService;
    static GameService gameService;
    static UserService userService;
    static MemoryDataAccess data;

    @BeforeAll
    public static void setup() {
        data = new MemoryDataAccess();
        clearService = new ClearService(data);
        gameService = new GameService(data);
        userService = new UserService(data);
    }

    @AfterEach
    public void reset() {
        clearService.clear();
    }

    @Test
    public void clearSuccess() {
        assertDoesNotThrow(() -> clearService.clear());
    }

    @Test
    public void registerSuccess() throws Exception {
        RegisterRequest request = new RegisterRequest("test", "pass", "email@test.com");
        RegisterResult result = userService.register(request);
        assertNotNull(result.authToken());
    }

    @Test
    public void registerFail() throws Exception {
        RegisterRequest request = new RegisterRequest("test", "pass", "email@test.com");
        userService.register(request);
        assertThrows(Exception.class, () -> userService.register(request));
    }

    @Test
    public void loginSuccess() throws Exception {
        userService.register(new RegisterRequest("test", "pass", "email@test.com"));
        LoginResult result = userService.login(new LoginRequest("test", "pass"));
        assertNotNull(result.authToken());
    }

    @Test
    public void loginFail() throws Exception {
        userService.register(new RegisterRequest("test", "pass", "email@test.com"));
        assertThrows(Exception.class, () -> userService.login(new LoginRequest("test", "wrong")));
    }

    @Test
    public void logoutSuccess() throws Exception {
        RegisterResult reg = userService.register(new RegisterRequest("test", "pass", "email@test.com"));
        assertDoesNotThrow(() -> userService.logout(reg.authToken()));
    }

    @Test
    public void logoutFail() {
        assertThrows(Exception.class, () -> userService.logout("badToken"));
    }

    @Test
    public void createGameSuccess() throws Exception {
        RegisterResult reg = userService.register(new RegisterRequest("test", "pass", "email@test.com"));
        CreateGameRequest request = new CreateGameRequest(reg.authToken(), "Game1");
        CreateGameResult result = gameService.createGame(request, reg.authToken());
        assertTrue(result.gameID() > 0);
    }

    @Test
    public void createGameFail() {
        CreateGameRequest request = new CreateGameRequest("badToken", "Game1");
        assertThrows(Exception.class, () -> gameService.createGame(request, "badToken"));
    }

    @Test
    public void listGamesSuccess() throws AlreadyTakenException, DataAccessException, BadRequestException, UnauthorizedException {
        // Register a user and create two games
        RegisterRequest registerRequest = new RegisterRequest("user", "pass", "mail@mail.com");
        RegisterResult registerResult = userService.register(registerRequest);
        String authToken = registerResult.authToken();

        gameService.createGame(new CreateGameRequest(authToken, "Game1"), authToken);
        gameService.createGame(new CreateGameRequest(authToken, "Game2"), authToken);

        // Get the list of games
        ListGamesResult result = gameService.listGames(authToken);
        List<GameData> actualGames = new ArrayList<>(result.games());

        // Validate size and names only
        assertEquals(2, actualGames.size());

        GameData game1 = actualGames.get(0);
        GameData game2 = actualGames.get(1);

        assertEquals("Game1", game1.gameName());
        assertEquals("Game2", game2.gameName());

        assertTrue(game1.gameID() > 0);
        assertTrue(game2.gameID() > 0);
        assertNotEquals(game1.gameID(), game2.gameID());

        // Ensure no usernames were assigned yet
        assertNull(game1.whiteUsername());
        assertNull(game1.blackUsername());
        assertNull(game2.whiteUsername());
        assertNull(game2.blackUsername());
    }



    @Test
    public void listGamesFail() {
        assertThrows(Exception.class, () -> gameService.listGames("badToken"));
    }

    @Test
    public void joinGameSuccess() throws Exception {
        RegisterResult reg = userService.register(new RegisterRequest("test", "pass", "email@test.com"));
        CreateGameResult game = gameService.createGame(new CreateGameRequest(reg.authToken(), "Game1"), reg.authToken());
        JoinGameRequest join = new JoinGameRequest(reg.username(), ChessGame.TeamColor.WHITE, game.gameID());
        assertDoesNotThrow(() -> gameService.joinGame(join, reg.authToken()));
    }

    @Test
    public void joinGameFail() throws Exception {
        RegisterResult reg = userService.register(new RegisterRequest("test", "pass", "email@test.com"));
        CreateGameResult game = gameService.createGame(new CreateGameRequest(reg.authToken(), "Game1"), reg.authToken());
        JoinGameRequest join = new JoinGameRequest(reg.username(), null, game.gameID());
        assertThrows(Exception.class, () -> gameService.joinGame(join, reg.authToken()));
    }
}
