package clients;

import backend.ServerFacade;
import chess.ChessGame;
import com.sun.nio.sctp.HandlerResult;
import com.sun.nio.sctp.Notification;
import com.sun.nio.sctp.NotificationHandler;
import exceptions.BadRequestException;
import model.AuthData;
import model.UserData;
import model.request.*;
import server.exceptions.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PostLoginClient extends Client implements NotificationHandler {
    private final ServerFacade server;
    private final String serverUrl;
    private final Map<Integer, Integer> gameNumberToId = new ConcurrentHashMap<>();
    private final Map<Integer, Integer> reverseGameIdToNumber = new ConcurrentHashMap<>();
    private int nextGameNumber = 1;
    private int lastCreatedGameNumber = -1;
    private GameClient gameClient;
    private ChessGame tempGame = new ChessGame();  // temporary placeholder game


    public PostLoginClient(String serverUrl, GameClient gameClient) {
        super(serverUrl);
        this.serverUrl = serverUrl;
        server = new ServerFacade(serverUrl);
        tempGame.getBoard().resetBoard();
        this.gameClient = gameClient;
    }

    public void setAuth(UserData user, AuthData auth){
        this.auth = auth;
        this.user = user;
    }

    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "logout" -> logout();
                case "listgames" -> listGames(params);
                case "new" -> createGame(params);
                case "play" -> playGame(params);
                case "observe" -> observeGame(params);
                default -> help();
            };
        } catch (BadRequestException e) {
            return e.getMessage();
        }
    }


    public void updateGameMapping() {
        try {
            var games = server.listGames(new ListGamesRequest(auth.authToken())).games();
            for (var game : games) {
                int gameId = game.gameID();
                if (!reverseGameIdToNumber.containsKey(gameId)) {
                    gameNumberToId.put(nextGameNumber, gameId);
                    reverseGameIdToNumber.put(gameId, nextGameNumber);
                    lastCreatedGameNumber = nextGameNumber;
                    nextGameNumber++;
                }
            }
        } catch (BadRequestException ignored) {}
    }


    public String createGame(String... params) throws BadRequestException {
        assertAuthenticated();
        assertNotEmpty(params);

        String name = String.join(" ", params);
        server.createGame(new CreateGameRequest(auth.authToken(), name));
        updateGameMapping();
        return String.format("%s created as game #%d.", name, lastCreatedGameNumber);
    }

    public String playGame(String... params) throws BadRequestException {
        assertAuthenticated();
        assertNotEmpty(params);

        if (params.length == 2) {
            int gameNumber = Integer.parseInt(params[0]);
            if (!gameNumberToId.containsKey(gameNumber)) {
                throw new BadRequestException("Invalid game number. Run listgames to list valid numbers, or create a new game.");
            }
            int gameId = gameNumberToId.get(gameNumber);

            ChessGame.TeamColor color = params[1].equalsIgnoreCase("white") ?
                    ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;

            userColor = color;

            gameClient.setGameAndColor(tempGame,color);
            server.joinGame(new JoinGameRequest(color, gameId, auth.authToken()));

            System.out.printf("You have joined game #%d as %s.%n", gameNumber, params[1].toUpperCase());
            return "play";
        }

        throw new BadRequestException("Expected: <gameNumber> [WHITE|BLACK]");
    }

    private String observeGame(String[] params) throws BadRequestException {
        assertAuthenticated();
        assertNotEmpty(params);

        int gameNumber = Integer.parseInt(params[0]);
        if (!gameNumberToId.containsKey(gameNumber)) {
            throw new BadRequestException("Invalid game number. Run listgames to list valid numbers, or create a new game.");
        }

        int gameId = gameNumberToId.get(gameNumber);


        // TEMP: Manually simulate game state until WebSocket phase
        game = new ChessGame(); // `game` is inherited from `Client`, so this sets state for GameClient
        game.getBoard().resetBoard();

        System.out.printf("You are observing game #%d.\n", gameNumber);
        return "observe"; // Causes REPL to drop into gameplay mode
    }


    public String listGames(String[] params) throws BadRequestException {
        assertAuthenticated();
        updateGameMapping();
        var games = server.listGames(new ListGamesRequest(auth.authToken())).games();
        if(games.isEmpty()){
            String result = "No created games";
            return result;
        }
        var result = new StringBuilder();

        for (var game : games) {
            int gameId = game.gameID();
            int gameNumber = reverseGameIdToNumber.get(gameId);
            result.append(gameNumber).append(". ");
            result.append("Game name: ").append(game.gameName()).append(" | ");
            result.append("White: ").append(game.whiteUsername()).append(" | ");
            result.append("Black: ").append(game.blackUsername()).append("\n");
        }

        return result.toString();
    }

    private String logout() throws BadRequestException {
        assertAuthenticated();
        System.out.println("Logging out with token: " + auth.authToken());
        server.logout(new LogoutRequest(auth.authToken()));
        auth = null;
        return "loggedOut";
    }

    public String help() {
        return """
                Please select one of the following options:

                help - lists possible commands
                logout - logs out the user
                listgames - displays a numbered list of all current games
                new <gameName> - Creates a new Chess game
                play <gameNumber> [WHITE or BLACK] - Joins an existing game as white or black
                observe <gameNumber> - Joins the specified game as a spectator
                """;
    }

    private void assertAuthenticated() throws BadRequestException {
        if (auth == null || auth.authToken() == null) {
            throw new BadRequestException("You must be logged in to perform this action.");
        }
    }




    @Override
    public HandlerResult handleNotification(Notification notification, Object o) {
        return null;
    }
}
