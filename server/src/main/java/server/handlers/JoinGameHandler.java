package server.handlers;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.JoinGameRequest;
import service.interfaces.GameService;
import service.memoryImplementation.MemoryGameService;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

public class JoinGameHandler implements Route {
    private final Gson gson = new Gson();
    private final GameService service = new MemoryGameService(MemoryDataAccess.getInstance());

    @Override
    public Object handle(Request req, Response res) {
        try {
            String authToken = req.headers("Authorization");
            if (authToken == null || authToken.isBlank()) {
                res.status(401);
                return gson.toJson(Map.of("message", "Error: unauthorized"));
            }

            JoinGameRequest request = gson.fromJson(req.body(), JoinGameRequest.class);

            if (request == null || request.gameID() <= 0) {
                res.status(400);
                return gson.toJson(Map.of("message", "Error: Invalid game ID"));
            }

            String playerColor = request.playerColor();
            if (playerColor != null &&
                    !playerColor.equals("WHITE") &&
                    !playerColor.equals("BLACK")) {
                res.status(400);
                return gson.toJson(Map.of("message", "Error: Invalid player color"));
            }

            service.joinGame(request, authToken);

            res.status(200);
            res.type("application/json");
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
