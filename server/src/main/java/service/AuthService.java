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


}
