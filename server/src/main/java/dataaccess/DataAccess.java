package dataaccess;

import model.AuthData;
import model.UserData;
import model.GameData;

public interface DataAccess {
    void clear();

    //User stuff
    void createUser(UserData user) throws DataAccessException;
    UserData getUser(String username) throws DataAccessException;

    //Auth stuff
    void createAuth(AuthData auth) throws DataAccessException;
    AuthData getAuth(String authToken) throws DataAccessException;
    void deleteAuth(String authToken) throws DataAccessException;

    //Game stuff
    void createGame(GameData game) throws DataAccessException;
    GameData getGame(int gameID) throws DataAccessException;
    void updateGame(GameData game) throws DataAccessException;
    GameData[] listGames() throws DataAccessException;
}
