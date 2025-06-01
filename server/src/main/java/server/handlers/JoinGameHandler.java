package server.handlers;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.request.JoinGameRequest;
import server.ErrorMessage;
import server.exceptions.*;
import service.GameService;
import spark.*;

public class JoinGameHandler implements Route {
    private final GameService gameService;

    public JoinGameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    @Override
    public Object handle(Request request, Response response) {
        try {
            String token = request.headers("authorization");
            JoinGameRequest body = new Gson().fromJson(request.body(), JoinGameRequest.class);
            JoinGameRequest gameRequest = new JoinGameRequest(body.playerColor(), body.gameID(),token);

            gameService.joinGame(gameRequest);

            response.status(200);
            return "";

        } catch (DataAccessException e) {
            response.status(500);
            return new Gson().toJson(new ErrorMessage("Error: " + e.getMessage())); // ensure "Error" is in message
        } catch (UnauthorizedException e) {
            response.status(401);
            return new Gson().toJson(new ErrorMessage("Error: " + e.getMessage()));
        } catch (AlreadyTakenException e) {
            response.status(403);
            return new Gson().toJson(new ErrorMessage("Error: " + e.getMessage()));
        } catch (BadRequestException e) {
            response.status(400);
            return new Gson().toJson(new ErrorMessage("Error: " + e.getMessage()));
        }
    }

}