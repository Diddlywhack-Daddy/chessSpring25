package clients;

import backend.ServerFacade;
import com.sun.nio.sctp.HandlerResult;
import com.sun.nio.sctp.Notification;
import com.sun.nio.sctp.NotificationHandler;

import java.util.Arrays;

public class PostLoginClient extends Client implements NotificationHandler {
    private final ServerFacade server;
    private final String serverUrl;

    public PostLoginClient(String serverUrl) {
        super(serverUrl);
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
            case "listgames" -> listGames(params);
            case "new" -> createGame(params);
            case "play" -> playGame(params);
            case "observe" -> observeGame(params);
            case "quit" -> "quit";
            default -> help();
        };
    }

    private String createGame(String[] params) {
        String result = "Hit createGame";
        return result;
    }

    private String observeGame(String[] params) {
        String result = "Hit observeGame";
        return result;
    }

    private String playGame(String[] params) {
        String result = "Hit playGame";
        return result;
    }

    private String listGames(String[] params) {
        String result = "Hit listGames";
        return result;
    }

    private String logout() {
        String result = "Hit logout";
        return "quit";
    }

    public String help() {
        return """
                Please select one of the following options:

                help - lists possible commands
                logout - logs out the user
                listgames - displays a numbered list of all current games
                new <gameName> -Creates a new Chess game
                play <gameID> [WHITE or BLACK] - play chess
                observe <gameID> - joins the specified game as a spectator
                
                """;
    }

    @Override
    public HandlerResult handleNotification(Notification notification, Object o) {
        return null;
    }
}
