package service;

import chess.ChessGame;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.*;
import model.request.CreateGameRequest;
import model.request.JoinGameRequest;
import model.request.ListGamesRequest;
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
    public CreateGameResult createGame(CreateGameRequest request)
            throws DataAccessException, BadRequestException, UnauthorizedException {
        String authToken = request.authToken();
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

        GameData newGame = new GameData(0, auth.username(), null, request.gameName(), new ChessGame());
        int gameID = data.createGame(newGame);
        return new CreateGameResult(gameID);
    }


    @Override
    public ListGamesResult listGames(ListGamesRequest listGamesRequest)
            throws DataAccessException, UnauthorizedException {
        String authToken = listGamesRequest.authToken();
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
    public JoinGameResult joinGame(JoinGameRequest request)
            throws DataAccessException, UnauthorizedException, BadRequestException, AlreadyTakenException {
        String authToken = request.authToken();

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

        if (color == ChessGame.TeamColor.WHITE) {
            if (game.whiteUsername() != null && !game.whiteUsername().equals(username)) {
                throw new AlreadyTakenException("Error: Another player has already taken that spot.");
            }
        } else if (color == ChessGame.TeamColor.BLACK) {
            if (game.blackUsername() != null && !game.blackUsername().equals(username)) {
                throw new AlreadyTakenException("Error: Another player has already taken that spot.");
            }
        } else {
            throw new BadRequestException("Error: Invalid player color.");
        }

        String white = (color == ChessGame.TeamColor.WHITE) ? username : game.whiteUsername();
        String black = (color == ChessGame.TeamColor.BLACK) ? username : game.blackUsername();
        GameData updatedGame = new GameData(game.gameID(), white, black, game.gameName(), game.game());
        data.updateGame(updatedGame);

        return new JoinGameResult();
    }
}
