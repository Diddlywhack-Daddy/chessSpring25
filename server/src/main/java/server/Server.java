package server;

import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import server.handlers.*;
import service.ClearService;
import service.GameService;
import service.UserService;
import spark.*;
import static spark.Spark.*;

public class Server {
    private final UserService userService;
    private final GameService gameService;
    private final ClearService clearService;

    public Server(UserService userService, GameService gameService, ClearService clearService) {
        this.userService = userService;
        this.gameService = gameService;
        this.clearService = clearService;
    }
    public Server(){
        DataAccess dataAccess = new MemoryDataAccess();
        userService = new UserService(dataAccess);
        gameService = new GameService(dataAccess);
        clearService = new ClearService(dataAccess);
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("/web");

        // Register your endpoints and handle exceptions here.
        delete("/db", new ClearHandler());
        post("/user", new RegisterHandler());
        post("/game", new CreateGameHandler());
        put("/game", new JoinGameHandler());
        get("/game", new ListGamesHandler());
        post("/session", new LoginHandler());
        delete("/session", new LogoutHandler());

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
