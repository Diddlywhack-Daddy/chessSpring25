package server.handlers;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.request.ListGamesRequest;
import model.result.ListGamesResult;
import server.ErrorMessage;
import server.exceptions.UnauthorizedException;
import service.GameService;
import spark.*;

public class ListGamesHandler implements Route {
    private final GameService gameService;

    public ListGamesHandler(GameService gameService) {
        this.gameService = gameService;
    }

    @Override
    public Object handle(Request request, Response response) {
        try {
            String token = request.headers("authorization");
            ListGamesResult result = gameService.listGames(token);
            response.status(200);
            return new Gson().toJson(result);
        } catch (UnauthorizedException e) {
            response.status(401);
            return new Gson().toJson(new ErrorMessage(e.getMessage()));
        } catch (DataAccessException e) {
            response.status(500);
            return new Gson().toJson(new ErrorMessage(e.getMessage()));
        }
    }
}
