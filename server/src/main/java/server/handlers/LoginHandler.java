package server.handlers;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.request.LoginRequest;
import model.result.LoginResult;
import server.ErrorMessage;
import server.exceptions.BadRequestException;
import server.exceptions.UnauthorizedException;
import service.UserService;
import spark.*;

public class LoginHandler implements Route {
    private final UserService userService;

    public LoginHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Object handle(Request request, Response response) {
        try {
            LoginRequest loginRequest = new Gson().fromJson(request.body(), LoginRequest.class);
            LoginResult result = userService.login(loginRequest);
            response.status(200);
            return new Gson().toJson(result);
        } catch (BadRequestException e) {
            response.status(400);
            return new Gson().toJson(new ErrorMessage(e.getMessage()));

        } catch (UnauthorizedException e) {
            response.status(401);
            return new Gson().toJson(new ErrorMessage(e.getMessage()));
        } catch (DataAccessException e) {
            response.status(500);
            return new Gson().toJson(new ErrorMessage(e.getMessage()));
        }
    }
}