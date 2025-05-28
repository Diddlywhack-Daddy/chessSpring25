package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;

public class AuthService {
    private final DataAccess db;

    public AuthService(DataAccess db) {
        this.db = db;
    }


    public AuthData validateToken(String token) throws DataAccessException {
        System.out.println("Validating token: " + token);
        if (token == null || token.isBlank()) {
            System.out.println("Token was null or blank.");
            throw new DataAccessException("Unauthorized");
        }

        try {
            AuthData auth = db.getAuth(token);
            System.out.println("Token validated for user: " + auth.username());
            return auth;
        } catch (DataAccessException e) {
            System.out.println("Token validation failed: " + e.getMessage());
            if ("Unauthorized".equalsIgnoreCase(e.getMessage())) {
                throw e;
            }
            throw new DataAccessException("Database failure", e);
        }
    }







}
