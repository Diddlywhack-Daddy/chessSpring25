package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.AuthResult;
import model.RegisterRequest;
import model.UserData;

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
    public AuthResult login(model.LoginRequest request) throws DataAccessException {
        throw new UnsupportedOperationException("Login not yet implemented");
    }

    @Override
    public model.BasicResult logout(String authToken) throws DataAccessException {
        throw new UnsupportedOperationException("Logout not yet implemented");
    }
}
