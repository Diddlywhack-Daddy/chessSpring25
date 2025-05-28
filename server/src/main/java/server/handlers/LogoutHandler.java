package server.handlers;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.request.LogoutRequest;
import server.ErrorMessage;
import server.exceptions.UnauthorizedException;
import service.UserService;
import spark.*;

public class LogoutHandler implements Route {
    private final UserService userService;

    public LogoutHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Object handle(Request request, Response response) {
        try {
            String token = request.headers("authorization");
            userService.logout(token);
            response.status(200);
            return "";
        } catch (UnauthorizedException e) {
            response.status(401);
            return new Gson().toJson(new ErrorMessage(e.getMessage()));
        } catch (DataAccessException e) {
            response.status(500);
            return new Gson().toJson(new ErrorMessage(e.getMessage()));
        }
    }
}