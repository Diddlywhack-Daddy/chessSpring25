package service.interfaces;

import dataaccess.DataAccessException;
import model.AuthResult;
import model.BasicResult;
import model.request.LoginRequest;
import model.request.RegisterRequest;

public interface UserService {
    AuthResult register(RegisterRequest req) throws DataAccessException;
    AuthResult login(LoginRequest req) throws DataAccessException;
    BasicResult logout(String authToken) throws Exception;
}
