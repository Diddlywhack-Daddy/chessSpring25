package client;

import backend.ServerFacade;
import chess.ChessGame;
import exceptions.BadRequestException;
import model.request.*;
import model.result.*;
import org.junit.jupiter.api.*;
import server.Server;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;
    private static String username = "user1";
    private static String password = "pass1";
    private static String email = "user1@email.com";
    private String authToken;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade("http://localhost:" + port);
    }

    @BeforeEach
    public void clearServer() throws Exception {
        facade.clear();
    }

    @Test
    public void registerSuccess() throws Exception {
        var request = new RegisterRequest(username, password, email);
        var result = facade.register(request);
        assertNotNull(result.authToken());
        assertEquals(username, result.username());
    }

    @Test
    public void registerFailure() {
        var request = new RegisterRequest(username, null, email);
        assertThrows(BadRequestException.class, () -> facade.register(request));
    }

    @Test
    public void loginSuccess() throws Exception {
        facade.register(new RegisterRequest(username, password, email));
        var result = facade.login(new LoginRequest(username, password));
        assertNotNull(result.authToken());
        assertEquals(username, result.username());
    }

    @Test
    public void loginFailure() {
        var request = new LoginRequest("fakeuser", "wrongpass");
        assertThrows(BadRequestException.class, () -> facade.login(request));
    }

    @Test
    public void logoutSuccess() throws Exception {
        var register = facade.register(new RegisterRequest(username, password, email));
        var logoutRequest = new LogoutRequest(register.authToken());
        assertDoesNotThrow(() -> facade.logout(logoutRequest));
    }

    @Test
    public void logoutFailure() {
        var logoutRequest = new LogoutRequest("bad-token");
        assertThrows(BadRequestException.class, () -> facade.logout(logoutRequest));
    }

    @Test
    public void listGamesSuccess() throws Exception {
        var register = facade.register(new RegisterRequest(username, password, email));
        var result = facade.listGames(new ListGamesRequest(register.authToken()));
        assertNotNull(result.games());
    }

    @Test
    public void listGamesFailure() {
        var request = new ListGamesRequest("bad-token");
        assertThrows(BadRequestException.class, () -> facade.listGames(request));
    }

    @Test
    public void createGameSuccess() throws Exception {
        var register = facade.register(new RegisterRequest(username, password, email));
        var request = new CreateGameRequest( register.authToken(),"MyGame");
        var result = facade.createGame(request);
        assertNotNull(result.gameID());
    }

    @Test
    public void createGameFailure() {
        var request = new CreateGameRequest("GameWithBadAuth", "bad-token");
        assertThrows(BadRequestException.class, () -> facade.createGame(request));
    }

    @Test
    public void joinGameSuccess() throws Exception {
        var register = facade.register(new RegisterRequest(username, password, email));
        var createResult = facade.createGame(new CreateGameRequest( register.authToken(),"GameToJoin"));

        var whiteJoin = facade.joinGame(new JoinGameRequest(ChessGame.TeamColor.WHITE, createResult.gameID(),register.authToken()));
        var blackJoin = facade.joinGame(new JoinGameRequest(ChessGame.TeamColor.BLACK, createResult.gameID(),register.authToken()));

        assertNull(whiteJoin);
        assertNull(blackJoin);
    }



    @Test
    public void joinGameFailure() {
        var joinRequest = new JoinGameRequest(ChessGame.TeamColor.BLACK,9999,"bad-token" );
        assertThrows(BadRequestException.class, () -> facade.joinGame(joinRequest));
    }

    @Test
    public void clearSuccess() {
        assertDoesNotThrow(() -> facade.clear());
    }

    @Test
    public void clearIdempotent() {
        assertDoesNotThrow(() -> {
            facade.clear();
            facade.clear();
        });
    }
}
