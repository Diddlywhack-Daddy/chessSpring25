package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.AuthData;
import model.RegisterRequest;
import model.UserData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import service.memoryimplementation.ClearService;
import service.memoryimplementation.GameService;
import service.memoryimplementation.UserService;

import static org.junit.jupiter.api.Assertions.fail;

public class ServiceTests {

    static ClearService clearService;
    static GameService gameService;
    static UserService userService;


    static MemoryDataAccess ChessData;

    @BeforeAll
    public static void init() {
        ChessData = new MemoryDataAccess();
        clearService = new ClearService(ChessData);
        gameService = new GameService(ChessData);
        userService = new UserService(ChessData);
    }


    @Test
    public void clearSuccess(){
        try {
            RegisterRequest request = new RegisterRequest("alice", "pass", "a@b.com");
            userService.register(request);        } catch (DataAccessException e) {
            fail("Unexpected error during setup");
        }

        clearService.clear();
        assert ChessData.getUsers().isEmpty();
        assert ChessData.getGames().isEmpty();
        assert ChessData.getAuthData().isEmpty();
    }
}
