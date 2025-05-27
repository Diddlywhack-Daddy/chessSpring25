package server.handlers;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.AuthResult;
import model.RegisterRequest;
import service.UserService;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

public class RegisterHandler implements Route {
    private final Gson gson = new Gson();
    private final UserService service;

    public RegisterHandler(UserService service) {
        this.service = service;
    }

    @Override
    public Object handle(Request req, Response res) {
        try {
            RegisterRequest request = gson.fromJson(req.body(), RegisterRequest.class);

            if (request.username() == null || request.password() == null || request.email() == null) {
                res.status(400);
                return gson.toJson(Map.of("message", "Error: bad request"));
            }

            AuthResult result = service.register(request);

            res.status(200);
            res.type("application/json");
            return gson.toJson(result);

        } catch (DataAccessException e) {
            res.status(403);
            return gson.toJson(Map.of("message", "Error: already taken"));
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }
}
