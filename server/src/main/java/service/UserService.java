package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import exceptions.AlreadyTakenException;
import exceptions.BadRequestException;
import exceptions.UnauthorizedException;
import model.request.LoginRequest;
import model.request.RegisterRequest;
import model.result.LoginResult;
import model.result.LogoutResult;
import model.result.RegisterResult;
import org.mindrot.jbcrypt.BCrypt;
import model.*;

import java.util.UUID;

public class UserService implements service.interfaces.UserService {
    private final DataAccess data;

    public UserService(DataAccess data) {
        this.data = data;
    }

    @Override
    public RegisterResult register(RegisterRequest request) throws DataAccessException, BadRequestException, AlreadyTakenException {
        if (request.username() == null || request.password() == null || request.email() == null) {
            throw new BadRequestException("Error: Invalid username or password.");
        }

        if (data.getUser(request.username()) != null) {
            throw new AlreadyTakenException("Error: Username already taken.");
        }

        String hashedPassword = BCrypt.hashpw(request.password(), BCrypt.gensalt());
        UserData user = new UserData(request.username(), hashedPassword, request.email());
        data.createUser(user);

        String token = UUID.randomUUID().toString();
        data.createAuth(new AuthData(token, request.username()));

        return new RegisterResult(request.username(), token);
    }

    @Override
    public LoginResult login(LoginRequest request) throws DataAccessException, UnauthorizedException, BadRequestException {
        if (request.username() == null || request.password() == null) {
            throw new BadRequestException("Error: Missing username or password.");
        }

        UserData user = data.getUser(request.username());
        if (user == null || !BCrypt.checkpw(request.password(), user.password())) {
            throw new UnauthorizedException("Error: Incorrect username or password.");
        }

        String token = UUID.randomUUID().toString();
        data.createAuth(new AuthData(token, user.username()));
        System.out.println("DEBUG: login returning token = " + token);
        return new LoginResult(user.username(), token);
    }

    public LogoutResult logout(String token) throws DataAccessException, UnauthorizedException {
        if (token == null || token.isBlank() || data.getAuth(token) == null) {
            throw new UnauthorizedException("Error: Invalid logout.");
        }

        data.deleteAuth(token);
        return new LogoutResult();
    }


}
