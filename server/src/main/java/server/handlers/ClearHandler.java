package server.handlers;

import com.google.gson.Gson;
import model.BasicResult;
import service.ClearService;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

public class ClearHandler implements Route {
    private final Gson gson = new Gson();
    private final ClearService service;

    public ClearHandler(ClearService service) {
        this.service = service;
    }

    @Override
    public Object handle(Request req, Response res) {
        BasicResult result = service.clear();
        if (result.success()) {
            res.status(200);
            return "{}";
        } else {
            res.status(500);
            return gson.toJson(Map.of("message", result.message()));
        }
    }
}
