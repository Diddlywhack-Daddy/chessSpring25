package service.memoryimplementation;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class GameService implements service.interfaces.GameService {
    private final DataAccess data;
    private final AtomicInteger idCounter = new AtomicInteger(1);

    public GameService(DataAccess dataAccess) {
        this.data = dataAccess;
    }
    @Override
    public CreateGameResult createGame(CreateGameRequest request, String authToken) throws DataAccessException {
        if (authToken == null || data.getAuth(authToken) == null) {
            throw new DataAccessException("Unauthorized");
        }
        if (request.gameName() == null) {
            throw new IllegalArgumentException("Missing game name");
        }

        int gameID = idCounter.getAndIncrement();
        var game = new GameData(gameID, null, null, request.gameName(), new chess.ChessGame());
        data.createGame(game);
        return new CreateGameResult(gameID);
    }

    @Override
    public ListGamesResult listGames(String authToken) throws DataAccessException {
        if (authToken == null || data.getAuth(authToken) == null) {
            throw new dataaccess.DataAccessException("Unauthorized");
        }

        GameData[] games = data.listGames();
        List<ListGamesResult.GameInfo> infoList = new ArrayList<>();

        for (GameData game : games) {
            infoList.add(new ListGamesResult.GameInfo(
                    game.gameID(),
                    game.whiteUsername(),
                    game.blackUsername(),
                    game.gameName()
            ));
        }

        return new ListGamesResult(infoList);
    }

    @Override
    public BasicResult joinGame(JoinGameRequest request, String authToken) throws DataAccessException {
        if (authToken == null || request.playerColor() == null) {
            throw new DataAccessException("Missing auth or color");
        }

        AuthData auth = data.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("Unauthorized");
        }

        GameData game = data.getGame(request.gameID());
        if (game == null) {
            throw new DataAccessException("Invalid game ID");
        }

        String username = auth.username();
        String color = request.playerColor();

        switch (color) {
            case "WHITE":
                if (game.whiteUsername() != null && !game.whiteUsername().equals(username)) {
                    throw new DataAccessException("Color already taken");
                }
                game = new GameData(game.gameID(), username, game.blackUsername(), game.gameName(), game.game());
                break;

            case "BLACK":
                if (game.blackUsername() != null && !game.blackUsername().equals(username)) {
                    throw new DataAccessException("Color already taken");
                }
                game = new GameData(game.gameID(), game.whiteUsername(), username, game.gameName(), game.game());
                break;


            default:
                throw new DataAccessException("Invalid player color");
        }

        data.updateGame(game);
        return new BasicResult(true, null);
    }



}
