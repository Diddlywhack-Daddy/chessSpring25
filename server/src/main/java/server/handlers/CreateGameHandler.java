package server.handlers;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.request.CreateGameRequest;
import model.result.CreateGameResult;
import model.GameData;
import server.ErrorMessage;
import server.exceptions.BadRequestException;
import server.exceptions.UnauthorizedException;
import service.GameService;
import spark.*;

public class CreateGameHandler implements Route {
    private final GameService gameService;

    public CreateGameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    @Override
    public Object handle(Request request, Response response) {
        try {
            String token = request.headers("authorization");
            GameData gameData = new Gson().fromJson(request.body(), GameData.class);
            CreateGameRequest gameRequest = new CreateGameRequest(token, gameData.gameName());
            CreateGameResult result = gameService.createGame(gameRequest);
            response.status(200);
            return new Gson().toJson(result);
        } catch (UnauthorizedException e) {
            response.status(401);
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
