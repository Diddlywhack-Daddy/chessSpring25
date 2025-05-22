package server;

import com.google.gson.Gson;
import dataaccess.MemoryDataAccess;
import model.BasicResult;
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
            System.out.println("JoinGame endpoint hit");

            String authToken = req.headers("Authorization");
            JoinGameRequest request = gson.fromJson(req.body(), JoinGameRequest.class);

            BasicResult result = service.joinGame(request, authToken);

            if (result.success()) {
                res.status(200);
                return "{}";
            } else {
                res.status(403);
                return gson.toJson(Map.of("message", result.message()));
            }

        } catch (Exception e) {
            res.status(500);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }
}
