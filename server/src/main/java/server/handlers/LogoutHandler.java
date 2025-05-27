package server.handlers;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.BasicResult;
import service.AuthService;
import service.UserService;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

public class LogoutHandler implements Route {
    private final Gson gson = new Gson();
    private final UserService userService;
    private final AuthService authService;

    public LogoutHandler(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @Override
    public Object handle(Request req, Response res) {
        res.type("application/json");

        try {
            String authToken = req.headers("Authorization");

            if (authToken == null || authToken.isBlank()) {
                res.status(401);
                return gson.toJson(Map.of("message", "Error: unauthorized"));
            }

            authService.validateToken(authToken);
            userService.logout(authToken);

            res.status(200);
            return "{}";

        } catch (DataAccessException e) {
            res.status(500);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }
}
