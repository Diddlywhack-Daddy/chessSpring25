package server;

import server.handlers.*;
import spark.*;
import static spark.Spark.*;

public class Server {

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
