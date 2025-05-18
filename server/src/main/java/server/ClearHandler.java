package server;

import com.google.gson.Gson;
import dataaccess.MemoryDataAccess;
import model.BasicResult;
import service.ClearService;
import service.MemoryClearService;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

public class ClearHandler implements Route {
    private final Gson gson = new Gson();
    private final ClearService service = new MemoryClearService(MemoryDataAccess.getInstance());

    @Override
    public Object handle(Request req, Response res) {
        BasicResult result = service.clear();
        if (result.success()) {
            res.status(200);
            System.out.println("Clear endpoint hit");
            return "{}";
        } else {
            res.status(500);
            return gson.toJson(Map.of("message", result.message()));
        }
    }
}
