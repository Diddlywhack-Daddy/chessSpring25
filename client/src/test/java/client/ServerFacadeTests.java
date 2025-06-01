package client;

import org.junit.jupiter.api.*;
import server.Server;
import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;
    private static String validAuthToken;
    private static int createdGameId;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade("http://localhost:" + port);

        // Register and log in a user to get a valid auth token for tests
        facade.register("testuser", "testpass", "test@example.com");
        var loginResult = facade.login("testuser", "testpass");
        validAuthToken = loginResult.authToken();
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    // -------- Register Tests --------
    @Test
    public void testRegisterSuccess() {
        var result = facade.register("newuser", "newpass", "new@example.com");
        assertNotNull(result.authToken());
    }

    @Test
    public void testRegisterFailureDuplicate() {
        var result = facade.register("testuser", "testpass", "test@example.com");
        assertNull(result.authToken());
    }

    // -------- Login Tests --------
    @Test
    public void testLoginSuccess() {
        var result = facade.login("testuser", "testpass");
        assertNotNull(result.authToken());
    }

    @Test
    public void testLoginFailureWrongPassword() {
        var result = facade.login("testuser", "wrongpass");
        assertNull(result.authToken());
    }

    // -------- Logout Tests --------
    @Test
    public void testLogoutSuccess() {
        var result = facade.logout(validAuthToken);
        assertTrue(result.success());
    }

    @Test
    public void testLogoutFailureInvalidToken() {
        var result = facade.logout("badtoken");
        assertFalse(result.success());
    }

    // -------- Create Game Tests --------
    @Test
    public void testCreateGameSuccess() {
        var result = facade.createGame(validAuthToken, "My Test Game");
        assertNotNull(result.gameID());
        createdGameId = result.gameID();
    }

    @Test
    public void testCreateGameFailureInvalidToken() {
        var result = facade.createGame("badtoken", "Fail Game");
        assertNull(result.gameID());
    }

    // -------- List Games Tests --------
    @Test
    public void testListGamesSuccess() {
        var result = facade.listGames(validAuthToken);
        assertNotNull(result.games());
        assertTrue(result.games().size() > 0);
    }

    @Test
    public void testListGamesFailureInvalidToken() {
        var result = facade.listGames("badtoken");
        assertNull(result.games());
    }

    // -------- Play Game Tests --------
    @Test
    public void testPlayGameSuccess() {
        var result = facade.playGame(validAuthToken, createdGameId);
        assertTrue(result.success());
    }

    @Test
    public void testPlayGameFailureInvalidGameId() {
        var result = facade.playGame(validAuthToken, -1);
        assertFalse(result.success());
    }

    // -------- Observe Game Tests --------
    @Test
    public void testObserveGameSuccess() {
        var result = facade.observeGame(validAuthToken, createdGameId);
        assertTrue(result.success());
    }

    @Test
    public void testObserveGameFailureInvalidToken() {
        var result = facade.observeGame("badtoken", createdGameId);
        assertFalse(result.success());
    }


}
