package service.memoryImplementation;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.*;
import service.interfaces.UserService;

import java.util.UUID;

public class MemoryUserService implements UserService {
    private final DataAccess data;

    public MemoryUserService(DataAccess dataAccess) {
        this.data = dataAccess;
    }

    @Override
    public AuthResult register(RegisterRequest request) throws DataAccessException {
        if (request.username() == null || request.password() == null || request.email() == null) {
            throw new IllegalArgumentException("Missing fields");
        }

        var user = new UserData(request.username(), request.password(), request.email());
        data.createUser(user);

        var token = UUID.randomUUID().toString();
        var auth = new AuthData(token, request.username());
        data.createAuth(auth);

        return new AuthResult(request.username(), token);
    }
    @Override
    public AuthResult login(LoginRequest request) throws DataAccessException {
        if (request.username() == null || request.password() == null) {
            throw new IllegalArgumentException("Missing username or password");
        }

        UserData user = data.getUser(request.username());
        if (user == null || !user.password().equals(request.password())) {
            throw new DataAccessException("Invalid credentials");
        }
        String token = UUID.randomUUID().toString();
        AuthData auth = new AuthData(token, user.username());
        data.createAuth(auth);

        return new AuthResult(user.username(), token);
    }



    @Override
    public BasicResult logout(String authToken) throws DataAccessException {
        if (authToken == null || data.getAuth(authToken) == null) {
            throw new dataaccess.DataAccessException("Invalid token");
        }

        data.deleteAuth(authToken);
        return new BasicResult(true, null);
    }

}
