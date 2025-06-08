package clients;

import backend.ServerFacade;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import com.sun.nio.sctp.HandlerResult;
import com.sun.nio.sctp.Notification;
import com.sun.nio.sctp.NotificationHandler;
import model.request.CreateGameRequest;
import model.request.JoinGameRequest;
import model.request.ListGamesRequest;
import model.request.LogoutRequest;
import server.exceptions.BadRequestException;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PostLoginClient extends Client implements NotificationHandler {
    private final ServerFacade server;
    private final String serverUrl;
    private final Map<Integer, Integer> gameNumberToId = new ConcurrentHashMap<>();
    private final Map<Integer, Integer> reverseGameIdToNumber = new ConcurrentHashMap<>();
    private int nextGameNumber = 1;
    private int lastCreatedGameNumber = -1;
    private ChessGame tempGame = new ChessGame();  // temporary placeholder game


    public PostLoginClient(String serverUrl) {
        super(serverUrl);
        this.serverUrl = serverUrl;
        server = new ServerFacade(serverUrl);
        tempGame.getBoard().resetBoard();
        updateGameMapping();
    }

    public String eval(String input) throws BadRequestException {
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
            default -> help();
        };
    }

    private void updateGameMapping() {
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

    public void printBoard(ChessGame.TeamColor color, ChessGame game) {
        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        this.drawHeader(out, color);
        Collection<ChessMove> moves = new ArrayList();
        this.drawBoard(out, game, color, moves);
        this.drawHeader(out, color);
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
            gameClient = new GameClient(serverUrl);
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

        // Join as observer (color = null)
        server.joinGame(new JoinGameRequest(null, gameId, auth.authToken()));

        // Prepare the client and assign the temporary game state
        gameClient = new GameClient(serverUrl);
        gameClient.userColor = ChessGame.TeamColor.WHITE;
        gameClient.game = tempGame;

        gameClient.printBoard(gameClient.userColor, gameClient.game);

        return String.format("You have joined game #%d as an observer.", gameNumber);
    }


    public String listGames(String[] params) throws BadRequestException {
        assertAuthenticated();
        updateGameMapping();

        var games = server.listGames(new ListGamesRequest(auth.authToken())).games();
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

    private void drawHeader(PrintStream out, ChessGame.TeamColor color) {
        setBorderColor(out);
        List<String> headers = Arrays.asList(" a ", "  b ", "  c ", " d ", "  e ", "  f ", " g ", "  h ");
        if (color.equals(ChessGame.TeamColor.BLACK)) {
            headers = headers.reversed();
        }

        out.print("   ");
        Iterator var4 = headers.iterator();

        while(var4.hasNext()) {
            String c = (String)var4.next();
            out.print(c);
        }

        out.print("   ");
        resetColors(out);
        out.println();
    }

    private void drawBoard(PrintStream out, ChessGame game, ChessGame.TeamColor color, Collection<ChessMove> moves) {
        out.print("\u001b[38;5;15m");
        List<String> rowNums = Arrays.asList(" 8 ", " 7 ", " 6 ", " 5 ", " 4 ", " 3 ", " 2 ", " 1 ");
        ChessPiece[][] pieces = game.getBoard().getPieces();
        Collection<ChessPosition> highlights = new ArrayList();
        ChessPosition start = new ChessPosition(-1, -1);
        Iterator var9 = moves.iterator();

        while(var9.hasNext()) {
            ChessMove move = (ChessMove)var9.next();
            highlights.add(move.getEndPosition());
            if (color == ChessGame.TeamColor.BLACK) {
                start = new ChessPosition(move.getStartPosition().getRow() - 1, 8 - move.getStartPosition().getColumn());
            } else {
                start = new ChessPosition(8 - move.getStartPosition().getRow(), move.getStartPosition().getColumn() - 1);
            }
        }

        int row;
        ChessPiece[][] blackPieces;
        if (color.equals(ChessGame.TeamColor.BLACK)) {
            rowNums = rowNums.reversed();
            row = 1;
            blackPieces = new ChessPiece[8][8];

            for(Iterator var11 = Arrays.stream(pieces).toList().iterator(); var11.hasNext(); ++row) {
                ChessPiece[] row = (ChessPiece[])var11.next();
                blackPieces[8 - row] = (ChessPiece[])Arrays.asList(row).reversed().toArray(new ChessPiece[8]);
            }

            pieces = blackPieces;
            Collection<ChessPosition> newPositions = new ArrayList();
            Iterator var27 = highlights.iterator();

            while(var27.hasNext()) {
                ChessPosition highlight = (ChessPosition)var27.next();
                newPositions.add(new ChessPosition(highlight.getRow() - 1, 8 - highlight.getColumn()));
            }

            highlights = newPositions;
        } else {
            Collection<ChessPosition> newPositions = new ArrayList();
            Iterator var23 = highlights.iterator();

            while(var23.hasNext()) {
                ChessPosition highlight = (ChessPosition)var23.next();
                newPositions.add(new ChessPosition(8 - highlight.getRow(), highlight.getColumn() - 1));
            }

            highlights = newPositions;
        }

        row = 0;
        blackPieces = pieces;
        int var26 = pieces.length;

        for(int var28 = 0; var28 < var26; ++var28) {
            ChessPiece[] pieceRow = blackPieces[var28];
            setBorderColor(out);
            out.print((String)rowNums.get(row));
            int index = 0;
            ChessPiece[] var15 = pieceRow;
            int var16 = pieceRow.length;

            for(int var17 = 0; var17 < var16; ++var17) {
                ChessPiece piece = var15[var17];
                ChessPosition position = new ChessPosition(row, index);
                if (highlights.contains(position)) {
                    this.highlightPiece(out, index, row);
                } else if (position.equals(start)) {
                    out.print("\u001b[48;2;220;220;220m");
                } else {
                    this.drawPiece(out, index, row);
                }

                if (piece == null) {
                    out.print("   ");
                } else {
                    out.print(this.getPiece(piece));
                }

                ++index;
            }

            setBorderColor(out);
            out.print((String)rowNums.get(row));
            resetColors(out);
            out.println();
            ++row;
        }

    }


    @Override
    public HandlerResult handleNotification(Notification notification, Object o) {
        return null;
    }
}
