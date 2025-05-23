package service.interfaces;

import dataaccess.DataAccessException;
import model.BasicResult;
import model.CreateGameRequest;
import model.CreateGameResult;
import model.JoinGameRequest;
import model.ListGamesResult;

public interface GameService {


    CreateGameResult createGame(CreateGameRequest request, String authToken) throws DataAccessException;


    ListGamesResult listGames(String authToken) throws DataAccessException;


    BasicResult joinGame(JoinGameRequest request, String authToken) throws DataAccessException;
}
