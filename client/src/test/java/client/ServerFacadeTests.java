package client;

import backend.ServerFacade;
import chess.ChessGame;
import model.request.*;
import model.result.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.Server;
import server.exceptions.BadRequestException;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private ServerFacade facade;

    @BeforeEach
    public void setup() {
        Server server = new Server(0);
        int port = server.run(0);
        facade = new ServerFacade("http://localhost:" + port);
    }


    @Test
    public void testRegisterSuccess() {
        var request = new RegisterRequest("testuser", "testpass", "test@example.com");
        try {
            RegisterResult result = facade.register(request);
            assertNotNull(result.authToken());
            assertEquals("testuser", result.username());
        } catch (BadRequestException e) {
            fail("Register should not throw: " + e.getMessage());
        }
    }

    @Test
    public void testLoginFailure_WrongCredentials() {
        var request = new LoginRequest("wronguser", "wrongpass");
        assertThrows(BadRequestException.class, () -> facade.login(request));
    }

    @Test
    public void testCreateGame() {
        try {
            var reg = facade.register(new RegisterRequest("creator", "pass", "a@b.com"));
            var req = new CreateGameRequest(reg.authToken(), "Cool Game");
            var res = facade.createGame(req);
            assertTrue(res.gameID() > 0);
        } catch (BadRequestException e) {
            fail("Unexpected error during game creation: " + e.getMessage());
        }
    }

    @Test
    public void testListGames() {
        try {
            var reg = facade.register(new RegisterRequest("lister", "pass", "a@b.com"));
            var res = facade.listGames(new ListGamesRequest(reg.authToken()));
            assertNotNull(res.games());
        } catch (BadRequestException e) {
            fail("Unexpected error during game list: " + e.getMessage());
        }
    }

    @Test
    public void testJoinGame() {
        try {
            var reg = facade.register(new RegisterRequest("joiner", "pass", "a@b.com"));
            var create = facade.createGame(new CreateGameRequest(reg.authToken(), "Joinable Game"));
            var req = new JoinGameRequest(ChessGame.TeamColor.WHITE, create.gameID(),reg.authToken());
            var result = facade.joinGame(req);
            assertNotNull(result);
        } catch (BadRequestException e) {
            fail("Unexpected error during join: " + e.getMessage());
        }
    }

    @Test
    public void testLogout() {
        try {
            var reg = facade.register(new RegisterRequest("logoutuser", "pass", "a@b.com"));
            facade.logout(new LogoutRequest(reg.authToken()));
        } catch (BadRequestException e) {
            fail("Unexpected error during logout: " + e.getMessage());
        }
    }

    @Test
    public void testClear() {
        try {
            facade.clear();
        } catch (BadRequestException e) {
            fail("Clear should not throw");
        }
    }
}
