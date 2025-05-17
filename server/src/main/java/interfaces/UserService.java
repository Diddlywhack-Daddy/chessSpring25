package interfaces;
import dataaccess.DataAccessException;
import model.*;

public interface UserService {
    AuthResult register(RegisterRequest req) throws DataAccessException;
    AuthResult login(LoginRequest req) throws DataAccessException;
    BasicResult logout(String authToken) throws DataAccessException;
}
