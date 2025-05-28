package server;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import dataaccess.SqlDataAccess;
import server.handlers.*;
import service.AuthService;
import service.ClearService;
import service.GameService;
import service.UserService;
import spark.*;
import static spark.Spark.*;

public class Server {
    private final UserService userService;
    private final GameService gameService;
    private final ClearService clearService;
    private final AuthService authService;

    public Server() {
        DataAccess dataAccess;
        try {
            dataAccess = new SqlDataAccess();
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to initialize SQL backend", e);
        }

        this.userService = new UserService(dataAccess);
        this.gameService = new GameService(dataAccess);
        this.clearService = new ClearService(dataAccess);
        this.authService = new AuthService(dataAccess);
    }


    public Server(UserService userService, GameService gameService, ClearService clearService, AuthService authService) {
        this.userService = userService;
        this.gameService = gameService;
        this.authService = authService;
        this.clearService = clearService;
    }


    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("/web");

        // Register your endpoints and handle exceptions here.
        post("/user", new RegisterHandler(userService));
        delete("/db", new ClearHandler(clearService));
        post("/session", new LoginHandler(userService));
        delete("/session", new LogoutHandler(userService));
        post("/game", new CreateGameHandler(gameService));
        get("/game", new ListGamesHandler(gameService));
        put("/game", new JoinGameHandler(gameService));

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
