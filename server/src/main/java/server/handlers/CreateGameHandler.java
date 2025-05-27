package server.handlers;

import com.google.gson.Gson;
import model.CreateGameRequest;
import model.CreateGameResult;
import service.GameService;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

public class CreateGameHandler implements Route {
    private final Gson gson = new Gson();
    private final GameService service;

    public CreateGameHandler(GameService service) {
        this.service = service;
    }

    @Override
    public Object handle(Request req, Response res) {
        try {
            String authToken = req.headers("Authorization");
            CreateGameRequest request = gson.fromJson(req.body(), CreateGameRequest.class);

            if (request == null || request.gameName() == null || request.gameName().isBlank()) {
                res.status(400);
                return gson.toJson(Map.of("message", "Error: bad request"));
            }

            CreateGameResult result = service.createGame(request, authToken);
            res.status(200);
            res.type("application/json");
            return gson.toJson(result);

        } catch (dataaccess.DataAccessException e) {
            res.status(401);
            return gson.toJson(Map.of("message", "Error: unauthorized"));
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }
}
