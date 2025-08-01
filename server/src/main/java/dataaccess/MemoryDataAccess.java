package dataaccess;

import model.AuthData;
import model.UserData;
import model.GameData;

import java.util.*;

public class MemoryDataAccess implements DataAccess {
    private static final MemoryDataAccess CHESS_DATA = new MemoryDataAccess();

    public static MemoryDataAccess getInstance() {
        return CHESS_DATA;
    }

    private final Map<String, UserData> users = new HashMap<>();
    private final Map<String, AuthData> authTokens = new HashMap<>();
    private final Map<Integer, GameData> games = new HashMap<>();
    private int nextGameID = 1;

    public Map<String, UserData> getUsers() {
        return users;
    }

    public Map<String, AuthData> getAuthData() {
        return authTokens;
    }

    public Map<Integer, GameData> getGames() {
        return games;
    }

    @Override
    public void clear() {
        users.clear();
        authTokens.clear();
        games.clear();
        nextGameID = 1;
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        if (users.containsKey(user.username())) {
            throw new DataAccessException("Username already exists");
        }
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
    public int createGame(GameData game) {
        games.put(game.gameID(), game);
        return game.gameID();
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
