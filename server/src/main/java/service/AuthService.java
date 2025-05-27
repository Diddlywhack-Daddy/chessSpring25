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
        if (token == null || token.isBlank()) {
            throw new DataAccessException("Unauthorized"); // for 401
        }

        try {
            AuthData auth = db.getAuth(token);
            if (auth == null) {
                throw new DataAccessException("Unauthorized"); // for 401
            }
            return auth;
        } catch (DataAccessException e) {
            if ("Unauthorized".equalsIgnoreCase(e.getMessage())) {
                throw e;
            } else {
                throw new DataAccessException("Database failure", e); // 500
            }
        } catch (Exception e) {
            throw new DataAccessException("Database failure", e);
        }
    }





}
