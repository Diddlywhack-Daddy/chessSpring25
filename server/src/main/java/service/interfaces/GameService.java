package service.interfaces;

import dataaccess.DataAccessException;
import model.request.CreateGameRequest;
import model.result.CreateGameResult;
import model.request.JoinGameRequest;
import model.result.JoinGameResult;
import model.result.ListGamesResult;
import server.exceptions.AlreadyTakenException;
import server.exceptions.BadRequestException;
import server.exceptions.UnauthorizedException;

public interface GameService {


    CreateGameResult createGame(CreateGameRequest request, String authToken)
            throws DataAccessException, BadRequestException, UnauthorizedException;


    ListGamesResult listGames(String authToken)
            throws DataAccessException, UnauthorizedException;


    JoinGameResult joinGame(JoinGameRequest request, String authToken)
            throws DataAccessException, UnauthorizedException, BadRequestException, AlreadyTakenException;
}
