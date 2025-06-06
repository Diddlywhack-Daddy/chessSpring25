package clients;

import backend.ServerFacade;
import com.sun.nio.sctp.HandlerResult;
import com.sun.nio.sctp.Notification;
import com.sun.nio.sctp.NotificationHandler;

import java.util.Arrays;

public class ChessClient implements NotificationHandler {
    private final ServerFacade server;
    private final String serverUrl;

    public ChessClient(String serverUrl) {
        this.serverUrl = serverUrl;
        server = new ServerFacade(serverUrl);
    }

    public String eval(String input) {
        var tokens = input.toLowerCase().split(" ");
        var cmd = (tokens.length > 0) ? tokens[0] : "help";
        var params = Arrays.copyOfRange(tokens, 1, tokens.length);
        return switch (cmd) {
            case "help" -> help();
            case "logout" -> logout();
            case "list" -> listGames(params);
            case "play" -> playGame(params);
            case "observe" -> observeGame(params);
            case "quit" -> "quit";
            default -> help();
        };
    }

    private String help() {
        return null;
    }

    private String logout() {
        return null;
    }

    private String listGames(String[] params) {
        return null;
    }

    private String playGame(String[] params) {
        return null;
    }

    private String observeGame(String[] params) {
        return null;
    }

    @Override
    public HandlerResult handleNotification(Notification notification, Object o) {
        return null;
    }
}
