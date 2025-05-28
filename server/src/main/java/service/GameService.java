package service;

import chess.ChessGame;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.*;
import model.request.CreateGameRequest;
import model.request.JoinGameRequest;
import model.result.CreateGameResult;
import model.result.JoinGameResult;
import model.result.ListGamesResult;
import server.exceptions.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GameService implements service.interfaces.GameService {
    private final DataAccess data;

    public GameService(DataAccess data) {
        this.data = data;
    }

    @Override
    public CreateGameResult createGame(CreateGameRequest request, String authToken)
            throws DataAccessException, BadRequestException, UnauthorizedException {
        if (authToken == null || authToken.isBlank()) {
            throw new UnauthorizedException("Error: Unauthorized access.");
        }

        AuthData auth = data.getAuth(authToken);
        if (auth == null) {
            throw new UnauthorizedException("Error: Unauthorized access.");
        }

        if (request == null || request.gameName() == null || request.gameName().isBlank()) {
            throw new BadRequestException("Error: Invalid game name.");
        }

        int gameID = data.createGame(new GameData(0, null, null, request.gameName(), new ChessGame()));
        return new CreateGameResult(gameID);
    }

    @Override
    public ListGamesResult listGames(String authToken)
            throws DataAccessException, UnauthorizedException {

        if (authToken == null || authToken.isBlank()) {
            throw new UnauthorizedException("Error: Unauthorized access.");
        }

        AuthData auth = data.getAuth(authToken);
        if (auth == null) {
            throw new UnauthorizedException("Error: Unauthorized access.");
        }

        Collection<GameData> gameList = List.of(data.listGames());  // or Arrays.asList(...) if array
        return new ListGamesResult(gameList);
    }


    @Override
    public JoinGameResult joinGame(JoinGameRequest request, String authToken)
            throws DataAccessException, UnauthorizedException, BadRequestException, AlreadyTakenException {

        if (authToken == null || request == null || request.color() == null) {
            throw new BadRequestException("Error: Invalid request.");
        }

        AuthData auth = data.getAuth(authToken);
        if (auth == null) {
            throw new UnauthorizedException("Error: Unauthorized access.");
        }

        GameData game = data.getGame(request.gameID());
        if (game == null) {
            throw new BadRequestException("Error: Invalid request.");
        }

        String username = auth.username();
        ChessGame.TeamColor color = request.color();

        switch (color) {
            case WHITE:
                if (game.whiteUsername() != null && !game.whiteUsername().equals(username)) {
                    throw new AlreadyTakenException("Error: Another player has already taken that spot.");
                }
                game = new GameData(game.gameID(), username, game.blackUsername(), game.gameName(), game.game());
                break;

            case BLACK:
                if (game.blackUsername() != null && !game.blackUsername().equals(username)) {
                    throw new AlreadyTakenException("Error: Another player has already taken that spot.");
                }
                game = new GameData(game.gameID(), game.whiteUsername(), username, game.gameName(), game.game());
                break;

            default:
                throw new BadRequestException("Error: Invalid player color.");
        }

        data.updateGame(game);
        return new JoinGameResult();
    }
}
