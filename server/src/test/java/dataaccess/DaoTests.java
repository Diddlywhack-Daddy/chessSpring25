
package dataaccess;

import chess.ChessGame;
import model.GameData;
import model.request.*;
import model.result.*;
import org.junit.jupiter.api.*;
import server.exceptions.AlreadyTakenException;
import server.exceptions.BadRequestException;
import server.exceptions.UnauthorizedException;
import service.AuthService;
import service.ClearService;
import service.GameService;
import service.UserService;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;

public class DaoTests {

    private static SqlDataAccess db;
    private ClearService clearService;
    private UserService userService;
    private GameService gameService;
    private AuthService authService;

    @BeforeAll
    public static void setupAll() throws Exception {
        db = new SqlDataAccess();
    }

    @BeforeEach
    public void setupEach() throws Exception {
        clearService = new ClearService(db);
        userService = new UserService(db);
        gameService = new GameService(db);
        authService = new AuthService(db);
        clearService.clear();
    }

    @Test
    public void clearPositive() {
        assertDoesNotThrow(() -> clearService.clear());
    }

    @Test
    public void registerUserPositive() throws Exception {
        RegisterRequest req = new RegisterRequest("alice", "password", "alice@email.com");
        RegisterResult res = userService.register(req);
        assertNotNull(res.authToken());
        assertEquals("alice", res.username());
    }

    @Test
    public void registerUserNegative_duplicate() throws Exception {
        RegisterRequest req = new RegisterRequest("bob", "pass", "b@email.com");
        userService.register(req);
        assertThrows(AlreadyTakenException.class, () -> userService.register(req));
    }

    @Test
    public void loginPositive() throws Exception {
        userService.register(new RegisterRequest("carol", "pass", "c@email.com"));
        LoginResult res = userService.login(new LoginRequest("carol", "pass"));
        assertNotNull(res.authToken());
        assertEquals("carol", res.username());
    }

    @Test
    public void loginNegative_wrongPassword() throws Exception {
        userService.register(new RegisterRequest("dave", "goodpass", "d@email.com"));
        assertThrows(UnauthorizedException.class, () -> userService.login(new LoginRequest("dave", "badpass")));
    }

    @Test
    public void createGamePositive() throws Exception {
        userService.register(new RegisterRequest("Whitey", "pw", "w@email.com"));
        userService.register(new RegisterRequest("Blackey", "pw", "b@email.com"));

        GameData game = new GameData(0, "Whitey", "Blackey", "Game1", new ChessGame());
        int gameID = db.createGame(game);

        assertTrue(gameID > 0);

        GameData retrieved = db.getGame(gameID);
        assertNotNull(retrieved);
        assertEquals("Whitey", retrieved.whiteUsername());
        assertEquals("Blackey", retrieved.blackUsername());
        assertEquals("Game1", retrieved.gameName());
    }




    @Test
    public void createGameNegative() throws UnauthorizedException {
        CreateGameRequest badRequest = new CreateGameRequest(null,"Game1");

        assertThrows(UnauthorizedException.class, () -> gameService.createGame(badRequest));    }

    @Test
    public void listGamesPositive() throws Exception {
        RegisterResult user = userService.register(new RegisterRequest("greg", "pw", "g@email.com"));
        gameService.createGame(new CreateGameRequest("One", user.authToken()));
        gameService.createGame(new CreateGameRequest("Two", user.authToken()));

        ListGamesRequest request = new ListGamesRequest(user.authToken());
        Collection<GameData> games = gameService.listGames(request).games();
        assertTrue(games.size() >= 2);
    }

    @Test
    public void joinGamePositive() throws Exception {
        RegisterResult user = userService.register(new RegisterRequest("helen", "pw", "h@email.com"));
        CreateGameResult created = gameService.createGame(new CreateGameRequest("Joinable", user.authToken()));
        assertDoesNotThrow(() -> gameService.joinGame(new JoinGameRequest(ChessGame.TeamColor.WHITE, created.gameID(),user.authToken())));
    }

    @Test
    public void joinGameNegative() throws Exception {
        RegisterResult user = userService.register(new RegisterRequest("ian", "pw", "i@email.com"));
        assertThrows(BadRequestException.class, () -> gameService.joinGame(new JoinGameRequest(ChessGame.TeamColor.WHITE,99999, user.authToken())));
    }
}
