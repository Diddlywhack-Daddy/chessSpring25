package clients;

import backend.ServerFacade;
import com.sun.nio.sctp.NotificationHandler;

import java.util.Arrays;

public class PreLoginClient {
    private final ServerFacade server;

    private final String serverUrl;
    private final NotificationHandler notificationHandler;


    public PreLoginClient(String serverUrl, NotificationHandler notificationHandler){
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
        this.notificationHandler = notificationHandler;
    }

    public String eval(String input){
        try{
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
            }
        catch (ResponseException ex) {
                return ex.getMessage();
            }
        }
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
