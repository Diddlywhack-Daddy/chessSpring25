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
        res.type("application/json");

        try {
            RegisterRequest request = gson.fromJson(req.body(), RegisterRequest.class);

            if (request == null ||
                    request.username() == null || request.username().isBlank() ||
                    request.password() == null || request.password().isBlank() ||
                    request.email() == null || request.email().isBlank()) {
                res.status(400);
                return gson.toJson(Map.of("message", "Error: bad request"));
            }

            AuthResult result = service.register(request);
            res.status(200);
            return gson.toJson(result);

        } catch (DataAccessException e) {
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("already taken")) {
                res.status(403); // Forbidden - username taken
            } else {
                res.status(500); // Internal Server Error
            }
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        } catch (Exception e) {
            res.status(500); // Catch-all fallback
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }
}
