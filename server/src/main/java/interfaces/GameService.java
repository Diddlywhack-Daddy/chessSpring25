package interfaces;
import dataaccess.DataAccessException;
import model.*;

public interface GameService {
    CreateGameResult createGame(CreateGameRequest req,String authToken) throws DataAccessException;
    BasicResult joinGame(JoinGameRequest req,String authToken) throws DataAccessException;
    ListGamesResult listGames(String authToken) throws DataAccessException;
}
