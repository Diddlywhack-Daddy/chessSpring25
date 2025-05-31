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

    /*@Test
    public void createGameSuccess() throws Exception {
        RegisterResult reg = userService.register(new RegisterRequest("test", "pass", "email@test.com"));
        CreateGameRequest request = new CreateGameRequest(reg.authToken(), "Game1");
        CreateGameResult result = gameService.createGame(request, reg.authToken());
        assertTrue(result.gameID() > 0);
    }*/

    @Test
    public void createGameFail() {
        CreateGameRequest request = new CreateGameRequest("badToken", "Game1");
        assertThrows(Exception.class, () -> gameService.createGame(request));
    }

    @Test
    public void ListGamesSuccess() throws AlreadyTakenException, DataAccessException, BadRequestException, UnauthorizedException {
        RegisterRequest registerRequest = new RegisterRequest("user", "pass", "mail@mail.com");
        RegisterResult registerResult = userService.register(registerRequest);
        String authToken1 = registerResult.authToken();
        CreateGameRequest createGameRequest = new CreateGameRequest(authToken1, "Game1");
        gameService.createGame(createGameRequest);
        createGameRequest = new CreateGameRequest(authToken1, "Game2");
        gameService.createGame(createGameRequest);
        ListGamesRequest listGamesRequest = new ListGamesRequest(registerResult.authToken());
        ListGamesResult listGamesResult = gameService.listGames(listGamesRequest);

        Collection<GameData> games = new ArrayList<>();
        games.add(new GameData(1, null, null, "Game1", new ChessGame()));
        games.add(new GameData(2, null, null, "Game2", new ChessGame()));
        ListGamesResult correctResult = new ListGamesResult(games);
        assertEquals(correctResult, listGamesResult);
    }
    @Test
    public void listGamesFail() {
        ListGamesRequest request = new ListGamesRequest("badToken");
        assertThrows(Exception.class, () -> gameService.listGames(request));
    }


    @Test
    public void joinGameSuccess() throws Exception {
        RegisterResult reg = userService.register(new RegisterRequest("test", "pass", "email@test.com"));
        CreateGameResult game = gameService.createGame(new CreateGameRequest(reg.authToken(), "Game1"));
        JoinGameRequest join = new JoinGameRequest(reg.authToken(), ChessGame.TeamColor.WHITE, game.gameID());
        assertDoesNotThrow(() -> gameService.joinGame(join));
    }

    @Test
    public void joinGameFail() throws Exception {
        RegisterResult reg = userService.register(new RegisterRequest("test", "pass", "email@test.com"));
        CreateGameResult game = gameService.createGame(new CreateGameRequest(reg.authToken(), "Game1"));
        JoinGameRequest join = new JoinGameRequest(reg.username(), null, game.gameID());
        assertThrows(Exception.class, () -> gameService.joinGame(join));
    }
}
