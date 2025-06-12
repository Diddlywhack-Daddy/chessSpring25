package clients;

import backend.ServerFacade;
import com.sun.nio.sctp.HandlerResult;
import com.sun.nio.sctp.Notification;
import com.sun.nio.sctp.NotificationHandler;
import model.AuthData;
import model.UserData;
import model.request.LoginRequest;
import model.request.RegisterRequest;
import model.result.LoginResult;
import model.result.RegisterResult;
import server.exceptions.BadRequestException;

import java.util.Arrays;

public class PreLoginClient extends Client implements NotificationHandler {
    private final ServerFacade server;
    private final String serverUrl;

    public PreLoginClient(String serverUrl) {
        super(serverUrl);
        this.serverUrl = serverUrl;
        server = new ServerFacade(serverUrl);
    }

    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "register" -> register(params);
                case "login" -> login(params);
                case "help" -> help();
                case "quit" -> "quit";
                default -> help();
            };
        } catch (BadRequestException e) {
            return e.getMessage();
        }
    }

    public String register(String[] params) {
        if (params.length == 3) {
            try {
                assertNotEmpty(params);
                user = new UserData(params[0], params[1], params[2]);
                RegisterRequest request = new RegisterRequest(user.username(), user.password(), user.email());
                RegisterResult result = server.register(request);
                return String.format("Successfully registered %s. Please Log in.", result.username());
            } catch (BadRequestException e) {
                return e.getMessage();
            }
        }
        return "Expected: <username> <password> <email>";
    }

    private String login(String[] params) {
        if (params.length != 2) {
            return "Expected: <username> <password>";
        }
        try {
            assertNotEmpty(params);
            user = new UserData(params[0], params[1], null);
            LoginResult result = server.login(new LoginRequest(user.username(), user.password()));
            auth = new AuthData(result.authToken(), result.username());
            return "postLogin";
        } catch (BadRequestException e) {
            return e.getMessage();
        }
    }

    public String help() {
        return """
               Please choose one of the following options:

               register <USERNAME> <PASSWORD> <EMAIL> - creates an account
               login <USERNAME> <PASSWORD> - logs in to play chess
               help - lists possible commands
               quit - exits the program

               """;
    }

    @Override
    public HandlerResult handleNotification(Notification notification, Object o) {
        return null;
    }
}
