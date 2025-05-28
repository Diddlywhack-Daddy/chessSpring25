package server.handlers;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.AuthResult;
import model.LoginRequest;
import service.UserService;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

public class LoginHandler implements Route {
    private final Gson gson = new Gson();
    private final UserService service;

    public LoginHandler(UserService service) {
        this.service = service;
    }

    @Override
    public Object handle(Request req, Response res) {
        res.type("application/json");

        try {
            LoginRequest request = gson.fromJson(req.body(), LoginRequest.class);

            if (request == null || request.username() == null || request.password() == null) {
                res.status(400);
                return gson.toJson(Map.of("message", "Error: bad request"));
            }

            AuthResult result = service.login(request);
            res.status(200);
            return gson.toJson(result);

        } catch (DataAccessException e) {
            String message = e.getMessage().toLowerCase();

            if (message.contains("connection") || message.contains("sql") || message.contains("database")) {
                res.status(500);
                return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
            }

            if (message.contains("unauthorized") || message.contains("invalid") || message.contains("not found")) {
                res.status(401);
                return gson.toJson(Map.of("message", "Error: unauthorized"));
            }

            res.status(500);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));


        } catch (Exception e) {
            res.status(500);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }
}
