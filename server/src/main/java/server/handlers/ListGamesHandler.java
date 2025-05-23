package server.handlers;

import com.google.gson.Gson;
import dataaccess.MemoryDataAccess;
import service.GameService;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

public class ListGamesHandler implements Route {
    private final Gson gson = new Gson();
    private final service.interfaces.GameService service = new GameService(MemoryDataAccess.getInstance());

    @Override
    public Object handle(Request req, Response res) {
        try {
            String authToken = req.headers("Authorization");
            var result = service.listGames(authToken);
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
