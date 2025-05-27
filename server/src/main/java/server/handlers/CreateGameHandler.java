package server.handlers;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.CreateGameRequest;
import model.CreateGameResult;
import service.AuthService;
import service.GameService;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

public class CreateGameHandler implements Route {
    private final Gson gson = new Gson();
    private final GameService gameService;
    private final AuthService authService;

    public CreateGameHandler(GameService gameService, AuthService authService) {
        this.gameService = gameService;
        this.authService = authService;
    }

    @Override
    public Object handle(Request req, Response res) {
        res.type("application/json");

        String authToken = req.headers("Authorization");
        if (authToken == null || authToken.isBlank()) {
            res.status(401);
            return gson.toJson(Map.of("message", "Error: unauthorized"));
        }

        try {
            authService.validateToken(authToken);
        } catch (DataAccessException e) {
            if (e.getMessage().toLowerCase().contains("unauthorized")) {
                res.status(401);
            } else {
                res.status(500);
            }
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        }

        try {
            CreateGameRequest request = gson.fromJson(req.body(), CreateGameRequest.class);
            if (request == null || request.gameName() == null || request.gameName().isBlank()) {
                res.status(400);
                return gson.toJson(Map.of("message", "Error: bad request"));
            }

            CreateGameResult result = gameService.createGame(request, authToken);
            res.status(200);
            return gson.toJson(result);

        } catch (DataAccessException e) {
            res.status(500);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }
}
