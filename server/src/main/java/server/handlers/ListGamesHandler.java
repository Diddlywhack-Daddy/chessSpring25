package server.handlers;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.AuthData;
import service.AuthService;
import service.GameService;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

public class ListGamesHandler implements Route {
    private final Gson gson = new Gson();
    private final GameService gameService;
    private final AuthService authService;

    public ListGamesHandler(GameService gameService, AuthService authService) {
        this.gameService = gameService;
        this.authService = authService;
    }

    @Override
    public Object handle(Request req, Response res) {
        res.type("application/json");

        String authHeader = req.headers("authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            res.status(401);
            return gson.toJson(Map.of("message", "Error: unauthorized"));
        }
        String authToken = authHeader.substring("Bearer ".length());
        if (authToken == null || authToken.isBlank()) {
            res.status(401);
            return gson.toJson(Map.of("message", "Error: unauthorized"));
        }

        try {
            AuthData auth = authService.validateToken(authToken);
            var result = gameService.listGames(auth.username());

            res.status(200);
            return gson.toJson(result);

        } catch (DataAccessException e) {
            String msg = e.getMessage().toLowerCase();
            if (msg.contains("unauthorized")) {
                res.status(401);
            } else {
                res.status(500);
            }
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));

        } catch (Exception e) {
            res.status(500);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }
}
