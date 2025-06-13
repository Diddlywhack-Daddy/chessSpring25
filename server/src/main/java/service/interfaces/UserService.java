package service.interfaces;

import dataaccess.DataAccessException;
import model.request.LoginRequest;
import model.request.RegisterRequest;
import model.result.LoginResult;
import model.result.LogoutResult;
import model.result.RegisterResult;
import exceptions.AlreadyTakenException;
import exceptions.BadRequestException;
import exceptions.UnauthorizedException;

public interface UserService {
    RegisterResult register(RegisterRequest req) throws DataAccessException, BadRequestException, AlreadyTakenException;
    LoginResult login(LoginRequest req) throws DataAccessException, UnauthorizedException, BadRequestException;
    LogoutResult logout(String authToken) throws Exception;
}
