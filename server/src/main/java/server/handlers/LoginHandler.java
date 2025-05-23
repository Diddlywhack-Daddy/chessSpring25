package server.handlers;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.AuthResult;
import model.LoginRequest;
import service.memoryImplementation.MemoryUserService;
import service.interfaces.UserService;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

public class LoginHandler implements Route {
    private final Gson gson = new Gson();
    private final UserService service = new MemoryUserService(MemoryDataAccess.getInstance());

    @Override
    public Object handle(Request req, Response res) {
        try {
            //System.out.println("Login endpoint hit");

            LoginRequest request = gson.fromJson(req.body(), LoginRequest.class);
            AuthResult result = service.login(request);

            res.status(200);
            res.type("application/json");
            return gson.toJson(result);

        } catch (IllegalArgumentException e) {
            res.status(400);
            return gson.toJson(Map.of("message", "Error: bad request"));
        } catch (DataAccessException e) {
            res.status(401);
            return gson.toJson(Map.of("message", "Error: unauthorized"));
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }
}
