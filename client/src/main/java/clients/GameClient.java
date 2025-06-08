package clients;

import backend.ServerFacade;
import chess.*;
import server.exceptions.BadRequestException;
import ui.EscapeSequences;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class GameClient extends Client {

    private final ServerFacade server;
    private final String serverUrl;

    public GameClient(String serverUrl) {
        super(serverUrl);
        this.serverUrl = serverUrl;
        server = new ServerFacade(serverUrl);
    }

    public String eval(String input) {
        var tokens = input.toLowerCase().split(" ");
        var cmd = (tokens.length > 0) ? tokens[0] : "help";
        var params = Arrays.copyOfRange(tokens, 1, tokens.length);
        return switch (cmd) {
            case "highlight" -> highlight(params);
            case "redraw" -> redraw();
            case "move" -> move(params);
            case "leave" -> leave();
            case "resign" -> resign();
            default -> help();
        };
    }

    private String highlight(String[] params) {
        try {
            if (params.length != 1) {
                throw new BadRequestException("Expected <position>");
            }

            ChessPosition position = parsePosition(params[0]);
            ChessPiece piece = game.getBoard().getPiece(position);

            if (piece == null) {
                throw new BadRequestException("There is no piece at this location.");
            }

            Collection<ChessMove> moves = game.validMoves(position);
            if (moves.isEmpty()) {
                throw new BadRequestException("This piece cannot move.");
            }

            drawBoardWithHighlights(userColor, game.getBoard(), moves);
            return "";
        } catch (BadRequestException e) {
            return e.getMessage();
        }
    }

    private String redraw() {
        drawBoardWithHighlights(userColor, game.getBoard(), new ArrayList<>());
        return "";
    }

    private String move(String[] params) {
        try {
            if (params.length < 2 || params.length > 3) {
                throw new BadRequestException("Expected: <source> <destination> <optional: promotion>");
            }

            ChessPosition start = parsePosition(params[0]);
            ChessPosition end = parsePosition(params[1]);
            ChessPiece.PieceType promotionPiece = (params.length == 3) ? convertStringToPiece(params[2]) : null;

            ChessMove move = new ChessMove(start, end, promotionPiece);
            if (game.validMoves(start).contains(move)) {
                // Placeholder for WebSocket or facade move call
                return "Move accepted (simulated).";
            } else {
                throw new BadRequestException("Invalid move.");
            }
        } catch (BadRequestException e) {
            return e.getMessage();
        }
    }

    private String leave() {
        game = null;
        return "You left the game.";
    }

    private String resign() {
        game = null;
        return "You resigned from the game.";
    }

    private String help() {
        return """
                Please choose one of the following options:
                - highlight <position> (ex. G7)
                - move <source> <destination> <optional: promotion> (ex. f5 e4 queen)
                - redraw (redraws the board)
                - resign (rage quit, game over you lose, returns to previous menu)
                - leave (returns to previous menu)
                - help (shows current options)
                """;
    }

    public void drawBoardWithHighlights(ChessGame.TeamColor color, ChessBoard board, Collection<ChessMove> highlights) {
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        drawHeader(out, color);

        Map<ChessPosition, ChessPiece> pieces = extractPieces(board);
        Set<ChessPosition> highlightTargets = new HashSet<>();
        ChessPosition start = null;

        for (ChessMove move : highlights) {
            highlightTargets.add(move.getEndPosition());
            start = move.getStartPosition();
        }

        List<Integer> rows = new ArrayList<>();
        List<Character> cols = new ArrayList<>();

        for (int r = 8; r >= 1; r--) rows.add(r);
        for (char c = 'a'; c <= 'h'; c++) cols.add(c);
        if (color == ChessGame.TeamColor.BLACK) {
            Collections.reverse(rows);
            Collections.reverse(cols);
        }

        for (int row : rows) {
            setBorderColor(out);
            out.print(" " + row + " ");
            for (char colChar : cols) {
                int col = colChar - 'a' + 1;
                ChessPosition pos = new ChessPosition(row, col);

                boolean isStart = start != null && start.equals(pos);
                boolean isHighlight = highlightTargets.contains(pos);
                ChessPiece piece = pieces.get(pos);

                printSquare(out, row, col, isStart, isHighlight, piece);
            }
            setBorderColor(out);
            out.print(" " + row + " ");
            resetColors(out);
            out.println();
        }

        drawHeader(out, color);
    }

    private void printSquare(PrintStream out, int row, int col, boolean isStart, boolean isHighlight, ChessPiece piece) {
        if (isStart) {
            out.print(EscapeSequences.SET_BG_COLOR_YELLOW);  // Start square
        } else if (isHighlight) {
            if ((row + col) % 2 == 0) {
                out.print(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);  // Highlight on light square
            } else {
                out.print(EscapeSequences.SET_BG_COLOR_DARK_GREY);   // Highlight on dark square
            }
        } else {
            if ((row + col) % 2 == 0) {
                out.print(EscapeSequences.SET_BG_COLOR_WHITE);       // Light square
            } else {
                out.print(EscapeSequences.SET_BG_COLOR_DARK_GREEN);  // Dark square
            }
        }

        out.print(piece == null ? EscapeSequences.EMPTY : getPieceSymbol(piece));
    }


    private void drawHeader(PrintStream out, ChessGame.TeamColor color) {
        setBorderColor(out);
        List<String> headers = Arrays.asList(" a ", " b ", " c ", " d ", " e ", " f ", " g ", " h ");
        if (color == ChessGame.TeamColor.BLACK) Collections.reverse(headers);
        out.print("   ");
        headers.forEach(out::print);
        out.print("   ");
        resetColors(out);
        out.println();
    }

    public Map<ChessPosition, ChessPiece> extractPieces(ChessBoard board) {
        Map<ChessPosition, ChessPiece> pieces = new HashMap<>();
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                if (piece != null) {
                    pieces.put(position, piece);
                }
            }
        }
        return pieces;
    }

    private String getPieceSymbol(ChessPiece piece) {
        return switch (piece.getTeamColor()) {
            case WHITE -> switch (piece.getPieceType()) {
                case KING -> EscapeSequences.WHITE_KING;
                case QUEEN -> EscapeSequences.WHITE_QUEEN;
                case BISHOP -> EscapeSequences.WHITE_BISHOP;
                case KNIGHT -> EscapeSequences.WHITE_KNIGHT;
                case ROOK -> EscapeSequences.WHITE_ROOK;
                case PAWN -> EscapeSequences.WHITE_PAWN;
            };
            case BLACK -> switch (piece.getPieceType()) {
                case KING -> EscapeSequences.BLACK_KING;
                case QUEEN -> EscapeSequences.BLACK_QUEEN;
                case BISHOP -> EscapeSequences.BLACK_BISHOP;
                case KNIGHT -> EscapeSequences.BLACK_KNIGHT;
                case ROOK -> EscapeSequences.BLACK_ROOK;
                case PAWN -> EscapeSequences.BLACK_PAWN;
            };
        };
    }

    private void setBorderColor(PrintStream out) {
        out.print(EscapeSequences.SET_BG_COLOR_DARK_GREEN);
        out.print(EscapeSequences.SET_TEXT_COLOR_BLACK);
    }


    private void resetColors(PrintStream out) {
        out.print(EscapeSequences.RESET_BG_COLOR);
        out.print(EscapeSequences.RESET_TEXT_COLOR);
    }

    private ChessPiece.PieceType convertStringToPiece(String piece) throws BadRequestException {
        return switch (piece.toLowerCase()) {
            case "queen" -> ChessPiece.PieceType.QUEEN;
            case "rook" -> ChessPiece.PieceType.ROOK;
            case "knight" -> ChessPiece.PieceType.KNIGHT;
            case "bishop" -> ChessPiece.PieceType.BISHOP;
            default -> throw new BadRequestException("Invalid promotion piece.");
        };
    }

    private ChessPosition parsePosition(String input) throws BadRequestException {
        if (input.length() != 2) throw new BadRequestException("Invalid position format.");
        int row = Character.getNumericValue(input.charAt(1));
        int col = switch (input.toLowerCase().charAt(0)) {
            case 'a' -> 1;
            case 'b' -> 2;
            case 'c' -> 3;
            case 'd' -> 4;
            case 'e' -> 5;
            case 'f' -> 6;
            case 'g' -> 7;
            case 'h' -> 8;
            default -> throw new BadRequestException("Invalid column letter.");
        };
        return new ChessPosition(row, col);
    }
}
