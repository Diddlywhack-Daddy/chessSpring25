package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.JoinGameRequest;
import service.GameService;
import service.MemoryGameService;
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
                    !playerColor.equalsIgnoreCase("WHITE") &&
                    !playerColor.equalsIgnoreCase("BLACK")) {
                res.status(400);
                return gson.toJson(Map.of("message", "Error: Invalid player color"));
            }

            service.joinGame(request, authToken);

            res.status(200);
            res.type("application/json");
            return "{}";

        } catch (DataAccessException e) {
            res.status(401);
            return gson.toJson(Map.of("message", "Error: unauthorized"));
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }
}
