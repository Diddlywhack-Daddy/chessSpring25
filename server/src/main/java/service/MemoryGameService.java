package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.*;

import java.util.concurrent.atomic.AtomicInteger;

public class MemoryGameService implements GameService {
    private final DataAccess data;
    private final AtomicInteger idCounter = new AtomicInteger(1);

    public MemoryGameService(DataAccess dataAccess) {
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
        return null;
    }

    @Override
    public BasicResult joinGame(JoinGameRequest request, String authToken) throws DataAccessException {
        return null;
    }
}
