package server.handlers;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.JoinGameRequest;
import service.AuthService;
import service.GameService;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

public class JoinGameHandler implements Route {
    private final Gson gson = new Gson();
    private final GameService gameService;
    private final AuthService authService;

    public JoinGameHandler(GameService gameService, AuthService authService) {
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

            authService.validateToken(authToken);

            JoinGameRequest request = gson.fromJson(req.body(), JoinGameRequest.class);
            if (request == null || request.gameID() <= 0) {
                res.status(400);
                return gson.toJson(Map.of("message", "Error: invalid game ID"));
            }

            String playerColor = request.playerColor();
            if (playerColor != null &&
                    !playerColor.equals("WHITE") &&
                    !playerColor.equals("BLACK")) {
                res.status(400);
                return gson.toJson(Map.of("message", "Error: invalid player color"));
            }

            gameService.joinGame(request, authToken);

            res.status(200);
            return "{}";

        } catch (DataAccessException e) {
            String message = e.getMessage().toLowerCase();

            if (message.contains("unauthorized")) {
                res.status(401);
            } else if (message.contains("already taken") || message.contains("forbidden")) {
                res.status(403);
            } else if (message.contains("bad request") || message.contains("invalid") || message.contains("missing")) {
                res.status(400);
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
