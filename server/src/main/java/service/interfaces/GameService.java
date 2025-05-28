package service.interfaces;

import dataaccess.DataAccessException;
import model.BasicResult;
import model.request.CreateGameRequest;
import model.result.CreateGameResult;
import model.request.JoinGameRequest;
import model.result.ListGamesResult;

public interface GameService {


    CreateGameResult createGame(CreateGameRequest request, String authToken) throws DataAccessException;


    ListGamesResult listGames(String authToken) throws DataAccessException;


    BasicResult joinGame(JoinGameRequest request, String authToken) throws DataAccessException;
}
