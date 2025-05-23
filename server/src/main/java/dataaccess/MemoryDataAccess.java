package dataaccess;

import model.AuthData;
import model.UserData;
import model.GameData;

import java.util.*;

public class MemoryDataAccess implements DataAccess {
    private static final MemoryDataAccess chessData = new MemoryDataAccess();
    public static MemoryDataAccess getInstance() { return chessData; }

    private final Map<String, UserData> users = new HashMap<>();
    private final Map<String, AuthData> authTokens = new HashMap<>();
    private final Map<Integer, GameData> games = new HashMap<>();
    private int nextGameID = 1;

    @Override
    public void clear() {
        users.clear();
        authTokens.clear();
        games.clear();
        nextGameID = 1;
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        if (users.containsKey(user.username())) throw new DataAccessException("Username already exists");
        users.put(user.username(), user);
    }

    @Override
    public UserData getUser(String username) {
        return users.get(username);
    }

    @Override
    public void createAuth(AuthData auth) {
        authTokens.put(auth.authToken(), auth);
    }

    @Override
    public AuthData getAuth(String token) {
        return authTokens.get(token);
    }

    @Override
    public void deleteAuth(String token) {
        authTokens.remove(token);
    }

    @Override
    public void createGame(GameData game) {
        games.put(game.gameID(), game);
    }

    @Override
    public GameData getGame(int gameID) {
        return games.get(gameID);
    }

    @Override
    public void updateGame(GameData game) {
        games.put(game.gameID(), game);
    }

    @Override
    public GameData[] listGames() {
        return games.values().toArray(new GameData[0]);
    }
}
