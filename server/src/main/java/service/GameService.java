package service;

import chess.*;
import dataaccess.*;
import exceptions.*;
import model.AuthData;
import model.GameData;
import model.request.*;
import model.result.*;


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

        ChessGame chessGame = new ChessGame();
        GameData tempGame = new GameData(0, null, null, request.gameName(), chessGame);

        int gameID = data.createGame(tempGame);


        GameData createdGame = new GameData(gameID, null, null, request.gameName(), chessGame);

        return new CreateGameResult(createdGame.gameID());
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

        Collection<GameData> gameList = List.of(data.listGames());
        return new ListGamesResult(gameList);
    }

    @Override
    public JoinGameResult joinGame(JoinGameRequest request)
            throws DataAccessException, UnauthorizedException, BadRequestException, AlreadyTakenException {

        if (request == null || request.authToken() == null) {
            throw new UnauthorizedException("Error: Unauthorized access.");
        }

        AuthData auth = data.getAuth(request.authToken());

        if (auth == null) {
            throw new UnauthorizedException("Error: Unauthorized access.");
        }

        if (request.playerColor() == null) {
            throw new BadRequestException("Error: Invalid request.");
        }

        // DEBUG: Print all games before fetching a specific one
        System.out.println("=== Current Games in DB BEFORE getGame ===");
        for (GameData g : data.listGames()) {
            System.out.printf("GameID: %d, Name: %s, White: %s, Black: %s%n",
                    g.gameID(), g.gameName(), g.whiteUsername(), g.blackUsername());
        }

        GameData game = data.getGame(request.gameID());

        if (game == null) {
            throw new BadRequestException("Error: Invalid request.");
        }


        String username = auth.username();
        ChessGame.TeamColor color = request.playerColor();

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
        System.out.printf("joinGame: gameID=%d, white=%s, black=%s%n",
                updatedGame.gameID(), updatedGame.whiteUsername(), updatedGame.blackUsername());

        data.updateGame(updatedGame);

        return new JoinGameResult();
    }



}
