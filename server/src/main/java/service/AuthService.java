package service;

import dataaccess.DataAccess;

public class AuthService {
    private final DataAccess db;

    public AuthService(DataAccess db) {
        this.db = db;
    }


}
