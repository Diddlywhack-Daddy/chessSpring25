package clients;

import backend.ServerFacade;
import com.sun.nio.sctp.HandlerResult;
import com.sun.nio.sctp.Notification;
import com.sun.nio.sctp.NotificationHandler;

import java.util.Arrays;

public class PreLoginClient implements NotificationHandler{
    private final ServerFacade server;

    private final String serverUrl;


    public PreLoginClient(String serverUrl) {
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
                case "login" -> login();
                case "help" -> help();
                case "quit" -> "quit";
                default -> help();
            };



        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String register(String[] params) {
        String result = "Hit register";
        return result;
    }

    private String login() {
        String result = "Hit login";
        return result;
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
