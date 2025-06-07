package clients;

import backend.ServerFacade;
import chess.ChessGame;
import com.sun.nio.sctp.HandlerResult;
import com.sun.nio.sctp.Notification;
import com.sun.nio.sctp.NotificationHandler;
import server.exceptions.BadRequestException;

import java.util.Arrays;

public class GameClient extends Client implements NotificationHandler {
    private final ServerFacade server;
    private final String serverUrl;

    public GameClient(String serverUrl) {
        super(serverUrl);
        this.serverUrl = serverUrl;
        server = new ServerFacade(serverUrl);
    }

    public String eval(String input) throws BadRequestException {
        var tokens = input.toLowerCase().split(" ");
        var cmd = (tokens.length > 0) ? tokens[0] : "help";
        var params = Arrays.copyOfRange(tokens, 1, tokens.length);
        return switch (cmd) {
            case "redraw" -> redraw();
            case "highlight" -> highlight(params);
            case "move" -> move(params);
            case "observe" -> observe(params);
            case "leave" -> leave();
            case "resign" -> resign();
            case "quit" -> "quit";
            case "clear" -> clear(params);
            default -> help();
        };
    }

    private String resign() {
        return null;
    }

    private String leave() {
        return null;
    }

    private String observe(String[] params) {
        return null;
    }

    private String move(String[] params) {
        return null;
    }

    private String highlight(String[] params) {
        return null;
    }

    private String redraw() {
        return null;
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

    public String printBoard(ChessGame.TeamColor white, ChessGame game){
        return null;
    }

    @Override
    public HandlerResult handleNotification(Notification notification, Object o) {
        return null;
    }
}
