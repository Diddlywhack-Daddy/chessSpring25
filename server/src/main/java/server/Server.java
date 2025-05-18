package server;

import spark.*;
import spark.Spark.*;

import static com.sun.tools.jdeprscan.Messages.get;
import static javax.swing.UIManager.put;
import static spark.Spark.delete;
import static spark.Spark.post;

public class Server {

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        delete("/db", new ClearHandler());
        post("/user", new RegisterHandler());
        post("/game", new CreateGameHandler());
        put("/game", new JoinGameHandler());
        get("/game", new ListGamesHandler());
        post("/session", new LoginHandler());
        delete("/session", new LogoutHandler());





        //This line initializes the server and can be removed once you have a functioning endpoint
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
