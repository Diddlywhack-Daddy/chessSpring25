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

        try {
            String authToken = req.headers("Authorization");
            if (authToken == null || authToken.isBlank()) {
                res.status(401);
                return gson.toJson(Map.of("message", "Error: unauthorized"));
            }

            AuthData auth = authService.validateToken(authToken);
            var result = gameService.listGames(auth.username());

            res.status(200);
            return gson.toJson(result);

        } catch (DataAccessException e) {
            res.status(401);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));

        } catch (Exception e) {
            res.status(500);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }
}
