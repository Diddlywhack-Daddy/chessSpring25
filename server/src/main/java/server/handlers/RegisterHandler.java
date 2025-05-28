package server.handlers;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.request.RegisterRequest;
import model.result.RegisterResult;
import server.ErrorMessage;
import server.exceptions.AlreadyTakenException;
import server.exceptions.BadRequestException;
import service.UserService;
import spark.*;

public class RegisterHandler implements Route {
    private final UserService userService;

    public RegisterHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Object handle(Request request, Response response) {
        try {
            RegisterRequest user = new Gson().fromJson(request.body(), RegisterRequest.class);
            RegisterResult result = userService.register(user);
            response.status(200);
            return new Gson().toJson(result);
        } catch (AlreadyTakenException e) {
            response.status(403);
            return new Gson().toJson(new ErrorMessage(e.getMessage()));
        } catch (BadRequestException e) {
            response.status(400);
            return new Gson().toJson(new ErrorMessage(e.getMessage()));
        } catch (DataAccessException e) {
            response.status(500);
            return new Gson().toJson(new ErrorMessage(e.getMessage()));
        }
    }
}