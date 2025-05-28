package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import server.exceptions.UnauthorizedException;

public class AuthService {
    private final DataAccess db;

    public AuthService(DataAccess db) {
        this.db = db;
    }

    public AuthData validateToken(String token) throws DataAccessException, UnauthorizedException {
        if (token == null || token.isBlank()) {
            throw new UnauthorizedException("Error: Unauthorized access.");
        }

        AuthData auth = db.getAuth(token);
        if (auth == null) {
            throw new UnauthorizedException("Error: Unauthorized access.");
        }

        return auth;
    }
}
