package service;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.SqlDataAccess;
import model.*;
import model.request.*;
import model.result.CreateGameResult;
import model.result.ListGamesResult;
import model.result.LoginResult;
import model.result.RegisterResult;
import org.junit.jupiter.api.*;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class ServiceTests {

    static ClearService clearService;
    static GameService gameService;
    static UserService userService;
    static SqlDataAccess data;

    @BeforeAll
    public static void setup() {
        try {
            data = new SqlDataAccess();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
        clearService = new ClearService(data);
        gameService = new GameService(data);
        userService = new UserService(data);
    }

    @BeforeEach
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
        CreateGameResult result = gameService.createGame(request);
        assertTrue(result.gameID() > 0);
    }

    @Test
    public void createGameFail() {
        CreateGameRequest request = new CreateGameRequest("badToken", "Game1");
        assertThrows(Exception.class, () -> gameService.createGame(request));
    }

    @Test
    public void listGamesSuccess() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest("user", "pass", "mail@mail.com");
        RegisterResult registerResult = userService.register(registerRequest);
        String authToken1 = registerResult.authToken();

        gameService.createGame(new CreateGameRequest(authToken1, "Game1"));
        gameService.createGame(new CreateGameRequest(authToken1, "Game2"));

        ListGamesResult listGamesResult = gameService.listGames(new ListGamesRequest(authToken1));
        Collection<GameData> games = listGamesResult.games();

        assertEquals(2, games.size());
        assertTrue(games.stream().anyMatch(g -> g.gameName().equals("Game1")));
        assertTrue(games.stream().anyMatch(g -> g.gameName().equals("Game2")));
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
        JoinGameRequest join = new JoinGameRequest(ChessGame.TeamColor.WHITE, game.gameID(), reg.authToken());
        assertDoesNotThrow(() -> gameService.joinGame(join));
    }

    @Test
    public void joinGameFail() throws Exception {
        RegisterResult reg = userService.register(new RegisterRequest("test", "pass", "email@test.com"));
        CreateGameResult game = gameService.createGame(new CreateGameRequest(reg.authToken(), "Game1"));
        JoinGameRequest join = new JoinGameRequest(null,game.gameID(), reg.authToken());
        assertThrows(Exception.class, () -> gameService.joinGame(join));
    }
}
